package com.example.photodiary

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    entry: DiaryEntry,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "상세 보기") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = onEditClick) {
                        Text(text = "수정")
                    }
                    TextButton(onClick = onDeleteClick) {
                        Text(text = "삭제")
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = entry.diaryDate.toDisplayDate(),
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "사진",
                style = MaterialTheme.typography.titleSmall
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                if (entry.imagePath.isNullOrBlank()) {
                    Text(
                        text = "사진이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    AsyncImage(
                        model = Uri.parse(entry.imagePath),
                        contentDescription = "저장된 사진",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
