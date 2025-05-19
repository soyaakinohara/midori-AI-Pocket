package com.soyaakinohara.aipocket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.soyaakinohara.aipocket.data.db.AppDatabase
import com.soyaakinohara.aipocket.data.db.CharacterEntity
import com.soyaakinohara.aipocket.data.db.ChatHistoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce

class ChatViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val characterDao = AppDatabase.getDatabase(application).characterDao()
    private val chatHistoryDao = AppDatabase.getDatabase(application).chatHistoryDao()
    private val apiKeyManager = ApiKeyManager(application)

    // --- 状態管理用のStateFlow ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    // モデル初期化状態 (APIキーが有効で、モデルのインスタンス化に成功したか)
    // 初期値は false にして、APIキー処理後に明示的に true/false を設定
    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady.asStateFlow()

    // --- データ取得と処理のFlow ---
    val characterId: StateFlow<Int?> = savedStateHandle.getStateFlow("characterId", null as String?)
        .map { idString ->
            idString?.toIntOrNull()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentCharacter: StateFlow<CharacterEntity?> = characterId.flatMapLatest { id ->
        if (id != null) {
            flow<CharacterEntity?> { // 型パラメータを明示
                val character: CharacterEntity? = characterDao.getCharacterById(id)
                emit(character)
            }
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val chatHistory: StateFlow<List<ChatHistoryEntity>> = characterId.flatMapLatest { charId ->
        if (charId != null) {
            searchQuery.debounce(300).flatMapLatest { query ->
                if (query.isBlank()) {
                    chatHistoryDao.getChatHistory(charId, 20)
                } else {
                    chatHistoryDao.getChatHistory(charId, 200).map { historyList ->
                        historyList.filter { it.message.contains(query, ignoreCase = true) }
                    }
                }
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var generativeModel: GenerativeModel? = null

    // APIキーを監視し、変更があればモデルを再初期化
    val apiKey: StateFlow<String?> = apiKeyManager.apiKeyFlow
        .onEach { key ->
            if (!key.isNullOrBlank()) {
                try {
                    generativeModel = GenerativeModel(
                        modelName = "gemini-2.0-flash",
                        apiKey = key
                    )
                    _isModelReady.value = true // モデル初期化成功
                    println("Gemini API Model initialized successfully.")
                } catch (e: Exception) {
                    generativeModel = null
                    _isModelReady.value = false // モデル初期化失敗
                    println("Gemini API Model initialization failed: ${e.message}")
                }
            } else {
                generativeModel = null
                _isModelReady.value = false // APIキーが空ならモデル初期化不可
                println("API Key is blank. Gemini API Model not initialized.")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)


    // --- ViewModelの関数 ---
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun sendMessage(messageText: String) {
        val charId = characterId.value ?: return
        val character = currentCharacter.value ?: return

        if (!_isModelReady.value || generativeModel == null) {
            println("Gemini API Model is not ready or not initialized.")
            // TODO: UIにエラーメッセージを表示する
            return
        }
        val currentModel = generativeModel!! // isModelReady が true なら null でないはず

        if (_isSendingMessage.value) return

        viewModelScope.launch {
            _isSendingMessage.value = true
            try {
                val userMessage = ChatHistoryEntity(
                    characterId = charId,
                    isUserMessage = true,
                    message = messageText,
                    timestamp = System.currentTimeMillis()
                )
                chatHistoryDao.insertHistory(userMessage)

                val prompt = buildPrompt(character, chatHistory.value, messageText)

                val response = currentModel.generateContent(prompt)
                val aiResponseText = response.text ?: "（応答がありませんでした）"
                val aiMessage = ChatHistoryEntity(
                    characterId = charId,
                    isUserMessage = false,
                    message = aiResponseText,
                    timestamp = System.currentTimeMillis()
                )
                chatHistoryDao.insertHistory(aiMessage)
                chatHistoryDao.deleteOldHistories(charId, 20)

            } catch (e: Exception) {
                println("Gemini API request failed: ${e.message}")
                val errorMessage = ChatHistoryEntity(
                    characterId = charId,
                    isUserMessage = false,
                    message = "エラーが発生しました: ${e.message}",
                    timestamp = System.currentTimeMillis()
                )
                chatHistoryDao.insertHistory(errorMessage)
                // TODO: UIにエラーメッセージを表示する
            } finally {
                _isSendingMessage.value = false
            }
        }
    }

    private fun buildPrompt(character: CharacterEntity, history: List<ChatHistoryEntity>, newMessage: String): String {
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("あなたはキャラクター「${character.name}」として振る舞ってください。")
        stringBuilder.appendLine("以下の設定に従って応答を生成してください。")
        stringBuilder.appendLine("# キャラクター設定")
        stringBuilder.appendLine("- 年齢: ${character.age}")
        stringBuilder.appendLine("- 口調: ${character.tone}")
        stringBuilder.appendLine("- 性格: ${character.personality}")
        stringBuilder.appendLine("- 世界観: ${character.worldview}")
        if (character.notes.isNotBlank()) {
            stringBuilder.appendLine("- その他注意事項: ${character.notes}")
        }
        stringBuilder.appendLine("\n# 会話履歴 (新しいものが下)")
        history.reversed().forEach {
            if (it.isUserMessage) {
                stringBuilder.appendLine("ユーザー: ${it.message}")
            } else {
                stringBuilder.appendLine("${character.name}: ${it.message}")
            }
        }
        stringBuilder.appendLine("ユーザー: $newMessage")
        stringBuilder.appendLine("${character.name}:")
        return stringBuilder.toString()
    }
}