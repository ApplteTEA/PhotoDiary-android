package com.example.photodiary

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    var previewImagePath by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
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
                Toast.makeText(
                    context,
                    "사진은 최대 5장까지 첨부할 수 있어 일부 사진만 추가되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGE_COUNT),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
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
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        )
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

    if (!previewImagePath.isNullOrBlank()) {
        Dialog(onDismissRequest = { previewImagePath = null }) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    AsyncImage(
                        model = Uri.parse(previewImagePath),
                        contentDescription = "이미지 크게 보기",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp),
                        contentScale = ContentScale.Fit
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { previewImagePath = null }) {
                            Text("닫기")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = { Text(text = "일기 작성") },
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
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
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
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = selectedDateMillis.toDisplayDate(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.5.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "제목",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        textStyle = MaterialTheme.typography.titleMedium,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp),
                        placeholder = {
                            Text(
                                text = "내용을 입력하세요",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "첨부 사진 ${imagePaths.size}/$MAX_IMAGE_COUNT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        if (imagePaths.size >= MAX_IMAGE_COUNT) {
                            Toast.makeText(
                                context,
                                "사진은 최대 5장까지 첨부할 수 있습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showAttachPicker = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(text = "사진 추가")
                }
            }

            imagePaths.forEachIndexed { index, _ ->
                if (index % 2 == 0) {
                    val leftImage = imagePaths.getOrNull(index)
                    val rightImage = imagePaths.getOrNull(index + 1)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (leftImage != null) {
                            ThumbnailCard(
                                imagePath = leftImage,
                                onDeleteClick = { imagePaths.remove(leftImage) },
                                onPreviewClick = { previewImagePath = leftImage },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rightImage != null) {
                            ThumbnailCard(
                                imagePath = rightImage,
                                onDeleteClick = { imagePaths.remove(rightImage) },
                                onPreviewClick = { previewImagePath = rightImage },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbnailCard(
    imagePath: String,
    onDeleteClick: () -> Unit,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(4f / 5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = Uri.parse(imagePath),
                contentDescription = "첨부 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "이미지 삭제"
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                IconButton(onClick = onPreviewClick) {
                    Icon(
                        imageVector = Icons.Outlined.ZoomIn,
                        contentDescription = "이미지 확대"
                    )
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
