package com.example.photodiary

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    entries: List<DiaryEntry>,
    onBackClick: () -> Unit,
    onEntryClick: (Long) -> Unit
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val monthFormatter = remember { SimpleDateFormat("yyyy년 M월", Locale.getDefault()) }

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis().toDayStartMillis()) }

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

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "Calendar") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "뒤로")
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
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
                        }.timeInMillis.toDayStartMillis()
                    }
                ) { Text(text = "◀") }

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
                                }.timeInMillis.toDayStartMillis()
                            },
                            currentYear,
                            currentMonth,
                            selectedCalendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) { Text(text = monthTitle) }

                TextButton(
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
                        }.timeInMillis.toDayStartMillis()
                    }
                ) { Text(text = "▶") }
            }

            WeekHeader()

            MonthGrid(
                year = currentYear,
                month = currentMonth,
                selectedDateMillis = selectedDay,
                diaryDateSet = diaryDateSet,
                onDateClick = { clicked ->
                    selectedDateMillis = clicked.toDayStartMillis()
                    val cal = Calendar.getInstance().apply { timeInMillis = clicked }
                    currentYear = cal.get(Calendar.YEAR)
                    currentMonth = cal.get(Calendar.MONTH)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (filteredEntries.isEmpty()) {
                    Text(
                        text = "선택한 날짜의 기록이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredEntries, key = { it.id }) { entry ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEntryClick(entry.id) }
                            ) {
                                Text(
                                    text = entry.diaryDate.toDisplayDate(),
                                    modifier = Modifier
                                        .padding(start = 12.dp, top = 12.dp, end = 12.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = entry.title,
                                    modifier = Modifier
                                        .padding(start = 12.dp, top = 6.dp, end = 12.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = entry.content,
                                    modifier = Modifier
                                        .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium
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
                style = MaterialTheme.typography.labelMedium
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
                                .height(44.dp)
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
                                    .size(30.dp)
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
                                .height(44.dp)
                        )
                    }
                }
            }
        }
    }
}
