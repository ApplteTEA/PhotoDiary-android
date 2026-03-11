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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateListOf
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

private const val MAX_IMAGE_COUNT = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    onBackClick: () -> Unit,
    initialDiaryDate: Long? = null,
    initialTitle: String = "",
    initialContent: String = "",
    initialImagePaths: List<String> = emptyList(),
    onSaveClick: (diaryDate: Long, title: String, content: String, imagePaths: List<String>) -> Unit
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current

    val initialDateMillis = remember(initialDiaryDate) {
        (initialDiaryDate ?: System.currentTimeMillis()).toDayStartMillis()
    }

    var selectedDateMillis by remember(initialDateMillis) { mutableLongStateOf(initialDateMillis) }
    var title by remember(initialTitle) { androidx.compose.runtime.mutableStateOf(initialTitle) }
    var content by remember(initialContent) { androidx.compose.runtime.mutableStateOf(initialContent) }
    var showAttachPicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    var pendingCameraImageUri by remember { androidx.compose.runtime.mutableStateOf<Uri?>(null) }
    val imagePaths = remember(initialImagePaths) {
        mutableStateListOf<String>().apply {
            addAll(initialImagePaths.take(MAX_IMAGE_COUNT))
        }
    }

    val appendImages: (List<String>) -> Unit = { newPaths ->
        val remaining = MAX_IMAGE_COUNT - imagePaths.size
        if (remaining <= 0) {
            Toast.makeText(context, "사진은 최대 5장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
        } else {
            imagePaths.addAll(newPaths.take(remaining))
            if (newPaths.size > remaining) {
                Toast.makeText(context, "사진은 최대 5장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {
                        // no-op
                    }
                }
                appendImages(uris.map { it.toString() })
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                pendingCameraImageUri?.toString()?.let { captured ->
                    appendImages(listOf(captured))
                }
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
                        if (imagePaths.size >= MAX_IMAGE_COUNT) {
                            Toast.makeText(context, "사진은 최대 5장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            val cameraUri = createCameraImageUri(context)
                            pendingCameraImageUri = cameraUri
                            cameraLauncher.launch(cameraUri)
                        }
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
                                    imagePaths.toList()
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
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                label = { Text("내용") }
            )

            Text(
                text = "첨부 사진 (${imagePaths.size}/$MAX_IMAGE_COUNT)",
                style = MaterialTheme.typography.titleSmall
            )

            OutlinedButton(
                onClick = { showAttachPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "사진 추가")
            }

            imagePaths.forEachIndexed { index, image ->
                AsyncImage(
                    model = Uri.parse(image),
                    contentDescription = "첨부 이미지 ${index + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { imagePaths.remove(image) }) {
                        Text("삭제")
                    }
                }
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
