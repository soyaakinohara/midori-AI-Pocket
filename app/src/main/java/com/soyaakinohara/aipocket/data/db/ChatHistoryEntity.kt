package com.soyaakinohara.aipocket.data.db // あなたのパッケージ名に合わせてください

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_history",
    foreignKeys = [ForeignKey(
        entity = CharacterEntity::class,
        parentColumns = ["id"],
        childColumns = ["characterId"],
        onDelete = ForeignKey.CASCADE // キャラクターが削除されたら、そのキャラクターの会話履歴も削除
    )],
    indices = [Index(value = ["characterId"])] // characterIdでの検索を高速化
)
data class ChatHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val characterId: Int, // どのキャラクターとの会話か (外部キー)
    val isUserMessage: Boolean, // trueならユーザーの発言、falseならAIの発言
    val message: String,
    val timestamp: Long // メッセージのタイムスタンプ (エポックミリ秒など)
)