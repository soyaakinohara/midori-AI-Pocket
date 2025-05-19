package com.soyaakinohara.aipocket.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // 競合が発生したら置き換える
    suspend fun insertCharacter(character: CharacterEntity): Long // 挿入された行のIDを返す

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?

    @Query("SELECT * FROM characters ORDER BY id ASC") // IDの昇順で取得
    fun getAllCharacters(): Flow<List<CharacterEntity>> // Flowで変更を監視可能

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacterById(id: Int)
}