package com.example.inzynierkaallegroolx.data.listings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings")
    suspend fun all(): List<ListingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<ListingEntity>)
}
