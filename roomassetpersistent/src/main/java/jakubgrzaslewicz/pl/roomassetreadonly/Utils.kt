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


}

