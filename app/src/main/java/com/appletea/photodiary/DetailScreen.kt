package com.appletea.photodiary

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current

    val documentBlocks = remember(entry.content, entry.imagePath) {
        parseDiaryDocument(entry.content, entry.imagePath.toImagePathList())
    }
    val stickerPlacements = remember(entry.sticker) { entry.sticker.toStickerPlacements() }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var detailViewportSize by remember { mutableStateOf(IntSize.Zero) }

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

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("이 기록을 삭제할까요?") },
            text = { Text("삭제한 기록은 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        text = "기록",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                    TextButton(onClick = onEditClick) {
                        Text(
                            text = "수정",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    TextButton(onClick = { showDeleteConfirmDialog = true }) {
                        Text(
                            text = "삭제",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        val viewportHeight = with(density) { detailViewportSize.height.toDp() }
        val canvasBaseHeight = if (detailViewportSize.height > 0) {
            viewportHeight
        } else {
            RecordCanvasMinHeight
        }
        val preserveStickerCanvas = stickerPlacements.isNotEmpty()
        val detailMinHeight = if (preserveStickerCanvas) {
            (canvasBaseHeight - 8.dp).coerceAtLeast(RecordCanvasMinHeight)
        } else {
            0.dp
        }
        val trailingBodyMinHeight = if (preserveStickerCanvas) {
            (detailMinHeight - RecordCanvasContentReserve).coerceAtLeast(RecordCanvasBodyMinHeight)
        } else {
            0.dp
        }

        RecordPageViewport(
            innerPadding = innerPadding,
            onViewportSizeChanged = { detailViewportSize = it },
            scrollState = rememberRecordPageScrollState(),
            scrollContent = {
                ScrapbookPage(
                    entry = entry,
                    documentBlocks = documentBlocks,
                    stickerPlacements = stickerPlacements,
                    surfaceMinHeight = detailMinHeight,
                    trailingBodyMinHeight = trailingBodyMinHeight,
                    onPreviewImage = { previewImagePath = it }
                )
            }
        )
    }
}

@Composable
private fun ScrapbookPage(
    entry: DiaryEntry,
    documentBlocks: List<DiaryDocumentBlock>,
    stickerPlacements: List<DiaryStickerPlacement>,
    surfaceMinHeight: Dp,
    trailingBodyMinHeight: Dp,
    onPreviewImage: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DiaryStickerWritingSurfaceReadOnly(
            placements = stickerPlacements,
            contentHorizontalPadding = RecordCanvasInnerHorizontalPadding,
            contentVerticalPadding = RecordCanvasTopPadding,
            surfaceMinHeight = surfaceMinHeight,
            surfaceColor = MaterialTheme.colorScheme.background,
            flat = true,
            modifier = Modifier.fillMaxWidth()
        ) { contentModifier ->
            RecordPageSurfaceContent(contentModifier = contentModifier) {
                    DetailInfoHeader(entry = entry)

                    if (entry.title.isNotBlank()) {
                        Text(
                            text = entry.title,
                            modifier = Modifier.padding(horizontal = RecordTextInset),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DetailDocumentContent(
                        blocks = documentBlocks,
                        trailingBodyMinHeight = trailingBodyMinHeight,
                        onPreviewImage = onPreviewImage
                    )
            }
        }
    }
}

@Composable
private fun DetailInfoHeader(entry: DiaryEntry) {
    val tagLabel = entry.tag
        .trim()
        .takeIf { it.isNotBlank() }
        ?.split(Regex("\\s+"))
        ?.joinToString(" ") { token -> "#${token.trimStart('#')}" }
    RecordPageInfoHeader(
        diaryDate = entry.diaryDate.toDisplayDate(),
        weatherLabel = entry.weather.toMetaLabelOrNull(weatherOptions),
        moodLabel = entry.mood.toMetaLabelOrNull(moodOptions),
        tagLabel = tagLabel,
        showEmptyMetaSlots = true
    )
}

@Composable
private fun AttachedPhotoStrip(
    imagePath: String,
    onPreviewImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    DetailThumbnailCard(
        imagePath = imagePath,
        onPreviewClick = onPreviewImage,
        modifier = modifier
            .fillMaxWidth(0.58f)
            .aspectRatio(0.72f),
        shape = RoundedCornerShape(18.dp),
        showZoomBadge = false
    )
}

@Composable
private fun DetailDocumentContent(
    blocks: List<DiaryDocumentBlock>,
    trailingBodyMinHeight: Dp,
    onPreviewImage: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is DiaryDocumentBlock.Text -> {
                    if (block.value.isNotBlank()) {
                        Text(
                            text = block.value,
                            modifier = Modifier.padding(horizontal = RecordTextInset),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.18f
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f)
                        )
                    } else if (index == blocks.lastIndex && trailingBodyMinHeight > 0.dp) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(trailingBodyMinHeight)
                        )
                    }
                }

                is DiaryDocumentBlock.Image -> {
                    AttachedPhotoStrip(
                        imagePath = block.path,
                        onPreviewImage = { onPreviewImage(block.path) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailThumbnailCard(
    imagePath: String,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    showZoomBadge: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f))
            .clickable(onClick = onPreviewClick)
    ) {
        AsyncImage(
            model = Uri.parse(imagePath),
            contentDescription = "저장된 사진",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (showZoomBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ZoomIn,
                    contentDescription = "이미지 확대",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
