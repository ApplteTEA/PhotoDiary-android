package com.example.photodiary

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

private enum class DetailVariant(val label: String) {
    Notebook("노트형"),
    Scrapbook("스크랩형"),
    Minimal("미니멀형")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    entry: DiaryEntry,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    val imagePaths = entry.imagePath.toImagePathList()
    val stickerPlacements = remember(entry.sticker) { entry.sticker.toStickerPlacements() }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedVariant by remember { mutableStateOf(DetailVariant.Notebook) }

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
                        text = "상세 보기",
                        style = MaterialTheme.typography.titleMedium
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
                    TextButton(onClick = onEditClick) { Text(text = "수정") }
                    TextButton(onClick = { showDeleteConfirmDialog = true }) { Text(text = "삭제") }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            VariantSwitcher(
                selectedVariant = selectedVariant,
                onSelect = { selectedVariant = it }
            )

            when (selectedVariant) {
                DetailVariant.Notebook -> NotebookDetailVariant(
                    entry = entry,
                    stickerPlacements = stickerPlacements,
                    imagePaths = imagePaths,
                    onPreviewImage = { previewImagePath = it }
                )

                DetailVariant.Scrapbook -> ScrapbookDetailVariant(
                    entry = entry,
                    stickerPlacements = stickerPlacements,
                    imagePaths = imagePaths,
                    onPreviewImage = { previewImagePath = it }
                )

                DetailVariant.Minimal -> MinimalDetailVariant(
                    entry = entry,
                    stickerPlacements = stickerPlacements,
                    imagePaths = imagePaths,
                    onPreviewImage = { previewImagePath = it }
                )
            }
        }
    }
}

@Composable
private fun VariantSwitcher(
    selectedVariant: DetailVariant,
    onSelect: (DetailVariant) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailVariant.entries.forEach { variant ->
            Surface(
                modifier = Modifier.clickable { onSelect(variant) },
                shape = RoundedCornerShape(20.dp),
                color = if (selectedVariant == variant) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
                },
                tonalElevation = if (selectedVariant == variant) 1.dp else 0.dp
            ) {
                Text(
                    text = variant.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NotebookDetailVariant(
    entry: DiaryEntry,
    stickerPlacements: List<DiaryStickerPlacement>,
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    val density = LocalDensity.current
    val guideTopPx = with(density) { 108.dp.toPx() }
    val guideSpacingPx = with(density) { 34.dp.toPx() }
    val guideColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    val marginColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)

    DiaryStickerOverlayReadOnly(
        placements = stickerPlacements,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 340.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    var currentY = guideTopPx
                    while (currentY < size.height) {
                        drawLine(
                            color = guideColor,
                            start = Offset(0f, currentY),
                            end = Offset(size.width, currentY),
                            strokeWidth = 1f
                        )
                        currentY += guideSpacingPx
                    }
                    drawLine(
                        color = marginColor,
                        start = Offset(22.dp.toPx(), 0f),
                        end = Offset(22.dp.toPx(), size.height),
                        strokeWidth = 1f
                    )
                }
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            DiaryEntryTextBlock(
                entry = entry,
                titleStyle = MaterialTheme.typography.headlineMedium,
                contentStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 34.sp),
                metaStyle = MaterialTheme.typography.bodySmall,
                dateStyle = MaterialTheme.typography.labelLarge
            )
        }
    }

    QuietPhotoSection(
        imagePaths = imagePaths,
        label = "붙여둔 사진",
        cardShape = RoundedCornerShape(18.dp),
        onPreviewImage = onPreviewImage
    )
}

@Composable
private fun ScrapbookDetailVariant(
    entry: DiaryEntry,
    stickerPlacements: List<DiaryStickerPlacement>,
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 4.dp,
            tonalElevation = 1.dp
        ) {
            DiaryStickerOverlayReadOnly(
                placements = stickerPlacements,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 340.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DiaryTopMetaRow(
                        date = entry.diaryDate.toDisplayDate(),
                        metaLine = entry.toMetaLine(),
                        dateStyle = MaterialTheme.typography.labelMedium,
                        metaStyle = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (imagePaths.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                imagePaths.forEachIndexed { index, imagePath ->
                    Surface(
                        modifier = Modifier
                            .width(156.dp)
                            .rotate(if (index % 2 == 0) -2f else 2f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailThumbnailCard(
                                imagePath = imagePath,
                                onPreviewClick = { onPreviewImage(imagePath) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                showZoomBadge = false
                            )
                            Text(
                                text = "붙여둔 장면 ${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalDetailVariant(
    entry: DiaryEntry,
    stickerPlacements: List<DiaryStickerPlacement>,
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    DiaryStickerOverlayReadOnly(
        placements = stickerPlacements,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DiaryTopMetaRow(
                date = entry.diaryDate.toDisplayDate(),
                metaLine = entry.toMetaLine(),
                dateStyle = MaterialTheme.typography.labelMedium,
                metaStyle = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
            )

            Text(
                text = entry.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 36.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    QuietPhotoSection(
        imagePaths = imagePaths,
        label = "photo archive",
        cardShape = RoundedCornerShape(14.dp),
        onPreviewImage = onPreviewImage
    )
}

@Composable
private fun DiaryEntryTextBlock(
    entry: DiaryEntry,
    titleStyle: androidx.compose.ui.text.TextStyle,
    contentStyle: androidx.compose.ui.text.TextStyle,
    metaStyle: androidx.compose.ui.text.TextStyle,
    dateStyle: androidx.compose.ui.text.TextStyle
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        DiaryTopMetaRow(
            date = entry.diaryDate.toDisplayDate(),
            metaLine = entry.toMetaLine(),
            dateStyle = dateStyle,
            metaStyle = metaStyle
        )

        Text(
            text = entry.title,
            style = titleStyle,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = entry.content,
            style = contentStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DiaryTopMetaRow(
    date: String,
    metaLine: String,
    dateStyle: androidx.compose.ui.text.TextStyle,
    metaStyle: androidx.compose.ui.text.TextStyle
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = date,
            style = dateStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            modifier = Modifier.align(Alignment.TopStart)
        )
        if (metaLine.isNotBlank()) {
            Text(
                text = metaLine,
                style = metaStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .widthIn(max = 190.dp)
            )
        }
    }
}

@Composable
private fun QuietPhotoSection(
    imagePaths: List<String>,
    label: String,
    cardShape: RoundedCornerShape,
    onPreviewImage: (String) -> Unit
) {
    if (imagePaths.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
        )

        imagePaths.forEachIndexed { index, _ ->
            if (index % 2 == 0) {
                val leftImage = imagePaths.getOrNull(index)
                val rightImage = imagePaths.getOrNull(index + 1)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (leftImage != null) {
                        DetailThumbnailCard(
                            imagePath = leftImage,
                            onPreviewClick = { onPreviewImage(leftImage) },
                            modifier = Modifier.weight(1f),
                            shape = cardShape
                        )
                    }
                    if (rightImage != null) {
                        DetailThumbnailCard(
                            imagePath = rightImage,
                            onPreviewClick = { onPreviewImage(rightImage) },
                            modifier = Modifier.weight(1f),
                            shape = cardShape
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
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
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.5.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = Uri.parse(imagePath),
                contentDescription = "저장된 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (showZoomBadge) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                ) {
                    IconButton(
                        onClick = onPreviewClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ZoomIn,
                            contentDescription = "이미지 확대",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .clickable(onClick = onPreviewClick)
                )
            }
        }
    }
}
