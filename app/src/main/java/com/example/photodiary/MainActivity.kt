package com.example.photodiary

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
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

    val dummyEntries = listOf(
        "2026-03-10 · 산책 사진",
        "2026-03-09 · 카페 기록",
        "2026-03-08 · 저녁 일기"
    )

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "Photo Diary") },
                windowInsets = WindowInsets.statusBars
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
            .navigationBarsPadding()
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
