package com.example.data.repository

import com.example.data.db.CallLogDao
import com.example.data.model.CallLogEntity
import kotlinx.coroutines.flow.Flow

class CallLogRepository(private val dao: CallLogDao) {
    val allLogs: Flow<List<CallLogEntity>> = dao.getAllLogs()
    val totalCount: Flow<Int> = dao.getTotalCallsCount()
    val savedCount: Flow<Int> = dao.getSavedCount()
    val existingCount: Flow<Int> = dao.getExistingCount()

    fun getLogsByStatus(status: String): Flow<List<CallLogEntity>> = dao.getLogsByStatus(status)

    fun searchLogs(query: String): Flow<List<CallLogEntity>> = dao.searchLogs(query)

    suspend fun insertLog(log: CallLogEntity): Long = dao.insertLog(log)

    suspend fun deleteLog(id: Long) = dao.deleteLogById(id)

    suspend fun clearAll() = dao.clearAllLogs()

    suspend fun updateContactName(id: Long, newName: String) = dao.updateContactName(id, newName)
}
