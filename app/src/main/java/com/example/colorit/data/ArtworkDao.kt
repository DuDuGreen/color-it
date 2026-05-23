package com.example.colorit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks ORDER BY timestamp DESC")
    fun getAllArtworksFlow(): Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks ORDER BY timestamp DESC")
    suspend fun getAllArtworks(): List<ArtworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtwork(artwork: ArtworkEntity): Long

    @Delete
    suspend fun deleteArtwork(artwork: ArtworkEntity)

    @Query("DELETE FROM artworks")
    suspend fun clearAllArtworks()
}
