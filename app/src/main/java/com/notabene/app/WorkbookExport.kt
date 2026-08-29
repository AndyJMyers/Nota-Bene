package com.notabene.app

import kotlinx.coroutines.flow.first
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private data class ExportSheet(val name: String, val rows: List<List<Any?>>)

suspend fun exportWorkbook(database: NotaBeneDatabase, output: OutputStream) {
    val payments = database.paymentDao().observeAll().first()
    val asks = database.askDao().observeAll().first()
    val tasks = database.taskDao().observeAll().first()
    val body = database.bodyDao().observeAll().first()
    val medicines = database.medicationDao().observeMedications().first()
    val doses = database.medicationDao().observeDoseLogs().first()
    val medicineNames = medicines.associate { it.id to it.name }

    val sheets = listOf(
        ExportSheet("SPEND", listOf(listOf("Date", "Merchant", "Amount", "Note", "Captured from")) + payments.map {
            listOf(exportDate(it.createdAt), it.merchant, it.amount, it.note, it.capturedFrom)
        }),
        ExportSheet("ASK", listOf(listOf("Date", "Question", "Completed")) + asks.map {
            listOf(exportDate(it.createdAt), it.text, yesNo(it.done))
        }),
        ExportSheet("TASK", listOf(listOf("Date", "Task", "Waiting on", "Completed")) + tasks.map {
            listOf(exportDate(it.createdAt), it.text, it.waitingOn, yesNo(it.done))
        }),
        ExportSheet("SOMA", listOf(listOf("Date", "Observation", "Measurement")) + body.map {
            listOf(exportDate(it.createdAt), it.observation, it.measurement)
        }),
        ExportSheet(
            "MEDS",
            listOf(listOf("Record type", "Medicine", "Dosage / dose date", "First reminder / scheduled", "Usual per day", "Stock / taken", "Reorder at", "Status")) +
                medicines.map {
                    listOf("Medicine", it.name, it.dosage, it.doseTime, it.dailyTarget, it.startingDoses, it.reorderAt, if (it.active) "Active" else "Halted")
                } + doses.map {
                    listOf("Dose taken", medicineNames[it.medicationId] ?: "Medicine #${it.medicationId}", it.doseDate, exportDate(it.scheduledFor), "", exportDate(it.takenAt), "", "Taken")
                }
        )
    )

    ZipOutputStream(output.buffered()).use { zip ->
        zip.xml("[Content_Types].xml", contentTypes(sheets.size))
        zip.xml("_rels/.rels", rootRelationships)
        zip.xml("xl/workbook.xml", workbookXml(sheets))
        zip.xml("xl/_rels/workbook.xml.rels", workbookRelationships(sheets.size))
        zip.xml("xl/styles.xml", stylesXml)
        sheets.forEachIndexed { index, sheet ->
            zip.xml("xl/worksheets/sheet${index + 1}.xml", sheetXml(sheet.rows))
        }
    }
}

private val exportDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun exportDate(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault()).format(exportDateFormat)

private fun yesNo(value: Boolean) = if (value) "Yes" else "No"

private fun ZipOutputStream.xml(path: String, content: String) {
    putNextEntry(ZipEntry(path))
    write(content.toByteArray(Charsets.UTF_8))
    closeEntry()
}

private fun sheetXml(rows: List<List<Any?>>): String = buildString {
    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
    append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
    rows.forEachIndexed { rowIndex, row ->
        append("<row r=\"${rowIndex + 1}\">")
        row.forEachIndexed { columnIndex, value ->
            val reference = "${columnName(columnIndex)}${rowIndex + 1}"
            if (value is Number) {
                append("<c r=\"$reference\"><v>$value</v></c>")
            } else {
                val style = if (rowIndex == 0) " s=\"1\"" else ""
                append("<c r=\"$reference\" t=\"inlineStr\"$style><is><t xml:space=\"preserve\">${xml(value?.toString().orEmpty())}</t></is></c>")
            }
        }
        append("</row>")
    }
    append("</sheetData></worksheet>")
}

private fun columnName(index: Int): String {
    var number = index + 1
    return buildString {
        while (number > 0) {
            insert(0, ('A'.code + (number - 1) % 26).toChar())
            number = (number - 1) / 26
        }
    }
}

private fun xml(value: String): String = value
    .filter { it == '\n' || it == '\r' || it == '\t' || it.code >= 32 }
    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    .replace("\"", "&quot;").replace("'", "&apos;")

private fun contentTypes(sheetCount: Int) = buildString {
    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
    append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
    append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
    append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
    append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
    repeat(sheetCount) { append("<Override PartName=\"/xl/worksheets/sheet${it + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>") }
    append("</Types>")
}

private fun workbookXml(sheets: List<ExportSheet>) = buildString {
    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>")
    sheets.forEachIndexed { index, sheet -> append("<sheet name=\"${sheet.name}\" sheetId=\"${index + 1}\" r:id=\"rId${index + 1}\"/>") }
    append("</sheets></workbook>")
}

private fun workbookRelationships(sheetCount: Int) = buildString {
    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
    repeat(sheetCount) { append("<Relationship Id=\"rId${it + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet${it + 1}.xml\"/>") }
    append("<Relationship Id=\"rId${sheetCount + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>")
    append("</Relationships>")
}

private const val rootRelationships = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

private const val stylesXml = """<?xml version="1.0" encoding="UTF-8"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts>
<fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills><borders count="1"><border/></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="2"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>"""
