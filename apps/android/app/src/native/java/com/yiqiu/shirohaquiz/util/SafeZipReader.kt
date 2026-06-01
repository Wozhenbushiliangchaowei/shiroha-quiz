package com.yiqiu.shirohaquiz.util

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object SafeZipReader {
    fun normalizeEntryName(rawName: String): String {
        val name = rawName.trim()
        require(isSafeEntryName(name)) { "Unsafe ZIP entry name: $rawName" }
        return name
    }

    fun isSafeEntryName(rawName: String): Boolean {
        val name = rawName.trim()
        if (name.isBlank()) return false
        if (name.indexOf('\u0000') >= 0) return false
        if (name.contains('\\')) return false
        if (name.startsWith("/") || name.startsWith("./")) return false
        if (Regex("""^[A-Za-z]:""").containsMatchIn(name)) return false
        return name.split('/').all { segment ->
            segment.isNotBlank() && segment != "." && segment != ".."
        }
    }

    fun readEntryBytes(
        zip: ZipInputStream,
        entry: ZipEntry,
        maxSize: Long
    ): ByteArray {
        require(maxSize > 0) { "ZIP entry size limit must be positive." }
        if (entry.size > maxSize) {
            throw IllegalArgumentException("ZIP entry too large: ${entry.name}")
        }

        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = zip.read(buffer)
            if (read <= 0) break
            if (total + read > maxSize) {
                throw IllegalArgumentException("ZIP entry too large: ${entry.name}")
            }
            output.write(buffer, 0, read)
            total += read
        }
        return output.toByteArray()
    }
}
