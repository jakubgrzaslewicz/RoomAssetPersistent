package jakubgrzaslewicz.pl.roomassetreadonly

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

public class RoomAssetRO {
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
            RoomAssetPersistent(context, dbName, storageLocation, factory).writableDatabase.close()
            Log.w(TAG, "RoomAssetRO is ready ")
        }
    }
}