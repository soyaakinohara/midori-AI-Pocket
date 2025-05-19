package com.soyaakinohara.aipocket

import android.app.Application
import android.content.Context // Context を SharedPreferences で使うためにインポート
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// SharedPreferences を使うためにインポート (androidx.preference を推奨)
// もし androidx.preference を使わない場合は android.preference.PreferenceManager でも可
import com.soyaakinohara.aipocket.data.db.AppDatabase
import com.soyaakinohara.aipocket.data.db.CharacterEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val characterDao = AppDatabase.getDatabase(application).characterDao()
    // SharedPreferences のインスタンスを取得 (Applicationコンテキストを使用)
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)

    companion object {
        // SharedPreferences で初回起動判定に使うキー
        private const val PREF_KEY_FIRST_LAUNCH_COMPLETED = "is_first_launch_completed_v1" // キー名を少し具体的に
    }

    // ViewModelが初期化されるときに初回起動チェックとキャラクターセットアップを実行
    init {
        checkAndSetupPreinstalledCharacters()
    }

    private fun checkAndSetupPreinstalledCharacters() {
        val isSetupCompleted = sharedPreferences.getBoolean(PREF_KEY_FIRST_LAUNCH_COMPLETED, false)
        if (!isSetupCompleted) {
            viewModelScope.launch {
                // --- プリインストールキャラクターの定義 ---
                val midoriChan = CharacterEntity(
                    // id は自動生成なので 0 のままでOK
                    name = "秋ノ原　緑",
                    iconUri = "android.resource://com.soyaakinohara.aipocket/drawable/midori", // 例: "android.resource://com.soyaakinohara.aipocket/drawable/midori_icon" (もしdrawableにあれば)
                    age = "13歳",
                    tone = "落ち着いた文体で、語尾は「〜だね」「〜なのかもしれない」などをよく使います。一人称は「私」を使います。自らの感情を表すときは素直に子供っぽく表現します。強い感情が出るときは「うわーん」「やだやだ！」など、年相応に崩れることがあります。",
                    personality = "大人びた口調で話すダウナー系の少女です。落ち着いた性格をしています",
                    worldview = "殺伐とした終末世界に生きている。",
                    notes = "二人称は基本的に「あなた」または「君」。できるだけAIらしくない文体で話してキャラクターに人間臭さを持たせてください。生成する一文が長くなりすぎないようにすること。長くても140文字以内。",
                    isUserCreated = false, // ユーザーが作成したものではない
                    isPreinstalled = true  // プリインストールキャラクターである
                )

                // 必要に応じて他のプリインストールキャラクターも追加
                // val anotherChar = CharacterEntity(...)

                // データベースに挿入
                characterDao.insertCharacter(midoriChan)
                // characterDao.insertCharacter(anotherChar)

                // セットアップ完了のフラグを保存
                sharedPreferences.edit().putBoolean(PREF_KEY_FIRST_LAUNCH_COMPLETED, true).apply()
                println("Preinstalled characters have been set up.")
            }
        } else {
            println("Preinstalled characters setup is already completed.")
        }
    }

    // データベースから全てのキャラクターリストを取得し、StateFlowとして公開
    val characters: StateFlow<List<CharacterEntity>> = characterDao.getAllCharacters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // キャラクターを削除する関数 (IDを指定)
    // ★ プリインストールキャラクターは削除しないように修正
    fun deleteCharacter(characterId: Int) {
        viewModelScope.launch {
            val characterToDelete = characterDao.getCharacterById(characterId)
            if (characterToDelete != null) {
                if (!characterToDelete.isPreinstalled) { // プリインストールされていなければ削除
                    characterDao.deleteCharacterById(characterId)
                    println("Character ${characterToDelete.name} (ID: $characterId) deleted.")
                } else {
                    println("Cannot delete preinstalled character: ${characterToDelete.name} (ID: $characterId)")
                    // TODO: UIに「このキャラクターは削除できません」と通知する (Snackbarなど)
                }
            } else {
                println("Character with ID: $characterId not found for deletion.")
            }
        }
    }

    // キャラクターを削除する関数 (エンティティを指定) - こちらも修正
    // 基本的にはID指定のdeleteCharacterを使うことが多いが、念のためこちらも対応
    fun deleteCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            if (!character.isPreinstalled) { // プリインストールされていなければ削除
                characterDao.deleteCharacter(character)
                println("Character ${character.name} (ID: ${character.id}) deleted.")
            } else {
                println("Cannot delete preinstalled character: ${character.name} (ID: ${character.id})")
                // TODO: UIに「このキャラクターは削除できません」と通知する
            }
        }
    }
}