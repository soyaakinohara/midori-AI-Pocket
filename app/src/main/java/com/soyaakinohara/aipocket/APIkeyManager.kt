package com.soyaakinohara.aipocket


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Context.dataStore をトップレベルプロパティとして定義
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ApiKeyManager(private val context: Context) {

    companion object {
        // APIキーを保存するためのキー
        val API_KEY = stringPreferencesKey("gemini_api_key")
    }

    // APIキーを保存する関数 (suspend関数なのでコルーチンから呼び出す)
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { settings ->
            settings[API_KEY] = apiKey
        }
    }

    // APIキーを読み込むFlow (変更を監視できる)
    val apiKeyFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }
}
