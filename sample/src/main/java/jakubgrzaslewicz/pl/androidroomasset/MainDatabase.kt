/*
 * Copyright (c) 2018.
 * Created by Ibrahim Eid (https://github.com/humazed)
 * Modified by Jakub Grząślewicz (https://github.com/jakubgrzaslewicz)
 *
 */

package jakubgrzaslewicz.pl.androidroomasset

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import jakubgrzaslewicz.pl.androidroomasset.DAOs.TestDao
import jakubgrzaslewicz.pl.androidroomasset.Entities.Test
import jakubgrzaslewicz.pl.roomassetreadonly.RoomAssetRO

const val VERSION = 7

@Database(
        version = VERSION,
        entities = [
            Test::class
        ],
        exportSchema = true
)
public abstract class MainDatabase : RoomDatabase() {
    abstract fun Test(): TestDao

    companion object {
        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getInstance(context: Context): MainDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                RoomAssetRO.databaseBuilder(context.applicationContext, MainDatabase::class.java, "MainDatabase.sqlite", VERSION)
                        .allowMainThreadQueries()
                        .build()
    }
}