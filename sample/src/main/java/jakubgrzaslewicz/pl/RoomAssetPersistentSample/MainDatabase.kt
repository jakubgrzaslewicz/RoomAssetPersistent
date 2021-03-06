/*
 * Copyright (c) 2018.
 * Created by Ibrahim Eid (https://github.com/humazed)
 * Modified by Jakub Grząślewicz (https://github.com/jakubgrzaslewicz)
 *
 */

package jakubgrzaslewicz.pl.roomassetpersistentSample

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import jakubgrzaslewicz.pl.roomassetpersistentSample.DAOs.TestDao
import jakubgrzaslewicz.pl.roomassetpersistentSample.Entities.Test
import jakubgrzaslewicz.pl.roomassetpersistent.RoomAsset

@Database(
        version = 1,
        entities = [
            Test::class
        ],
        exportSchema = true
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun Test(): TestDao

    companion object {
        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getInstance(context: Context): MainDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                RoomAsset.databaseBuilder(context.applicationContext, MainDatabase::class.java, "MainDatabase.sqlite")
                        .allowMainThreadQueries()
                        .build()
    }
}