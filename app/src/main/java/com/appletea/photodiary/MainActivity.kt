package com.appletea.photodiary

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateMapOf
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
import java.util.Calendar

private const val PREFS_NAME = "photo_diary_prefs"
private const val PREF_THEME_KEY = "selected_theme"

private enum class AppScreen {
    Main,
    Write,
    Detail,
    Calendar,
    MyPage,
    MonthReflection
}

data class DiaryEntry(
    val id: Long,
    val diaryDate: Long,
    val title: String,
    val content: String,
    val imagePath: String?,
    val mood: String = "",
    val weather: String = "",
    val tag: String = "",
    val sticker: String = "",
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
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val initialTheme = DiaryTheme.fromKey(prefs.getString(PREF_THEME_KEY, DiaryTheme.Cream.key))

        setContent {
            var selectedTheme by remember { mutableStateOf(initialTheme) }
            LaunchedEffect(selectedTheme) {
                prefs.edit().putString(PREF_THEME_KEY, selectedTheme.key).apply()
            }

            PhotoDiaryTheme(theme = selectedTheme) {
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
                    val initCal = remember { Calendar.getInstance() }
                    var calendarViewYear by remember { mutableStateOf(initCal.get(Calendar.YEAR)) }
                    var calendarViewMonth by remember { mutableStateOf(initCal.get(Calendar.MONTH)) }
                    val mainListState = rememberLazyListState()
                    var calendarWriteDateMillis by remember { mutableStateOf<Long?>(null) }
                    var selectedReflectionMonthKey by remember { mutableStateOf<String?>(null) }
                    val diaryEntries = remember { mutableStateListOf<DiaryEntry>() }
                    val monthlyReflections = remember { mutableStateMapOf<String, MonthlyReflectionEntity>() }
                    val scope = rememberCoroutineScope()
                    val database = remember { DiaryDatabase.getInstance(applicationContext) }
                    val dao = remember { database.diaryDao() }
                    val monthlyReflectionDao = remember { database.monthlyReflectionDao() }

                    LaunchedEffect(Unit) {
                        diaryEntries.replaceFromDatabase(dao)
                        monthlyReflections.replaceFromDatabase(monthlyReflectionDao)
                        scope.launch(Dispatchers.IO) {
                            cleanupOrphanedImages(applicationContext, dao, monthlyReflectionDao)
                        }
                    }

                    when (currentScreen) {
                        AppScreen.Main -> MainScreen(
                            entries = diaryEntries.toList(),
                            monthlyReflections = monthlyReflections,
                            onCalendarClick = {
                                calendarSelectedDateMillis = System.currentTimeMillis().toDayStartMillis()
                                val now = Calendar.getInstance()
                                calendarViewYear = now.get(Calendar.YEAR)
                                calendarViewMonth = now.get(Calendar.MONTH)
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
                            },
                            onMonthlyReflectionClick = { monthKey ->
                                selectedReflectionMonthKey = monthKey
                                currentScreen = AppScreen.MonthReflection
                            },
                            listState = mainListState
                        )

                        AppScreen.Calendar -> CalendarScreen(
                            entries = diaryEntries.toList(),
                            initialSelectedDateMillis = calendarSelectedDateMillis,
                            initialViewYear = calendarViewYear,
                            initialViewMonth = calendarViewMonth,
                            onSelectedDateChange = { calendarSelectedDateMillis = it },
                            onViewMonthChange = { year, month ->
                                calendarViewYear = year
                                calendarViewMonth = month
                            },
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
                                        calendarSelectedDateMillis =
                                            calendarWriteDateMillis ?: calendarSelectedDateMillis
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
                                initialMood = editingEntry?.mood.orEmpty(),
                                initialWeather = editingEntry?.weather.orEmpty(),
                                initialTag = editingEntry?.tag.orEmpty(),
                                initialSticker = editingEntry?.sticker.orEmpty(),
                                onSaveClick = { diaryDate, title, content, imagePaths, mood, weather, tag, sticker ->
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
                                                        mood = mood,
                                                        weather = weather,
                                                        tag = tag,
                                                        sticker = sticker,
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
                                                        mood = mood,
                                                        weather = weather,
                                                        tag = tag,
                                                        sticker = sticker,
                                                        updatedAt = now
                                                    ).toEntity()
                                                )

                                                removedImagePaths.forEach { path ->
                                                    monthlyReflectionDao.clearCoverImagePath(path)
                                                }
                                                removedImagePaths.deleteInternalImageCopies(applicationContext)
                                            }
                                        }
                                        diaryEntries.replaceFromDatabase(dao)
                                        if (editingEntry == null && writeOriginScreen == AppScreen.Calendar) {
                                            calendarSelectedDateMillis =
                                                calendarWriteDateMillis ?: diaryDate.toDayStartMillis()
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
                                                val imagePaths = selectedEntry.imagePath.toImagePathList()
                                                imagePaths.forEach { path ->
                                                    monthlyReflectionDao.clearCoverImagePath(path)
                                                }
                                                dao.delete(selectedEntry.toEntity())
                                                imagePaths.deleteInternalImageCopies(applicationContext)
                                            }
                                            diaryEntries.replaceFromDatabase(dao)
                                            selectedEntryId = null
                                            currentScreen = AppScreen.Main
                                        }
                                    }
                                )
                            }
                        }

                        AppScreen.MonthReflection -> {
                            val monthKey = selectedReflectionMonthKey
                            val currentMonthKey = System.currentTimeMillis().toYearMonthKey()
                            if (monthKey.isNullOrBlank() || monthKey >= currentMonthKey) {
                                currentScreen = AppScreen.Main
                            } else {
                                val monthEntries = diaryEntries.filter { it.diaryDate.toYearMonthKey() == monthKey }
                                if (monthEntries.isEmpty()) {
                                    currentScreen = AppScreen.Main
                                } else {
                                    val monthImagePaths = monthEntries
                                        .flatMap { it.imagePath.toImagePathList() }
                                        .distinct()
                                    val storedReflection = monthlyReflections[monthKey]
                                    val initialCoverImagePath = storedReflection?.coverImagePath
                                        ?.takeIf { savedPath ->
                                            savedPath.isNotBlank() &&
                                                (monthImagePaths.isEmpty() || monthImagePaths.contains(savedPath))
                                        }
                                        .orEmpty()
                                    val moodSummary = monthEntries
                                        .map { it.mood }
                                        .topCountKeys()
                                        .mapNotNull { it.toMetaLabelOrNull(moodOptions) }
                                    val weatherSummary = monthEntries
                                        .map { it.weather }
                                        .topCountKeys()
                                        .mapNotNull { it.toMetaLabelOrNull(weatherOptions) }
                                    val tagSummary = monthEntries
                                        .flatMap { entry ->
                                            entry.tag.trim()
                                                .replace("#", " ")
                                                .split(Regex("\\s+"))
                                                .map { it.trim().trim(',') }
                                                .filter { it.isNotBlank() }
                                        }
                                        .topCountKeys()
                                        .map { "#$it" }
                                    MonthlyReflectionScreen(
                                        monthKey = monthKey,
                                        entriesCount = monthEntries.size,
                                        imagePaths = monthImagePaths,
                                        moodSummary = moodSummary,
                                        weatherSummary = weatherSummary,
                                        tagSummary = tagSummary,
                                        initialCoverImagePath = initialCoverImagePath,
                                        initialReflectionText = storedReflection?.reflectionText.orEmpty(),
                                        onBackClick = { currentScreen = AppScreen.Main },
                                        onSaveClick = { coverImagePath, reflectionText ->
                                            scope.launch {
                                                val now = System.currentTimeMillis()
                                                withContext(Dispatchers.IO) {
                                                    val existing = monthlyReflectionDao.getByYearMonth(monthKey)
                                                    monthlyReflectionDao.upsert(
                                                        MonthlyReflectionEntity(
                                                            id = existing?.id ?: 0,
                                                            yearMonth = monthKey,
                                                            coverImagePath = coverImagePath,
                                                            reflectionText = reflectionText,
                                                            createdAt = existing?.createdAt ?: now,
                                                            updatedAt = now
                                                        )
                                                    )
                                                }
                                                monthlyReflections.replaceFromDatabase(monthlyReflectionDao)
                                                currentScreen = AppScreen.Main
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        AppScreen.MyPage -> MyPageScreen(
                            diaryCount = diaryEntries.size,
                            selectedTheme = selectedTheme,
                            onThemeChange = { selectedTheme = it },
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
    monthlyReflections: Map<String, MonthlyReflectionEntity>,
    onCalendarClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onWriteClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onMonthlyReflectionClick: (String) -> Unit,
    listState: LazyListState = rememberLazyListState()
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
                monthlyReflections = monthlyReflections,
                onEntryClick = onEntryClick,
                onMonthlyReflectionClick = onMonthlyReflectionClick,
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, top = 10.dp, end = 18.dp)
            )
        }
    }
}

@Composable
private fun DiaryListSection(
    entries: List<DiaryEntry>,
    monthlyReflections: Map<String, MonthlyReflectionEntity>,
    onEntryClick: (Long) -> Unit,
    onMonthlyReflectionClick: (String) -> Unit,
    listState: LazyListState = rememberLazyListState(),
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
            val currentMonthKey = remember { System.currentTimeMillis().toYearMonthKey() }
            val groupedEntries = remember(entries) {
                entries
                    .groupBy { it.diaryDate.toYearMonthKey() }
                    .map { (yearMonthKey, monthEntries) ->
                        Triple(
                            yearMonthKey.toYearMonthLabel(),
                            monthEntries,
                            yearMonthKey
                        )
                    }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 0.dp, bottom = 28.dp)
            ) {
                groupedEntries.forEach { (monthLabel, monthEntries, monthKey) ->
                    val monthlyReflection = monthlyReflections[monthKey]
                    item(key = "header-$monthKey") {
                        MonthArchiveHeader(
                            monthLabel = monthLabel,
                            entryCount = monthEntries.size,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (monthKey < currentMonthKey) {
                        item(key = "reflection-$monthKey") {
                            MonthlyReflectionPreviewCard(
                                monthLabel = monthLabel,
                                reflection = monthlyReflection,
                                entryCount = monthEntries.size,
                                onClick = { onMonthlyReflectionClick(monthKey) }
                            )
                        }
                    }

                    items(monthEntries, key = { it.id }) { entry ->
                        DiaryArchiveCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArchiveHeader(
    monthLabel: String,
    entryCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "기록 ${entryCount}개",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
        )
    }
}

@Composable
private fun DiaryArchiveCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePaths = remember(entry.imagePath) { entry.imagePath.toImagePathList().take(5) }
    val previewContent = remember(entry.content, entry.imagePath) {
        parseDiaryDocument(entry.content, entry.imagePath.toImagePathList()).toPlainTextPreview()
    }
    val moodLabel = remember(entry.mood) { entry.mood.toMetaLabelOrNull(moodOptions) }
    val weatherLabel = remember(entry.weather) { entry.weather.toMetaLabelOrNull(weatherOptions) }
    val weatherMeta = weatherLabel.toMetaHeaderParts()
    val moodMeta = moodLabel.toMetaHeaderParts()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.5.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.diaryDate.toDisplayDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
                    )
                    if (weatherMeta != null || moodMeta != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (weatherMeta != null) {
                                MetaHeaderSlot(
                                    label = weatherMeta.first ?: " ",
                                    caption = weatherMeta.second,
                                    selected = true,
                                    onClick = null
                                )
                            }
                            if (moodMeta != null) {
                                MetaHeaderSlot(
                                    label = moodMeta.first ?: " ",
                                    caption = moodMeta.second,
                                    selected = true,
                                    onClick = null
                                )
                            }
                        }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (entry.title.isNotBlank()) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (previewContent.isNotBlank()) {
                        Text(
                            text = previewContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.84f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (imagePaths.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        imagePaths.forEach { imagePath ->
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
                            ) {
                                AsyncImage(
                                    model = imagePath,
                                    contentDescription = "첨부 이미지",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyReflectionPreviewCard(
    monthLabel: String,
    reflection: MonthlyReflectionEntity?,
    entryCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
                )
                Text(
                    text = "월간 회고",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
                )
            }
            Text(
                text = if (reflection == null) {
                    "${entryCount}개의 기록을 한 장의 회고로 정리해보세요."
                } else {
                    reflection.reflectionText.ifBlank {
                        "${entryCount}개의 기록을 한 장의 회고로 남겨두었어요."
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (reflection == null) "회고 쓰기" else "회고 보기",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
            )
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 5.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 10.dp)
                    .height(64.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BottomNavigationTab(
                        text = "달력",
                        icon = Icons.Filled.CalendarToday,
                        modifier = Modifier.weight(1f),
                        onClick = onCalendarClick
                    )

                    Spacer(modifier = Modifier.width(66.dp))

                    BottomNavigationTab(
                        text = "마이",
                        icon = Icons.Filled.Person,
                        modifier = Modifier.weight(1f),
                        onClick = onMyPageClick
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-6).dp)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
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
                            tint = MaterialTheme.colorScheme.onPrimary
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
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




private fun Long.toYearMonthKey(): String {
    val cal = Calendar.getInstance().apply { timeInMillis = this@toYearMonthKey }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return "%04d-%02d".format(year, month)
}

private fun String.toYearMonthLabel(): String {
    val parts = split("-")
    if (parts.size != 2) return this
    val year = parts[0].toIntOrNull() ?: return this
    val month = parts[1].toIntOrNull() ?: return this
    return "${year}년 ${month}월"
}

private fun List<String>.topCountKeys(limit: Int = 3): List<String> {
    return asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .take(limit)
        .map { it.key }
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

private suspend fun MutableMap<String, MonthlyReflectionEntity>.replaceFromDatabase(
    dao: MonthlyReflectionDao
) {
    val loaded = withContext(Dispatchers.IO) {
        dao.getAll()
    }
    clear()
    loaded.forEach { item ->
        this[item.yearMonth] = item
    }
}

private suspend fun cleanupOrphanedImages(
    context: Context,
    dao: DiaryDao,
    reflectionDao: MonthlyReflectionDao
) {
    val imageDir = File(context.filesDir, "images")
    if (!imageDir.exists() || !imageDir.isDirectory) return

    val allFiles = imageDir.listFiles() ?: return
    if (allFiles.isEmpty()) return

    val entries = dao.getAllOrdered()
    val reflections = reflectionDao.getAll()

    val referencedPaths = mutableSetOf<String>()
    entries.forEach { entry ->
        referencedPaths.addAll(entry.imagePath.toImagePathList())
    }
    reflections.forEach { reflection ->
        if (reflection.coverImagePath.isNotBlank()) {
            referencedPaths.add(reflection.coverImagePath)
        }
    }

    allFiles.forEach { file ->
        val uriString = Uri.fromFile(file).toString()
        val absolutePath = file.absolutePath
        
        // Both Uri format and absolute path check for safety
        val isReferenced = referencedPaths.any { it == uriString || it == absolutePath }
        
        if (!isReferenced) {
            file.delete()
        }
    }
}
