package com.soyaakinohara.aipocket.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Insert
    suspend fun insertHistory(history: ChatHistoryEntity)

    @Query("SELECT * FROM chat_history WHERE characterId = :characterId ORDER BY timestamp ASC LIMIT :limit")
    fun getChatHistory(characterId: Int, limit: Int): Flow<List<ChatHistoryEntity>>

    // 古い履歴を削除するためのメソッド (例: 上限を超えた場合)
    @Query("DELETE FROM chat_history WHERE characterId = :characterId AND id NOT IN (SELECT id FROM chat_history WHERE characterId = :characterId ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun deleteOldHistories(characterId: Int, limit: Int)

    @Query("DELETE FROM chat_history WHERE characterId = :characterId")
    suspend fun deleteAllHistoriesForCharacter(characterId: Int)
}