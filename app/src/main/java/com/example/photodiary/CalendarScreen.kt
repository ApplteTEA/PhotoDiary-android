package com.example.photodiary

import android.widget.CalendarView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
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

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val selectedDate = remember(selectedDateMillis) {
        formatter.format(selectedDateMillis)
    }

    val filteredEntries = remember(entries, selectedDate) {
        entries
            .filter { it.diaryDate == selectedDate }
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    CalendarView(context).apply {
                        date = selectedDateMillis
                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                            val picked = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            selectedDateMillis = picked.timeInMillis
                        }
                    }
                },
                update = { view ->
                    if (view.date != selectedDateMillis) {
                        view.date = selectedDateMillis
                    }
                }
            )

            Text(
                text = "선택 날짜: $selectedDate",
                style = MaterialTheme.typography.labelLarge
            )

            Box(
                modifier = Modifier.fillMaxSize(),
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
                                    text = entry.diaryDate,
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
