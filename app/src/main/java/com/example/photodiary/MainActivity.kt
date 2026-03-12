package com.example.photodiary

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AppScreen {
    Main,
    Write,
    Detail,
    Calendar,
    MyPage
}

data class DiaryEntry(
    val id: Long,
    val diaryDate: Long,
    val title: String,
    val content: String,
    val imagePath: String?,
    val createdAt: Long,
    val updatedAt: Long
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        setContent {
            PhotoDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.Main) }
                    var selectedEntryId by remember { mutableStateOf<Long?>(null) }
                    var detailBackScreen by remember { mutableStateOf(AppScreen.Main) }
                    var calendarSelectedDateMillis by remember {
                        mutableLongStateOf(System.currentTimeMillis().toDayStartMillis())
                    }
                    val diaryEntries = remember { mutableStateListOf<DiaryEntry>() }
                    val scope = rememberCoroutineScope()
                    val dao = remember { DiaryDatabase.getInstance(applicationContext).diaryDao() }

                    LaunchedEffect(Unit) {
                        diaryEntries.replaceFromDatabase(dao)
                    }

                    when (currentScreen) {
                        AppScreen.Main -> MainScreen(
                            entries = diaryEntries.toList(),
                            onCalendarClick = { currentScreen = AppScreen.Calendar },
                            onMyPageClick = { currentScreen = AppScreen.MyPage },
                            onWriteClick = {
                                selectedEntryId = null
                                currentScreen = AppScreen.Write
                            },
                            onEntryClick = { entryId ->
                                selectedEntryId = entryId
                                detailBackScreen = AppScreen.Main
                                currentScreen = AppScreen.Detail
                            }
                        )

                        AppScreen.Calendar -> CalendarScreen(
                            entries = diaryEntries.toList(),
                            initialSelectedDateMillis = calendarSelectedDateMillis,
                            onSelectedDateChange = { calendarSelectedDateMillis = it },
                            onBackClick = { currentScreen = AppScreen.Main },
                            onEntryClick = { entryId ->
                                selectedEntryId = entryId
                                detailBackScreen = AppScreen.Calendar
                                currentScreen = AppScreen.Detail
                            }
                        )

                        AppScreen.Write -> {
                            val editingEntry = diaryEntries.firstOrNull { it.id == selectedEntryId }
                            WriteScreen(
                                onBackClick = {
                                    currentScreen = if (editingEntry == null) AppScreen.Main else AppScreen.Detail
                                },
                                initialDiaryDate = editingEntry?.diaryDate,
                                initialTitle = editingEntry?.title.orEmpty(),
                                initialContent = editingEntry?.content.orEmpty(),
                                initialImagePaths = editingEntry?.imagePath.toImagePathList(),
                                onSaveClick = { diaryDate, title, content, imagePaths ->
                                    val now = System.currentTimeMillis()
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            if (editingEntry == null) {
                                                dao.insert(
                                                    DiaryEntity(
                                                        diaryDate = diaryDate.toDayStartMillis(),
                                                        title = title,
                                                        content = content,
                                                        imagePath = imagePaths.toImagePathPayload(),
                                                        createdAt = now,
                                                        updatedAt = now
                                                    )
                                                )
                                            } else {
                                                dao.update(
                                                    editingEntry.copy(
                                                        diaryDate = diaryDate.toDayStartMillis(),
                                                        title = title,
                                                        content = content,
                                                        imagePath = imagePaths.toImagePathPayload(),
                                                        updatedAt = now
                                                    ).toEntity()
                                                )
                                            }
                                        }
                                        diaryEntries.replaceFromDatabase(dao)
                                        currentScreen = if (editingEntry == null) AppScreen.Main else AppScreen.Detail
                                    }
                                }
                            )
                        }

                        AppScreen.Detail -> {
                            val selectedEntry = diaryEntries.firstOrNull { it.id == selectedEntryId }
                            if (selectedEntry == null) {
                                currentScreen = AppScreen.Main
                            } else {
                                DetailScreen(
                                    entry = selectedEntry,
                                    onBackClick = { currentScreen = detailBackScreen },
                                    onEditClick = { currentScreen = AppScreen.Write },
                                    onDeleteClick = {
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                dao.delete(selectedEntry.toEntity())
                                            }
                                            diaryEntries.replaceFromDatabase(dao)
                                            selectedEntryId = null
                                            currentScreen = AppScreen.Main
                                        }
                                    }
                                )
                            }
                        }

                        AppScreen.MyPage -> MyPageScreen(
                            diaryCount = diaryEntries.size,
                            onBackClick = { currentScreen = AppScreen.Main }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    entries: List<DiaryEntry>,
    onCalendarClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onWriteClick: () -> Unit,
    onEntryClick: (Long) -> Unit
) {
    val context = LocalContext.current
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressedAt <= 2_000L) {
            (context as? ComponentActivity)?.finish()
        } else {
            lastBackPressedAt = now
            Toast.makeText(
                context,
                "뒤로가기 버튼을 한번 더 누르시면 앱이 종료됩니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val sortedEntries = remember(entries) {
        entries.sortedWith(
            compareByDescending<DiaryEntry> { it.diaryDate }
                .thenByDescending { it.createdAt }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "Photo Diary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            BottomButtonBar(
                onCalendarClick = onCalendarClick,
                onWriteClick = onWriteClick,
                onMyPageClick = onMyPageClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DiaryListSection(
                entries = sortedEntries,
                onEntryClick = onEntryClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 8.dp, end = 12.dp)
            )
        }
    }
}

@Composable
private fun DiaryListSection(
    entries: List<DiaryEntry>,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (entries.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "아직 작성된 다이어리가 없습니다.",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Write 버튼으로 첫 기록을 남겨보세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val imagePaths = entry.imagePath.toImagePathList().take(5)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEntryClick(entry.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = entry.diaryDate.toDisplayDate(),
                                style = MaterialTheme.typography.labelSmall
                                    .copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = entry.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (imagePaths.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    imagePaths.forEach { imagePath ->
                                        AsyncImage(
                                            model = imagePath,
                                            contentDescription = "첨부 이미지",
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    }
                                    repeat(5 - imagePaths.size) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomButtonBar(
    onCalendarClick: () -> Unit,
    onWriteClick: () -> Unit,
    onMyPageClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text(text = "Calendar")
            }

            FilledTonalButton(
                onClick = onWriteClick,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text(
                    text = "Write",
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onMyPageClick,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text(text = "MyPage")
            }
        }
    }
}

private suspend fun MutableList<DiaryEntry>.replaceFromDatabase(dao: DiaryDao) {
    val loaded = withContext(Dispatchers.IO) {
        dao.getAllOrdered().map { it.toUiModel() }
    }
    clear()
    addAll(loaded)
}
