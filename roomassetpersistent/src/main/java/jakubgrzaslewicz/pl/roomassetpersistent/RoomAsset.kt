package jakubgrzaslewicz.pl.roomassetpersistent

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

public class RoomAsset {
    companion object {

        @JvmStatic
        @JvmOverloads
        fun <T : RoomDatabase> databaseBuilder(
                context: Context,
                dbClass: Class<T>,
                dbName: String,
                storageLocation: String? = null,
                factory: SQLiteDatabase.CursorFactory? = null
        ): RoomDatabase.Builder<T> {
            initializeDatabase(context, dbClass, dbName, storageLocation, factory)

            return Room.databaseBuilder(context, dbClass, dbName)
        }

        private fun <T> initializeDatabase(context: Context, dbClass: Class<T>, dbName: String, storageLocation: String?, factory: SQLiteDatabase.CursorFactory?) {
            roomassetpersistent(context, dbName, storageLocation, factory).writableDatabase.close()
            Log.w(TAG, "RoomAsset is ready ")
        }
    }
}