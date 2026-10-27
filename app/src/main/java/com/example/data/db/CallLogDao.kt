package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE status = :status ORDER BY timestamp DESC")
    fun getLogsByStatus(status: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE phoneNumber LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<CallLogEntity>>

    @Query("SELECT COUNT(*) FROM call_logs")
    fun getTotalCallsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM call_logs WHERE status IN ('SAVED', 'MANUAL_SAVED')")
    fun getSavedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM call_logs WHERE status = 'ALREADY_EXISTS'")
    fun getExistingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CallLogEntity): Long

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllLogs()

    @Query("UPDATE call_logs SET contactName = :newName WHERE id = :id")
    suspend fun updateContactName(id: Long, newName: String)
}
