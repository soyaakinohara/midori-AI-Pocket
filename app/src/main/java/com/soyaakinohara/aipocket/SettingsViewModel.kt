package com.soyaakinohara.aipocket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKeyManager = ApiKeyManager(application)

    // DataStoreからAPIキーを読み込み、StateFlowとして公開する
    // これにより、Compose UIはAPIキーの変更をリアクティブに監視できる
    val apiKey: StateFlow<String?> = apiKeyManager.apiKeyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // UIが購読している間だけアクティブ
            initialValue = null // 初期値はnull（まだ読み込まれていないか、未設定）
        )

    // APIキーを保存する関数
    fun saveApiKey(key: String) {
        viewModelScope.launch { // ViewModelのコルーチンスコープで実行
            apiKeyManager.saveApiKey(key)
        }
    }
}