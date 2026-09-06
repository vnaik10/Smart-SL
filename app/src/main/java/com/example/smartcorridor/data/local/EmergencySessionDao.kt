package com.example.smartcorridor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencySessionDao {

    @Query("SELECT * FROM emergency_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<EmergencySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: EmergencySessionEntity)

    @Query("SELECT * FROM emergency_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): EmergencySessionEntity?

    @Query("DELETE FROM emergency_sessions")
    suspend fun clearAll()
}
