package com.appletea.photodiary

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    entries: List<DiaryEntry>,
    initialSelectedDateMillis: Long,
    initialViewYear: Int,
    initialViewMonth: Int,
    onSelectedDateChange: (Long) -> Unit,
    onViewMonthChange: (year: Int, month: Int) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: (Long) -> Unit,
    onEntryClick: (Long) -> Unit
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val monthFormatter = remember { SimpleDateFormat("yyyy년 M월", Locale.getDefault()) }

    var selectedDateMillis by remember { mutableLongStateOf(initialSelectedDateMillis.toDayStartMillis()) }

    LaunchedEffect(initialSelectedDateMillis) {
        selectedDateMillis = initialSelectedDateMillis.toDayStartMillis()
    }

    val selectedCalendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }
    var currentYear by remember { mutableIntStateOf(initialViewYear) }
    var currentMonth by remember { mutableIntStateOf(initialViewMonth) }

    val monthTitle = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time.let(monthFormatter::format)
    }

    val selectedDay = remember(selectedDateMillis) { selectedDateMillis.toDayStartMillis() }
    val diaryDateSet = remember(entries) { entries.map { it.diaryDate.toDayStartMillis() }.toSet() }

    val filteredEntries = remember(entries, selectedDay) {
        entries
            .filter { it.diaryDate.toDayStartMillis() == selectedDay }
            .sortedWith(
                compareByDescending<DiaryEntry> { it.diaryDate }
                    .thenByDescending { it.createdAt }
            )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "일정",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val selectedDayMillis = selectedDateMillis.toDayStartMillis()
                            onSelectedDateChange(selectedDayMillis)
                            onAddClick(selectedDayMillis)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "기록 추가"
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 6.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, currentYear)
                                        set(Calendar.MONTH, currentMonth)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                        add(Calendar.MONTH, -1)
                                    }
                                    currentYear = cal.get(Calendar.YEAR)
                                    currentMonth = cal.get(Calendar.MONTH)
                                    onViewMonthChange(currentYear, currentMonth)
                                    selectedDateMillis = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, currentYear)
                                        set(Calendar.MONTH, currentMonth)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                    }.timeInMillis.toDayStartMillis().also(onSelectedDateChange)
                                }
                            ) {
                                Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "이전 달")
                            }

                            TextButton(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            currentYear = year
                                            currentMonth = month
                                            onViewMonthChange(currentYear, currentMonth)
                                            selectedDateMillis = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            }.timeInMillis.toDayStartMillis().also(onSelectedDateChange)
                                        },
                                        currentYear,
                                        currentMonth,
                                        selectedCalendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = monthTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, currentYear)
                                        set(Calendar.MONTH, currentMonth)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                        add(Calendar.MONTH, 1)
                                    }
                                    currentYear = cal.get(Calendar.YEAR)
                                    currentMonth = cal.get(Calendar.MONTH)
                                    onViewMonthChange(currentYear, currentMonth)
                                    selectedDateMillis = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, currentYear)
                                        set(Calendar.MONTH, currentMonth)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                    }.timeInMillis.toDayStartMillis().also(onSelectedDateChange)
                                }
                            ) {
                                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "다음 달")
                            }
                        }

                        WeekHeader()

                        MonthGrid(
                            year = currentYear,
                            month = currentMonth,
                            selectedDateMillis = selectedDay,
                            diaryDateSet = diaryDateSet,
                            onDateClick = { clicked ->
                                selectedDateMillis = clicked.toDayStartMillis().also(onSelectedDateChange)
                                val cal = Calendar.getInstance().apply { timeInMillis = clicked }
                                currentYear = cal.get(Calendar.YEAR)
                                currentMonth = cal.get(Calendar.MONTH)
                                onViewMonthChange(currentYear, currentMonth)
                            }
                        )
                    }
                }

                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "선택한 날짜의 기록이 아직 없어요",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "오늘의 장면이나 기분을 이 날짜에 바로 남겨보세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            TextButton(
                                onClick = {
                                    val selectedDayMillis = selectedDateMillis.toDayStartMillis()
                                    onSelectedDateChange(selectedDayMillis)
                                    onAddClick(selectedDayMillis)
                                }
                            ) {
                                Text("이 날짜에 기록 추가")
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 10.dp)
                        ) {
                            items(filteredEntries, key = { it.id }) { entry ->
                                val imagePaths = entry.imagePath.toImagePathList().take(5)
                                val previewContent = remember(entry.content, entry.imagePath) {
                                    parseDiaryDocument(entry.content, entry.imagePath.toImagePathList()).toPlainTextPreview()
                                }
                                val moodLabel = entry.mood.toMetaLabelOrNull(moodOptions)
                                val weatherLabel = entry.weather.toMetaLabelOrNull(weatherOptions)
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
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = entry.diaryDate.toDisplayDate(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (weatherLabel != null) {
                                                    CompactMetaPill(
                                                        label = weatherLabel,
                                                        selected = true,
                                                        onClick = null
                                                    )
                                                }
                                                if (moodLabel != null) {
                                                    CompactMetaPill(
                                                        label = moodLabel,
                                                        selected = true,
                                                        onClick = null
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = entry.title.ifBlank { "제목 없는 기록" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (previewContent.isNotBlank()) {
                                            Text(
                                                text = previewContent,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                                                modifier = Modifier.padding(top = 2.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (imagePaths.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 5.dp)
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                imagePaths.forEach { imagePath ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(62.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
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
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHeader() {
    val labels = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Int,
    selectedDateMillis: Long,
    diaryDateSet: Set<Long>,
    onDateClick: (Long) -> Unit
) {
    val firstDay = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val offset = firstDay.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = ((offset + daysInMonth + 6) / 7) * 7

    Column {
        var dayNumber = 1
        repeat(totalCells / 7) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val inMonth = cellIndex >= offset && dayNumber <= daysInMonth

                    if (inMonth) {
                        val dateCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                        }
                        val dateMillis = dateCalendar.timeInMillis.toDayStartMillis()
                        val isSelected = dateMillis == selectedDateMillis.toDayStartMillis()
                        val hasDiary = diaryDateSet.contains(dateMillis)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { onDateClick(dateMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                            } else {
                                                Color.Transparent
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(if (hasDiary) 5.dp else 0.dp)
                                        .background(
                                            if (hasDiary) {
                                                Color(0xFFFF9800).copy(alpha = 0.74f)
                                            } else {
                                                Color.Transparent
                                            },
                                            CircleShape
                                        )
                                )
                            }
                        }
                        dayNumber += 1
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        )
                    }
                }
            }
        }
    }
}
