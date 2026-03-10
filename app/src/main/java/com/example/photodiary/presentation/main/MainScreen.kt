package com.example.photodiary.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("UNUSED_PARAMETER")
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateCalendar: () -> Unit,
    onNavigateWrite: () -> Unit,
    onNavigateDetail: () -> Unit,
    onNavigateMyPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Main Screen")
        Text(text = "다이어리 리스트가 들어갈 예정입니다.")

        Button(onClick = onNavigateCalendar) { Text("Calendar") }
        Button(onClick = onNavigateWrite) { Text("Write") }
        Button(onClick = onNavigateDetail) { Text("Detail") }
        Button(onClick = onNavigateMyPage) { Text("MyPage") }
    }
}
