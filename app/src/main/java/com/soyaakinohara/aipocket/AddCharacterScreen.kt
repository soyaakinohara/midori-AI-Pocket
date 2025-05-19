package com.soyaakinohara.aipocket // あなたのパッケージ名に合わせてください

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter // Coilライブラリを使って画像を表示
// AddCharacterViewModel の import を忘れずに
// import com.soyaakinohara.aipocket.AddCharacterViewModel (もし別パッケージならパスを修正)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCharacterScreen(
    navController: NavHostController,
    addCharacterViewModel: AddCharacterViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("") }
    var personality by remember { mutableStateOf("") }
    var worldview by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // 画像選択のためのランチャー
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("新しいキャラクターを追加") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (name.isNotBlank()) { // 最低限名前は入力されているかチェック
                    addCharacterViewModel.addCharacter(
                        name = name,
                        iconUri = selectedImageUri,
                        age = age,
                        tone = tone,
                        personality = personality,
                        worldview = worldview,
                        notes = notes
                    )
                    android.widget.Toast.makeText(context, "$name を追加しました", android.widget.Toast.LENGTH_SHORT).show()
                    navController.popBackStack() // 保存後、前の画面に戻る
                } else {
                    android.widget.Toast.makeText(context, "キャラクター名を入力してください", android.widget.Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = "キャラクターを保存") // アイコンは適当、後で変更可
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffoldからのpaddingを適用
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // 長いフォームに対応するためスクロール可能に
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // アイコン選択部分
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                        contentDescription = "選択されたアイコン",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.AddAPhoto,
                        contentDescription = "アイコンを追加",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名前 *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("年齢") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tone, onValueChange = { tone = it }, label = { Text("口調") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = personality, onValueChange = { personality = it }, label = { Text("性格") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = worldview, onValueChange = { worldview = it }, label = { Text("世界観") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("その他注意事項 (文の長さ指定など)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        }
    }
}