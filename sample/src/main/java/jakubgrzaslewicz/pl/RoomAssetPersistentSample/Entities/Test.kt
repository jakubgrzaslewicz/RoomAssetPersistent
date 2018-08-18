/*
 * Copyright (c) 2018.
 * Created by Ibrahim Eid (https://github.com/humazed)
 * Modified by Jakub Grząślewicz (https://github.com/jakubgrzaslewicz)
 *
 */

package jakubgrzaslewicz.pl.RoomAssetPersistentSample.Entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "test")
data class Test(
        @ColumnInfo(name = "entryValue")
        var Value: String?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var ID: Int = 0
}