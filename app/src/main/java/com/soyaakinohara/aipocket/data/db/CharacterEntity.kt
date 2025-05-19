package com.soyaakinohara.aipocket.data.db // あなたのパッケージ名に合わせてください

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters") // テーブル名を指定
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) // 主キー、自動生成
    val id: Int = 0,
    val name: String,
    val iconUri: String?, // アイコン画像のURI (null許容)
    val age: String,
    val tone: String, // 口調
    val personality: String, // 性格
    val worldview: String, // 世界観
    val notes: String, // その他注意事項
    val isUserCreated: Boolean, // ユーザーが作成したキャラか (デフォルトキャラとの区別
    val isPreinstalled: Boolean = false // ★ プリインストールキャラかどうかを示すフラグ (デフォルトはfalse)
)