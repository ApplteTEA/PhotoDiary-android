package com.example.photodiary

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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
    onSelectedDateChange: (Long) -> Unit,
    onBackClick: () -> Unit,
    onEntryClick: (Long) -> Unit
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val monthFormatter = remember { SimpleDateFormat("yyyy년 M월", Locale.getDefault()) }

    var selectedDateMillis by remember(initialSelectedDateMillis) {
        mutableLongStateOf(initialSelectedDateMillis.toDayStartMillis())
    }

    val selectedCalendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }
    var currentYear by remember(selectedDateMillis) { mutableIntStateOf(selectedCalendar.get(Calendar.YEAR)) }
    var currentMonth by remember(selectedDateMillis) { mutableIntStateOf(selectedCalendar.get(Calendar.MONTH)) }

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

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "Calendar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
                .padding(horizontal = 12.dp, top = 8.dp),
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
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (filteredEntries.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "선택한 날짜의 기록이 없습니다.",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "다른 날짜를 선택하거나 새 기록을 작성해보세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 6.dp)
                    ) {
                        items(filteredEntries, key = { it.id }) { entry ->
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
                            val bgColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                hasDiary -> Color(0xFFFF9800).copy(alpha = 0.26f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(bgColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = dayNumber.toString())
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
