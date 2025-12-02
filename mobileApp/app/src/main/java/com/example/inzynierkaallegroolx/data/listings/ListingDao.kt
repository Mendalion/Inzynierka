package com.example.inzynierkaallegroolx.data.listings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings")
    suspend fun getAll(): List<ListingEntity>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getById(id: String): ListingEntity?

    @Query("SELECT * FROM listings")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(listings: List<ListingEntity>)

    @Query("DELETE FROM listings")
    suspend fun clearAll()

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteById(id: String)
}