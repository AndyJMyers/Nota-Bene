# Import and migration — Alpha 37

Alpha 38 fixes Android's unsupported XML parser feature settings. XML is decoded strictly as UTF-8 before validation and parsing; DTD/entity declarations and external references are rejected. Android instrumentation covers all five record types, repeated doses, preservation of existing records, rollback and unsafe XML. Development builds now use a separate `.dev` application ID to support device tests without replacing either user installation.

Verification: 29 JVM tests passed. Both `WorkbookImportDeviceTest` instrumentation tests passed on the connected Android phone on 4 September 2026. These tests used generated example records and an in-memory database; the user's export has not yet been imported into the Play app.

Settings (`*`) contains EXPORT XLSX and IMPORT XLSX. Import uses Android's file picker, validates the original Nota Bene five-sheet export, and shows counts for all record types before confirmation. It appends records in a single transaction. Existing records are retained; importing a file again creates duplicates.

Keep the original installation and export as your backup. Use the same time zone when exporting and importing: old exports store local timestamps to minute precision. Medicine creation dates and interface preferences are absent from the old format. Medicine stock, active/halted status, usual daily counts and dose history are restored. Ambiguous name-based dose links are rejected before any database write.

Supported input is an unedited Nota Bene XLSX export. Formula cells, unsupported layouts, oversized archives and malformed values are rejected. This is not a general Excel importer.

The Play-installed app must receive Alpha 37 through Google Play, signed by Play App Signing. A locally signed development APK cannot replace the Play installation. Do not uninstall either existing installation to work around a signature mismatch.
