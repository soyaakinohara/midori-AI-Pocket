package com.soyaakinohara.aipocket

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soyaakinohara.aipocket.data.db.AppDatabase
import com.soyaakinohara.aipocket.data.db.CharacterEntity
import kotlinx.coroutines.launch

class AddCharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val characterDao = AppDatabase.getDatabase(application).characterDao()

    // キャラクターをデータベースに保存する関数
    fun addCharacter(
        name: String,
        iconUri: Uri?, // 選択された画像のURI
        age: String,
        tone: String,
        personality: String,
        worldview: String,
        notes: String
    ) {
        viewModelScope.launch {
            val newCharacter = CharacterEntity(
                name = name,
                iconUri = iconUri?.toString(), // UriをStringに変換して保存
                age = age,
                tone = tone,
                personality = personality,
                worldview = worldview,
                notes = notes,
                isUserCreated = true // ユーザーが作成したキャラクター
            )
            characterDao.insertCharacter(newCharacter)
        }
    }
}