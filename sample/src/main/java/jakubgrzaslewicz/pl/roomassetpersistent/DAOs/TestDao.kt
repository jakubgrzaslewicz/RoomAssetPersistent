/*
 * Copyright (c) 2018.
 * Created by Ibrahim Eid (https://github.com/humazed)
 * Modified by Jakub Grząślewicz (https://github.com/jakubgrzaslewicz)
 *
 */

package jakubgrzaslewicz.pl.roomassetpersistent.DAOs

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import jakubgrzaslewicz.pl.roomassetpersistent.Entities.Test

@Dao
public interface TestDao {
    @Query("SELECT * FROM test")
    fun getAll(): List<Test>
}