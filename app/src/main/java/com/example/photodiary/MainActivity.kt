package com.example.photodiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val dummyEntries = listOf(
        "2026-03-10 · 산책 사진",
        "2026-03-09 · 카페 기록",
        "2026-03-08 · 저녁 일기"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Photo Diary") }
            )
        },
        bottomBar = {
            BottomButtonBar(
                onCalendarClick = { /* TODO */ },
                onWriteClick = { /* TODO */ },
                onMyPageClick = { /* TODO */ }
            )
        }
    ) { innerPadding ->
        DiaryListSection(
            entries = dummyEntries,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun DiaryListSection(
    entries: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (entries.isEmpty()) {
            Text(
                text = "아직 작성된 다이어리가 없습니다.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(entries) { entry ->
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = entry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = onCalendarClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Calendar")
        }

        Button(
            onClick = onWriteClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Write",
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = onMyPageClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "MyPage")
        }
    }
}
