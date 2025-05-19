package com.soyaakinohara.aipocket

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.soyaakinohara.aipocket.data.db.CharacterEntity
import com.soyaakinohara.aipocket.data.db.ChatHistoryEntity
// ViewModelのimport (同じパッケージにあれば不要な場合もあるが、明示的に記述)
import com.soyaakinohara.aipocket.ui.theme.AI緑ちゃんポケットTheme
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {
    // ★★★ MainActivityのonCreateの引数からhomeViewModelを削除 ★★★
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AI緑ちゃんポケットTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home_screen") {
                        composable(route = "home_screen") {
                            HomeScreen(navController = navController) // ★ homeViewModelはHomeScreen内で取得
                        }
                        composable(route = "settings_screen") {
                            SettingsScreen(navController = navController)
                        }
                        composable(route = "chat_screen/{characterId}") { // ★ NavBackStackEntryを受け取るように修正
                            // val characterId = it.arguments?.getString("characterId") // これはViewModelがSavedStateHandleで取得
                            ChatScreen(navController = navController)
                        }
                        composable(route = "add_character_screen") {
                            AddCharacterScreen(navController = navController)
                        }
                        composable(route = "readme_screen") {
                            ReadmeScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavHostController, homeViewModel: HomeViewModel = viewModel()) { // ★ NavHostController型に修正
    val characters by homeViewModel.characters.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var characterToDelete by remember { mutableStateOf<CharacterEntity?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("キャラクターを選択") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "設定")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_character_screen") }) {
                Icon(Icons.Filled.Add, contentDescription = "新しいキャラクターを追加")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (characters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("キャラクターがいません。\n右下のボタンから追加してください。")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(characters, key = { it.id }) { character ->
                        CharacterItem(
                            character = character,
                            onClick = {
                                navController.navigate("chat_screen/${character.id}")
                            },
                            onLongClick = {
                                if (!character.isPreinstalled) {
                                    characterToDelete = character
                                    showDialog = true
                                } else {
                                    Toast.makeText(context, "${character.name} は削除できません", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDialog && characterToDelete != null) {
            characterToDelete?.let { charToDelete ->
                if (!charToDelete.isPreinstalled) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            characterToDelete = null
                        },
                        title = { Text("キャラクター削除") },
                        text = { Text("${charToDelete.name} を削除しますか？\nこの操作は元に戻せません。") },
                        confirmButton = {
                            TextButton(onClick = {
                                homeViewModel.deleteCharacter(charToDelete.id)
                                showDialog = false
                                characterToDelete = null
                            }) {
                                Text("削除", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDialog = false
                                characterToDelete = null
                            }) {
                                Text("キャンセル")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterItem(character: CharacterEntity, onLongClick: () -> Unit, onClick: () -> Unit) { // ★ onLongClickをnull非許容に (呼び出し元に合わせる)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (character.iconUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(character.iconUri)),
                        contentDescription = "${character.name} のアイコン",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "デフォルトアイコン",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Text(text = character.name, style = MaterialTheme.typography.titleMedium)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentApiKey by settingsViewModel.apiKey.collectAsState()
    var inputApiKey by remember(currentApiKey) { mutableStateOf(currentApiKey ?: "") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("設定画面", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = inputApiKey,
            onValueChange = { inputApiKey = it },
            label = { Text("Gemini APIキー") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                settingsViewModel.saveApiKey(inputApiKey)
                Toast.makeText(context, "APIキーを保存しました", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("APIキーを保存")
        }
        Button(
            onClick = { navController.navigate("readme_screen") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Readmeを読む")
        }
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("戻る")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(navController: NavHostController, /* ★★★ it: String パラメータを削除 ★★★ */ chatViewModel: ChatViewModel = viewModel()) {
    val currentCharacter by chatViewModel.currentCharacter.collectAsState()
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    var inputMessage by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val isSendingMessage by chatViewModel.isSendingMessage.collectAsState()

    val searchQuery by chatViewModel.searchQuery.collectAsState()
    var searchMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchMode) {
        if (searchMode) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Scaffold(
        topBar = {
            if (searchMode) {
                SearchAppBar(
                    query = searchQuery,
                    onQueryChange = { chatViewModel.onSearchQueryChange(it) },
                    onCloseSearch = {
                        searchMode = false
                        chatViewModel.onSearchQueryChange("")
                    },
                    focusRequester = focusRequester
                )
            } else {
                DefaultChatAppBar(
                    characterName = currentCharacter?.name ?: "チャット",
                    characterIconUri = currentCharacter?.iconUri?.let { Uri.parse(it) },
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = {
                        searchMode = true
                    }
                )
            }
        },
        bottomBar = {
            ChatInputRow(
                value = inputMessage,
                onValueChange = { newValue ->
                    if (!isSendingMessage) {
                        inputMessage = newValue
                    }
                },
                onSendClick = {
                    if (inputMessage.text.isNotBlank() && !isSendingMessage) {
                        chatViewModel.sendMessage(inputMessage.text) // ★ chatViewModelインスタンスを使用
                        inputMessage = TextFieldValue("")
                        keyboardController?.hide()
                    }
                },
                isSending = isSendingMessage
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(chatHistory, key = { it.id }) { message ->
                val displayMessage = if (searchQuery.isNotBlank() && message.message.contains(searchQuery, ignoreCase = true)) {
                    message.copy(message = "🔎 ${message.message}")
                } else {
                    message
                }
                ChatMessageItem(message = displayMessage, characterName = currentCharacter?.name ?: "")
            }
        }
    }
}


@Composable
fun ChatInputRow( // ★★★ 関数の引数の型を修正 ★★★
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("メッセージを入力...") },
            shape = RoundedCornerShape(24.dp),
            enabled = !isSending
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onSendClick,
                enabled = !isSending
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "送信",
                    tint = if (isSending) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary
                )
            }
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultChatAppBar( // ★★★ 関数の引数の型を修正 ★★★
    characterName: String,
    characterIconUri: Uri?,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (characterIconUri != null) {
                        Image(painter = rememberAsyncImagePainter(model = characterIconUri), contentDescription = "$characterName のアイコン", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = "デフォルトアイコン", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(characterName)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "検索")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar( // ★★★ 関数の引数の型を修正 ★★★
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    focusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                placeholder = { Text("会話を検索...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* TODO */ }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Filled.Close, contentDescription = "検索を閉じる")
            }
        }
    )
}

@Composable
fun ChatMessageItem(message: ChatHistoryEntity, characterName: String) { // ★★★ 関数の引数の順序を修正 ★★★
    val columnHorizontalAlignment = if (message.isUserMessage) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isUserMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isUserMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val horizontalPadding = if (message.isUserMessage) PaddingValues(start = 48.dp) else PaddingValues(end = 48.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontalPadding),
        horizontalAlignment = columnHorizontalAlignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isUserMessage) 16.dp else 4.dp,
                        topEnd = if (message.isUserMessage) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.message,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadmeScreen(navController: NavHostController) {
    val context = LocalContext.current
    var readmeContent by remember { mutableStateOf("読み込み中...") }

    LaunchedEffect(Unit) {
        try {
            val inputStream = context.assets.open("readme.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            readmeContent = reader.readText()
            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            readmeContent = "Readmeファイルの読み込みに失敗しました。\n${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Readme") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = readmeContent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavHostController.AddCharacterScreen(
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
            TopAppBar(
                title = { Text("新しいキャラクターを追加") },
                navigationIcon = {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (name.isNotBlank()) {
                    addCharacterViewModel.addCharacter(
                        name = name,
                        iconUri = selectedImageUri,
                        age = age,
                        tone = tone,
                        personality = personality,
                        worldview = worldview,
                        notes = notes
                    )
                    Toast.makeText(context, "$name を追加しました", Toast.LENGTH_SHORT).show()
                    popBackStack()
                } else {
                    Toast.makeText(context, "キャラクター名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.Save, contentDescription = "キャラクターを保存")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AI緑ちゃんポケットTheme {
        // Preview content
    }
}