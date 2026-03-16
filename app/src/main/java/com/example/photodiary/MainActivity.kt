package com.example.photodiary

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.EventNote
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
                    var writeOriginScreen by remember { mutableStateOf(AppScreen.Main) }
                    var calendarSelectedDateMillis by remember {
                        mutableLongStateOf(System.currentTimeMillis().toDayStartMillis())
                    }
                    var calendarWriteDateMillis by remember { mutableStateOf<Long?>(null) }
                    val diaryEntries = remember { mutableStateListOf<DiaryEntry>() }
                    val scope = rememberCoroutineScope()
                    val dao = remember { DiaryDatabase.getInstance(applicationContext).diaryDao() }

                    LaunchedEffect(Unit) {
                        diaryEntries.replaceFromDatabase(dao)
                    }

                    when (currentScreen) {
                        AppScreen.Main -> MainScreen(
                            entries = diaryEntries.toList(),
                            onCalendarClick = {
                                calendarSelectedDateMillis = System.currentTimeMillis().toDayStartMillis()
                                calendarWriteDateMillis = null
                                currentScreen = AppScreen.Calendar
                            },
                            onMyPageClick = { currentScreen = AppScreen.MyPage },
                            onWriteClick = {
                                selectedEntryId = null
                                writeOriginScreen = AppScreen.Main
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
                            onAddClick = { selectedDateMillis ->
                                selectedEntryId = null
                                writeOriginScreen = AppScreen.Calendar
                                calendarSelectedDateMillis = selectedDateMillis.toDayStartMillis()
                                calendarWriteDateMillis = selectedDateMillis.toDayStartMillis()
                                currentScreen = AppScreen.Write
                            },
                            onEntryClick = { entryId ->
                                selectedEntryId = entryId
                                detailBackScreen = AppScreen.Calendar
                                currentScreen = AppScreen.Detail
                            }
                        )

                        AppScreen.Write -> {
                            val editingEntry = diaryEntries.firstOrNull { it.id == selectedEntryId }
                            val writeInitialDiaryDate = editingEntry?.diaryDate
                                ?: calendarWriteDateMillis
                                ?: if (writeOriginScreen == AppScreen.Calendar) calendarSelectedDateMillis else null
                            WriteScreen(
                                onBackClick = {
                                    if (editingEntry == null && writeOriginScreen == AppScreen.Calendar) {
                                        calendarWriteDateMillis?.let { calendarSelectedDateMillis = it }
                                        currentScreen = AppScreen.Calendar
                                        calendarWriteDateMillis = null
                                    } else {
                                        currentScreen = if (editingEntry == null) writeOriginScreen else AppScreen.Detail
                                    }
                                },
                                initialDiaryDate = writeInitialDiaryDate,
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
                                                val previousImagePaths = editingEntry.imagePath.toImagePathList()
                                                val removedImagePaths = previousImagePaths - imagePaths.toSet()

                                                dao.update(
                                                    editingEntry.copy(
                                                        diaryDate = diaryDate.toDayStartMillis(),
                                                        title = title,
                                                        content = content,
                                                        imagePath = imagePaths.toImagePathPayload(),
                                                        updatedAt = now
                                                    ).toEntity()
                                                )

                                                removedImagePaths.deleteInternalImageCopies(applicationContext)
                                            }
                                        }
                                        diaryEntries.replaceFromDatabase(dao)
                                        if (editingEntry == null && writeOriginScreen == AppScreen.Calendar) {
                                            calendarSelectedDateMillis = diaryDate.toDayStartMillis()
                                            currentScreen = AppScreen.Calendar
                                            calendarWriteDateMillis = null
                                        } else {
                                            currentScreen = if (editingEntry == null) writeOriginScreen else AppScreen.Detail
                                        }
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
                                    onEditClick = {
                                        writeOriginScreen = AppScreen.Detail
                                        calendarWriteDateMillis = null
                                        currentScreen = AppScreen.Write
                                    },
                                    onDeleteClick = {
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                dao.delete(selectedEntry.toEntity())
                                                selectedEntry.imagePath
                                                    .toImagePathList()
                                                    .deleteInternalImageCopies(applicationContext)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EventNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "아직 작성된 다이어리가 없습니다.",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "Write 버튼으로 첫 기록을 남겨보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(9.dp),
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    imagePaths.forEach { imagePath ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
                                        ) {
                                            AsyncImage(
                                                model = imagePath,
                                                contentDescription = "첨부 이미지",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    repeat(5 - imagePaths.size) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
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
        tonalElevation = 3.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 14.dp, top = 6.dp, end = 14.dp, bottom = 8.dp)
                    .height(68.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BottomNavigationTab(
                        text = "Calendar",
                        icon = Icons.Filled.CalendarToday,
                        selected = false,
                        modifier = Modifier.weight(1f),
                        onClick = onCalendarClick
                    )

                    Spacer(modifier = Modifier.width(66.dp))

                    BottomNavigationTab(
                        text = "MyPage",
                        icon = Icons.Filled.Person,
                        selected = false,
                        modifier = Modifier.weight(1f),
                        onClick = onMyPageClick
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-10).dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    IconButton(
                        onClick = onWriteClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "새 일기 작성",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationTab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = contentColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}


private fun List<String>.deleteInternalImageCopies(context: Context) {
    forEach { path ->
        path.toInternalImageFileOrNull(context)?.let { file ->
            if (file.exists()) {
                file.delete()
            }
        }
    }
}

private fun String.toInternalImageFileOrNull(context: Context): File? {
    val parsed = runCatching { Uri.parse(this) }.getOrNull() ?: return null
    val candidate = when {
        parsed.scheme == "file" -> parsed.path?.let(::File)
        startsWith(context.filesDir.absolutePath) -> File(this)
        else -> null
    } ?: return null

    val basePath = runCatching { context.filesDir.canonicalPath }.getOrNull() ?: return null
    val candidatePath = runCatching { candidate.canonicalPath }.getOrNull() ?: return null
    return if (candidatePath.startsWith(basePath)) candidate else null
}

private suspend fun MutableList<DiaryEntry>.replaceFromDatabase(dao: DiaryDao) {
    val loaded = withContext(Dispatchers.IO) {
        dao.getAllOrdered().map { it.toUiModel() }
    }
    clear()
    addAll(loaded)
}
