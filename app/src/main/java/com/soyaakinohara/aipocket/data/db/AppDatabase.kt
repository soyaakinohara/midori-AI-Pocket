package com.soyaakinohara.aipocket.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CharacterEntity::class, ChatHistoryEntity::class], version = 1, exportSchema = false)
// exportSchema = false は、スキーマのバージョン管理情報をエクスポートしない設定 (学習用にはこれでOK)
// 本格的なアプリでは true にしてスキーマのマイグレーションを管理することが推奨されます
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        @Volatile // 他のスレッドからこのインスタンスの変更が即座に見えるようにする
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // 同期ブロックでインスタンス作成
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aipocket_database" // データベースファイル名
                )
                    // .addMigrations(...) // スキーマ変更時のマイグレーション処理 (今回は省略)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}