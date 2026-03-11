package com.example.photodiary

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    var showAttachPicker by remember { mutableStateOf(false) }
    var pendingCameraImageUri by remember { mutableStateOf<Uri?>(null) }

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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imagePath = pendingCameraImageUri?.toString()
            }
        }
    )

    if (showAttachPicker) {
        AlertDialog(
            onDismissRequest = { showAttachPicker = false },
            title = { Text("사진 첨부") },
            text = { Text("첨부 방법을 선택해주세요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAttachPicker = false
                        imagePickerLauncher.launch(arrayOf("image/*"))
                    }
                ) {
                    Text("갤러리에서 선택")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAttachPicker = false
                        val cameraUri = createCameraImageUri(context)
                        pendingCameraImageUri = cameraUri
                        cameraLauncher.launch(cameraUri)
                    }
                ) {
                    Text("카메라로 촬영")
                }
            }
        )
    }

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
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { showAttachPicker = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "이미지 첨부"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                label = { Text("내용") }
            )

            if (!imagePath.isNullOrBlank()) {
                Text(
                    text = "첨부 이미지",
                    style = MaterialTheme.typography.titleSmall
                )
                AsyncImage(
                    model = Uri.parse(imagePath),
                    contentDescription = "선택한 사진",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

private fun createCameraImageUri(context: android.content.Context): Uri {
    val imageDirectory = File(context.filesDir, "images").apply {
        if (!exists()) mkdirs()
    }
    val fileName = "camera_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}.jpg"
    val imageFile = File(imageDirectory, fileName)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
