package jakubgrzaslewicz.pl.roomassetpersistent


import android.util.Log
import java.io.*
import java.nio.file.Path


import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

internal object Utils {

    private val TAG = RoomAssetPersistent.TAG

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

    fun ExtractZip(stream: InputStream, absolutePath: String?) {
        val inputStream = ZipInputStream(BufferedInputStream(stream))
        var entry = inputStream.nextEntry
        while (entry != null) {
            val f = File("$absolutePath/${entry.name}")
            if (f.exists()) f.delete()
            inputStream.toFile("$absolutePath/${entry.name}")
            inputStream.closeEntry()
            entry = inputStream.nextEntry
        }
        inputStream.close()
    }

    fun InputStream.toFile(path: String) {
        File(path).outputStream().use { this.copyTo(it) }
    }

}

