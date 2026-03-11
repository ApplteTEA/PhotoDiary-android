package com.example.photodiary

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    onBackClick: () -> Unit,
    initialDiaryDate: Long? = null,
    initialTitle: String = "",
    initialContent: String = "",
    initialImagePath: String? = null,
    onSaveClick: (diaryDate: Long, title: String, content: String, imagePath: String?) -> Unit
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current

    val initialDateMillis = remember(initialDiaryDate) {
        (initialDiaryDate ?: System.currentTimeMillis()).toDayStartMillis()
    }

    var selectedDateMillis by remember(initialDateMillis) { mutableLongStateOf(initialDateMillis) }
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var content by remember(initialContent) { mutableStateOf(initialContent) }
    var imagePath by remember(initialImagePath) { mutableStateOf(initialImagePath) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // no-op
                }
                imagePath = uri.toString()
            }
        }
    )

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "일기 작성") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "뒤로")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "제목과 내용을 입력해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onSaveClick(
                                    selectedDateMillis.toDayStartMillis(),
                                    title.trim(),
                                    content.trim(),
                                    imagePath
                                )
                            }
                        }
                    ) {
                        Text(text = "저장")
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
            OutlinedButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            selectedDateMillis = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }.timeInMillis.toDayStartMillis()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "날짜: ${selectedDateMillis.toDisplayDate()}")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("제목") }
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                label = { Text("내용") }
            )

            Text(
                text = "사진 첨부",
                style = MaterialTheme.typography.titleSmall
            )

            OutlinedButton(
                onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "갤러리에서 사진 선택")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                if (imagePath.isNullOrBlank()) {
                    Text(
                        text = "사진이 선택되지 않았습니다.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    AsyncImage(
                        model = Uri.parse(imagePath),
                        contentDescription = "선택한 사진",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
