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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    initialMood: String = "",
    initialWeather: String = "",
    initialTag: String = "",
    initialSticker: String = "",
    onSaveClick: (
        diaryDate: Long,
        title: String,
        content: String,
        imagePaths: List<String>,
        mood: String,
        weather: String,
        tag: String,
        sticker: String
    ) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
    val initialImagePathsSnapshot = remember(initialImagePaths) { initialImagePaths.take(MAX_IMAGE_COUNT) }
    var selectedMood by remember(initialMood) { androidx.compose.runtime.mutableStateOf(initialMood) }
    var selectedWeather by remember(initialWeather) { androidx.compose.runtime.mutableStateOf(initialWeather) }
    var tag by remember(initialTag) { androidx.compose.runtime.mutableStateOf(initialTag) }
    val initialStickerPlacements = remember(initialSticker) { initialSticker.toStickerPlacements() }
    val stickerPlacements = remember(initialSticker) {
        mutableStateListOf<DiaryStickerPlacement>().apply { addAll(initialStickerPlacements) }
    }
    val initialStickerPayload = remember(initialStickerPlacements) {
        initialStickerPlacements.toStickerPayload()
    }
    val stickerPayload = stickerPlacements.toList().toStickerPayload()
    var showExitConfirmDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    val hasChanges = remember(
        selectedDateMillis,
        title,
        content,
        imagePaths.size,
        selectedMood,
        selectedWeather,
        tag,
        stickerPlacements.size,
        stickerPayload,
        initialDateMillis,
        initialTitle,
        initialContent,
        initialImagePathsSnapshot,
        initialMood,
        initialWeather,
        initialTag,
        initialStickerPayload
    ) {
        selectedDateMillis.toDayStartMillis() != initialDateMillis.toDayStartMillis() ||
            title != initialTitle ||
            content != initialContent ||
            imagePaths.toList() != initialImagePathsSnapshot ||
            selectedMood != initialMood ||
            selectedWeather != initialWeather ||
            tag != initialTag ||
            stickerPayload != initialStickerPayload
    }

    val canSave = remember(title, content, imagePaths.size, stickerPlacements.size) {
        !(title.isBlank() && content.isBlank() && imagePaths.isEmpty() && stickerPlacements.isEmpty())
    }

    val attemptExit: () -> Unit = {
        if (hasChanges) {
            showExitConfirmDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(onBack = attemptExit)

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
                scope.launch {
                    val copiedPaths = withContext(Dispatchers.IO) {
                        uris.mapNotNull { uri ->
                            copyImageToInternalStorage(context, uri)
                        }
                    }
                    if (copiedPaths.isEmpty()) {
                        Toast.makeText(
                            context,
                            "이미지를 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        appendImages(copiedPaths)
                    }
                }
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
                        contentScale = ContentScale.Crop
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

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("작성 중인 내용을 나갈까요?") },
            text = { Text("저장하지 않으면 입력한 내용이 사라집니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        onBackClick()
                    }
                ) {
                    Text(
                        text = "나가기",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(
                        text = "계속 작성",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "일기 작성",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = attemptExit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (!canSave) return@TextButton
                            onSaveClick(
                                selectedDateMillis.toDayStartMillis(),
                                title.trim(),
                                content.trim(),
                                imagePaths.toList(),
                                selectedMood,
                                selectedWeather,
                                tag.trim(),
                                stickerPayload
                            )
                        },
                        enabled = canSave
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
                .padding(horizontal = 14.dp)
                .padding(top = 3.dp, bottom = 8.dp)
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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

            OutlinedTextField(
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
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.07f),
                    cursorColor = MaterialTheme.colorScheme.onSurface
                )
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 190.dp),
                placeholder = {
                    Text(
                        text = "내용을 입력해주세요",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.07f),
                    cursorColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "오늘의 분위기",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moodOptions.forEach { option ->
                            FilterChip(
                                selected = selectedMood == option.key,
                                onClick = {
                                    selectedMood = if (selectedMood == option.key) "" else option.key
                                },
                                label = { Text(option.label) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weatherOptions.forEach { option ->
                            FilterChip(
                                selected = selectedWeather == option.key,
                                onClick = {
                                    selectedWeather = if (selectedWeather == option.key) "" else option.key
                                },
                                label = { Text(option.label) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = tag,
                        onValueChange = { value -> tag = value.take(16) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("태그 (예: 가족, 여행, 카페)") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.07f),
                            cursorColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "다이어리 꾸미기",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "스티커를 덧붙여 오늘의 페이지를 조금 더 자유롭게 꾸며보세요. 리스트에는 보이지 않고, 수정과 상세에서 그대로 남아요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.84f)
                    )
                    DiaryStickerCanvasEditor(
                        placements = stickerPlacements,
                        titlePreview = title,
                        contentPreview = content,
                        onMoveSticker = { index, xRatio, yRatio ->
                            stickerPlacements[index] = stickerPlacements[index].copy(
                                xRatio = xRatio,
                                yRatio = yRatio
                            )
                        },
                        onRemoveSticker = { index -> stickerPlacements.removeAt(index) }
                    )
                    Text(
                        text = if (canAddMoreStickers(stickerPlacements)) {
                            "스티커를 눌러 추가하고, 끌어서 원하는 곳에 붙여보세요. 최대 8개까지 가능해요."
                        } else {
                            "스티커는 최대 8개까지 붙일 수 있어요."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DiaryStickerPalette(
                            onAddSticker = { key ->
                                if (canAddMoreStickers(stickerPlacements)) {
                                    stickerPlacements.add(
                                        nextStickerPlacement(
                                            key = key,
                                            existingCount = stickerPlacements.size
                                        )
                                    )
                                }
                            },
                            canAddMore = canAddMoreStickers(stickerPlacements)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(16.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (leftImage != null) {
                                    ThumbnailCard(
                                        imagePath = leftImage,
                                        onDeleteClick = {
                                            deleteInternalImageIfExists(context, leftImage)
                                            imagePaths.remove(leftImage)
                                        },
                                        onPreviewClick = { previewImagePath = leftImage },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rightImage != null) {
                                    ThumbnailCard(
                                        imagePath = rightImage,
                                        onDeleteClick = {
                                            deleteInternalImageIfExists(context, rightImage)
                                            imagePaths.remove(rightImage)
                                        },
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
            .aspectRatio(1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = Uri.parse(imagePath),
                contentDescription = "첨부 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "이미지 삭제",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
            ) {
                IconButton(
                    onClick = onPreviewClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ZoomIn,
                        contentDescription = "이미지 확대",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}


private fun copyImageToInternalStorage(context: android.content.Context, sourceUri: Uri): String? {
    return runCatching {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(sourceUri)
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val imageDirectory = File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }

        val fileName = "gallery_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}_${(1000..9999).random()}.$extension"
        val targetFile = File(imageDirectory, fileName)

        resolver.openInputStream(sourceUri).use { input ->
            if (input == null) return null
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(targetFile).toString()
    }.getOrNull()
}

private fun deleteInternalImageIfExists(context: android.content.Context, imagePath: String) {
    val file = imagePath.toInternalImageFileOrNull(context)
    if (file?.exists() == true) {
        file.delete()
    }
}

private fun String.toInternalImageFileOrNull(context: android.content.Context): File? {
    val parsed = runCatching { Uri.parse(this) }.getOrNull() ?: return null
    val candidate = when {
        parsed.scheme == "file" -> parsed.path?.let(::File)
        startsWith(context.filesDir.absolutePath) -> File(this)
        else -> null
    } ?: return null

    val basePath = runCatching { context.filesDir.canonicalPath }.getOrNull() ?: return null
    val candidatePath = runCatching { candidate.canonicalPath }.getOrNull() ?: return null
    return if (candidatePath.startsWith(basePath)) candidate else null
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
