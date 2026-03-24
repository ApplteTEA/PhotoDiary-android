package com.appletea.photodiary

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val initialDateMillis = remember(initialDiaryDate) {
        (initialDiaryDate ?: System.currentTimeMillis()).toDayStartMillis()
    }

    var selectedDateMillis by remember(initialDateMillis) { mutableLongStateOf(initialDateMillis) }
    var title by remember(initialTitle) { androidx.compose.runtime.mutableStateOf(initialTitle) }
    var content by remember(initialContent) { androidx.compose.runtime.mutableStateOf(initialContent) }
    var showAttachPicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    var pendingCameraImageUri by remember { androidx.compose.runtime.mutableStateOf<Uri?>(null) }
    var previewImagePath by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var showExitConfirmDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showMoodPicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showWeatherPicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showStickerTray by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showPhotoTray by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showTagDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

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
    var stickerCanvasSize by remember { androidx.compose.runtime.mutableStateOf(IntSize.Zero) }
    var editorViewportSize by remember { androidx.compose.runtime.mutableStateOf(IntSize.Zero) }
    val stickerPayload = stickerPlacements.toList().toStickerPayload()

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
    val contentScrollState = rememberScrollState()
    var previousContentLength by remember(initialContent) { mutableIntStateOf(initialContent.length) }

    LaunchedEffect(content, contentScrollState.maxValue) {
        val currentLength = content.length
        val contentGrew = currentLength > previousContentLength
        previousContentLength = currentLength

        if (contentGrew && contentScrollState.maxValue > 0) {
            contentScrollState.animateScrollTo(contentScrollState.maxValue)
        }
    }

    val attemptExit: () -> Unit = {
        if (hasChanges) {
            showExitConfirmDialog = true
        } else {
            onBackClick()
        }
    }

    val collapseToolPanels: () -> Unit = {
        showPhotoTray = false
        showStickerTray = false
    }

    val dismissKeyboardAndToggleTools: (showPhoto: Boolean) -> Unit = { showPhoto ->
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        if (showPhoto) {
            showPhotoTray = !showPhotoTray
            if (showPhotoTray) showStickerTray = false
        } else {
            showStickerTray = !showStickerTray
            if (showStickerTray) showPhotoTray = false
        }
    }

    BackHandler(onBack = attemptExit)

    val appendImages: (List<String>) -> Unit = { newPaths ->
        val remaining = MAX_IMAGE_COUNT - imagePaths.size
        if (remaining <= 0) {
            Toast.makeText(context, "사진은 최대 5장까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
        } else {
            imagePaths.addAll(newPaths.take(remaining))
            showPhotoTray = true
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

    if (showMoodPicker) {
        DiaryOptionPickerDialog(
            title = "오늘은 어때?",
            options = moodOptions,
            selectedKey = selectedMood,
            onDismiss = { showMoodPicker = false },
            onSelect = { key ->
                selectedMood = if (selectedMood == key) "" else key
                showMoodPicker = false
            }
        )
    }

    if (showWeatherPicker) {
        DiaryOptionPickerDialog(
            title = "오늘 날씨는?",
            options = weatherOptions,
            selectedKey = selectedWeather,
            onDismiss = { showWeatherPicker = false },
            onSelect = { key ->
                selectedWeather = if (selectedWeather == key) "" else key
                showWeatherPicker = false
            }
        )
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("태그 메모") },
            text = {
                OutlinedTextField(
                    value = tag,
                    onValueChange = { value -> tag = value.take(32) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("가족 여행 카페") },
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tag = tag.toStoredTagText()
                        showTagDialog = false
                    }
                ) {
                    Text("완료")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tag = tag.toStoredTagText()
                        showTagDialog = false
                    }
                ) {
                    Text("닫기")
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
                        text = "기록 쓰기",
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
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showStickerTray) {
                    FloatingStickerTray(
                        placements = stickerPlacements,
                        onAddSticker = { key ->
                            if (canAddMoreStickers(stickerPlacements)) {
                                stickerPlacements.add(
                                    nextStickerPlacement(
                                        key = key,
                                        existingCount = stickerPlacements.size,
                                        scrollOffsetPx = contentScrollState.value,
                                        viewportHeightPx = editorViewportSize.height,
                                        canvasHeightPx = stickerCanvasSize.height
                                    )
                                )
                            }
                        }
                    )
                }

                if (showPhotoTray) {
                    FloatingPhotoTray(
                        imagePaths = imagePaths,
                        onAddPhoto = {
                            if (imagePaths.size >= MAX_IMAGE_COUNT) {
                                Toast.makeText(
                                    context,
                                    "사진은 최대 5장까지 첨부할 수 있습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showAttachPicker = true
                            }
                        },
                        onDeletePhoto = { imagePath ->
                            deleteInternalImageIfExists(context, imagePath)
                            imagePaths.remove(imagePath)
                        },
                        onPreviewPhoto = { imagePath ->
                            previewImagePath = imagePath
                        }
                    )
                }

                FloatingToolBar(
                    imageCount = imagePaths.size,
                    stickerCount = stickerPlacements.size,
                    tag = tag,
                    onPhotoClick = {
                        dismissKeyboardAndToggleTools(true)
                    },
                    onStickerClick = {
                        dismissKeyboardAndToggleTools(false)
                    },
                    onTagClick = {
                        showTagDialog = true
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onSizeChanged { editorViewportSize = it }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val editorMinHeight = (maxHeight - 8.dp).coerceAtLeast(500.dp)
                    val bodyMinHeight = (editorMinHeight - 156.dp).coerceAtLeast(280.dp)

                    DiaryStickerWritingSurfaceEditor(
                        placements = stickerPlacements,
                        onMoveSticker = { index, xRatio, yRatio ->
                            stickerPlacements[index] = stickerPlacements[index].copy(
                                xRatio = xRatio,
                                yRatio = yRatio
                            )
                        },
                        onRemoveSticker = { index -> stickerPlacements.removeAt(index) },
                        onCanvasSizeChanged = { stickerCanvasSize = it },
                        contentHorizontalPadding = 18.dp,
                        contentVerticalPadding = 20.dp,
                        surfaceMinHeight = editorMinHeight,
                        modifier = Modifier.fillMaxWidth()
                    ) { contentSizeModifier ->
                        Column(
                            modifier = contentSizeModifier
                                .fillMaxWidth()
                                .padding(bottom = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WriteInfoHeader(
                                diaryDate = selectedDateMillis,
                                moodLabel = selectedMood.toMetaLabelOrNull(moodOptions) ?: "기분 선택",
                                weatherLabel = selectedWeather.toMetaLabelOrNull(weatherOptions) ?: "날씨 선택",
                                isMoodSelected = selectedMood.isNotBlank(),
                                isWeatherSelected = selectedWeather.isNotBlank(),
                                onDateClick = {
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
                                onMoodClick = {
                                    showMoodPicker = true
                                    showWeatherPicker = false
                                },
                                onWeatherClick = {
                                    showWeatherPicker = true
                                    showMoodPicker = false
                                }
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        collapseToolPanels()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { state ->
                                            if (state.isFocused) collapseToolPanels()
                                        },
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            text = "제목",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.titleSmall,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                    colors = lowChromeTextFieldColors()
                                )

                                OutlinedTextField(
                                    value = content,
                                    onValueChange = {
                                        content = it
                                        collapseToolPanels()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = bodyMinHeight)
                                        .onFocusChanged { state ->
                                            if (state.isFocused) collapseToolPanels()
                                        },
                                    placeholder = {
                                        Text(
                                            text = "오늘 남기고 싶은 이야기를 천천히 적어보세요.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                    colors = lowChromeTextFieldColors(),
                                    minLines = 10,
                                    maxLines = Int.MAX_VALUE
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WriteInfoHeader(
    diaryDate: Long,
    moodLabel: String,
    weatherLabel: String,
    isMoodSelected: Boolean,
    isWeatherSelected: Boolean,
    onDateClick: () -> Unit,
    onMoodClick: () -> Unit,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WriteDateCard(
            diaryDate = diaryDate,
            onClick = onDateClick,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactMetaPill(
                label = moodLabel,
                selected = isMoodSelected,
                onClick = onMoodClick
            )
            CompactMetaPill(
                label = weatherLabel,
                selected = isWeatherSelected,
                onClick = onWeatherClick
            )
        }
    }
}

@Composable
fun CompactMetaPill(
    label: String,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else {
            modifier
        },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun WriteDateCard(
    diaryDate: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Text(
            text = diaryDate.toDisplayDate(),
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DiaryMetaPill(
    label: String,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else {
            modifier
        },
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
        },
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun DiaryOptionPickerDialog(
    title: String,
    options: List<DiaryOption>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.chunked(4).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { option ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelect(option.key) },
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedKey == option.key) {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
                                },
                                tonalElevation = if (selectedKey == option.key) 1.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        repeat(4 - rowOptions.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun FloatingToolBar(
    imageCount: Int,
    stickerCount: Int,
    tag: String,
    onPhotoClick: () -> Unit,
    onStickerClick: () -> Unit,
    onTagClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingToolButton(
                label = if (imageCount > 0) "사진 $imageCount" else "사진",
                onClick = onPhotoClick,
                leading = {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            FloatingToolButton(
                label = if (stickerCount > 0) "스티커 $stickerCount" else "스티커",
                onClick = onStickerClick,
                leading = {
                    Text(
                        text = "✦",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
            FloatingToolButton(
                label = if (tag.isBlank()) "태그" else tag.toDisplayHashtags().take(8),
                onClick = onTagClick,
                leading = {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun FloatingToolButton(
    label: String,
    onClick: () -> Unit,
    leading: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FloatingStickerTray(
    placements: List<DiaryStickerPlacement>,
    onAddSticker: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (canAddMoreStickers(placements)) {
                    "붙이고 싶은 스티커를 골라보세요"
                } else {
                    "스티커는 최대 8개까지 붙일 수 있어요"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiaryStickerPalette(
                    onAddSticker = onAddSticker,
                    canAddMore = canAddMoreStickers(placements)
                )
            }
        }
    }
}

@Composable
private fun FloatingPhotoTray(
    imagePaths: List<String>,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onPreviewPhoto: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                TextButton(onClick = onAddPhoto) {
                    Text("사진 추가")
                }
            }

            if (imagePaths.isEmpty()) {
                Text(
                    text = "사진은 하단 도구 바에서 불러와 기록과 함께 남길 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    imagePaths.forEach { imagePath ->
                        ThumbnailCard(
                            imagePath = imagePath,
                            onDeleteClick = { onDeletePhoto(imagePath) },
                            onPreviewClick = { onPreviewPhoto(imagePath) },
                            modifier = Modifier.width(92.dp)
                        )
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
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)
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

@Composable
private fun lowChromeTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.onSurface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
)

private fun String.toStoredTagText(): String {
    return trim()
        .replace("#", " ")
        .split(Regex("\\s+"))
        .map { it.trim().trim(',') }
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .take(32)
}

private fun String.toEditableTagText(): String {
    return trim()
}

private fun String.toDisplayHashtags(): String {
    if (isBlank()) return ""
    return trim()
        .replace("#", " ")
        .split(Regex("\\s+"))
        .map { it.trim().trim(',') }
        .filter { it.isNotBlank() }
        .joinToString(" ") { "#$it" }
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
