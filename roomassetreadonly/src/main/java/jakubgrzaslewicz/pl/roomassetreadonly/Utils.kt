package jakubgrzaslewicz.pl.roomassetreadonly


import android.util.Log


import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Scanner
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal object Utils {

    private val TAG = SQLiteAssetROHelper.TAG

    fun splitSqlScript(script: String, delim: Char): List<String> {
        val statements = ArrayList<String>()
        var sb = StringBuilder()
        var inLiteral = false
        val content = script.toCharArray()
        for (i in 0 until script.length) {
            if (content[i] == '"') {
                inLiteral = !inLiteral
            }
            if (content[i] == delim && !inLiteral) {
                if (sb.length > 0) {
                    statements.add(sb.toString().trim { it <= ' ' })
                    sb = StringBuilder()
                }
            } else {
                sb.append(content[i])
            }
        }
        if (sb.length > 0) {
            statements.add(sb.toString().trim { it <= ' ' })
        }
        return statements
    }

    @Throws(IOException::class)
    fun writeExtractedFileToDisk(`in`: InputStream, outs: OutputStream) {
        `in`.copyTo(outs)
        `in`.close()
        outs.close()
    }

    @Throws(IOException::class)
    fun getFileFromZip(zipFileStream: InputStream): ZipInputStream? {
        val zis = ZipInputStream(zipFileStream)
        val ze: ZipEntry?
        ze = zis.nextEntry
        if (ze != null) {
            Log.d(TAG, "extracting file: '" + ze.name + "'...")
            return zis
        }
        return null
    }

    fun convertStreamToString(`is`: InputStream): String {
        return Scanner(`is`).useDelimiter("\\A").next()
    }

}

