package com.notabene.app

import androidx.room.withTransaction
import java.io.InputStream
import java.io.StringReader
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/** Reads the original, unedited five-sheet Nota Bene export. Never executes formulas. */
internal fun readWorkbook(input: InputStream): WorkbookSnapshot {
    val entries = mutableMapOf<String, ByteArray>()
    var total = 0
    ZipInputStream(input).use { zip ->
        var count = 0
        while (true) {
            val entry = zip.nextEntry ?: break
            require(++count <= 100) { "Too many files in workbook" }
            val bytes = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = zip.read(buffer)
                if (n < 0) break
                total += n
                require(total <= 8_000_000) { "Workbook exceeds the 8 MB import limit" }
                bytes.write(buffer, 0, n)
            }
            require(entries.put(entry.name, bytes.toByteArray()) == null) { "Duplicate workbook file" }
        }
    }
    fun xml(path: String): Element {
        val bytes = entries[path] ?: error("Missing workbook file: $path")
        // Parse precisely the UTF-8 text we validate. Android's DOM factory does not
        // support the desktop SAX feature switches for external entities.
        val text = Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        require(!text.contains('\u0000') && !text.contains("<!DOCTYPE", true) && !text.contains("<!ENTITY", true)) {
            "Unsupported XML declaration"
        }
        val factory = DocumentBuilderFactory.newInstance()
        factory.isExpandEntityReferences = false
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver { _, _ -> throw SAXException("External XML references are forbidden") }
        return builder.parse(InputSource(StringReader(text))).documentElement
    }
    val names = listOf("SPEND", "ASK", "TASK", "SOMA", "MEDS")
    val sheets = xml("xl/workbook.xml").getElementsByTagName("sheet")
    require(sheets.length == 5) { "Choose an original five-sheet Nota Bene export" }
    names.forEachIndexed { i, name ->
        val sheet = sheets.item(i) as Element
        require(sheet.getAttribute("name") == name && sheet.getAttribute("r:id") == "rId${i + 1}") {
            "Choose an original, unedited Nota Bene export"
        }
    }
    val headers = listOf(
        listOf("Date", "Merchant", "Amount", "Note", "Captured from"),
        listOf("Date", "Question", "Completed"),
        listOf("Date", "Task", "Waiting on", "Completed"),
        listOf("Date", "Observation", "Measurement"),
        listOf("Record type", "Medicine", "Dosage / dose date", "First reminder / scheduled", "Usual per day", "Stock / taken", "Reorder at", "Status")
    )
    val tables = names.indices.map { index ->
        val root = xml("xl/worksheets/sheet${index + 1}.xml")
        require(root.getElementsByTagName("f").length == 0) { "Formula cells cannot be imported" }
        val rows = root.getElementsByTagName("row")
        require(rows.length in 1..20001) { "Invalid number of rows" }
        val table = (0 until rows.length).map { rowIndex ->
            val cells = (rows.item(rowIndex) as Element).getElementsByTagName("c")
            require(cells.length == headers[index].size) { "Missing cells in ${names[index]} row ${rowIndex + 1}" }
            (0 until cells.length).map { column ->
                val cell = cells.item(column) as Element
                require(cell.getAttribute("r") == "${'A' + column}${rowIndex + 1}") { "Reordered cells are unsupported" }
                val type = cell.getAttribute("t")
                require(type == "inlineStr" || type.isEmpty()) { "Use the original export, without spreadsheet edits" }
                cell.textContent.also { require(it.length <= 100000) { "Cell is too long" } }
            }
        }
        require(table.first() == headers[index]) { "Unrecognised ${names[index]} columns" }
        table.drop(1)
    }
    fun date(value: String) = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(java.time.format.ResolverStyle.STRICT))
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    fun yes(value: String): Boolean { require(value in listOf("Yes", "No")); return value == "Yes" }
    val medicines = tables[4].filter { it[0] == "Medicine" }.mapIndexed { i, r ->
        LocalTime.parse(r[3])
        require(r[1].isNotBlank() && r[4].toInt() > 0 && r[5].toInt() >= 0 && r[6].toInt() >= 0)
        require(r[7] in listOf("Active", "Halted"))
        Medication((i + 1).toLong(), r[1], r[2], r[3], r[4].toInt(), r[5].toInt(), r[6].toInt(), r[7] == "Active")
    }
    require(tables[4].all { it[0] in listOf("Medicine", "Dose taken") }) { "Unrecognised MEDS record" }
    val doses = tables[4].filter { it[0] == "Dose taken" }.map { r ->
        val matches = medicines.filter { it.name == r[1] }
        require(matches.size == 1) { "Dose history for '${r[1]}' cannot be matched uniquely. No records were imported." }
        LocalDate.parse(r[2])
        require(r[7] == "Taken")
        DoseLog(medicationId = matches.single().id, doseDate = r[2], scheduledFor = date(r[3]), takenAt = date(r[5]))
    }
    return WorkbookSnapshot(
        payments = tables[0].map { PaymentRecord(merchant = it[1], amount = it[2], note = it[3], capturedFrom = it[4], createdAt = date(it[0])) },
        asks = tables[1].map { AskItem(text = it[1], done = yes(it[2]), createdAt = date(it[0])) },
        tasks = tables[2].map { TaskItem(text = it[1], waitingOn = it[2], done = yes(it[3]), createdAt = date(it[0])) },
        body = tables[3].map { BodyItem(observation = it[1], measurement = it[2], createdAt = date(it[0])) },
        medicines = medicines, doses = doses
    )
}

internal suspend fun importWorkbook(database: NotaBeneDatabase, snapshot: WorkbookSnapshot) {
    database.withTransaction {
        snapshot.payments.forEach { database.paymentDao().insert(it.copy(id = 0)) }
        snapshot.asks.forEach { database.askDao().insert(it.copy(id = 0)) }
        snapshot.tasks.forEach { database.taskDao().insert(it.copy(id = 0)) }
        snapshot.body.forEach { database.bodyDao().insert(it.copy(id = 0)) }
        val ids = snapshot.medicines.associate { it.id to database.medicationDao().insertMedication(it.copy(id = 0)) }
        snapshot.doses.forEach { database.medicationDao().insertDoseLog(it.copy(id = 0, medicationId = ids.getValue(it.medicationId))) }
    }
}

internal fun WorkbookSnapshot.importSummary() = "SPEND: ${payments.size}\nMEDS: ${medicines.size} medicines, ${doses.size} dose records\nSOMA: ${body.size}\nTASK: ${tasks.size}\nASK: ${asks.size}"
