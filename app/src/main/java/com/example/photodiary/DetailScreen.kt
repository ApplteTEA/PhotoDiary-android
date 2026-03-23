package com.example.photodiary

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    val imagePaths = entry.imagePath.toImagePathList()
    val stickerPlacements = remember(entry.sticker) { entry.sticker.toStickerPlacements() }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ScrapbookPage(
                entry = entry,
                stickerPlacements = stickerPlacements,
                imagePaths = imagePaths,
                onPreviewImage = { previewImagePath = it }
            )
        }
    }
}

@Composable
private fun ScrapbookPage(
    entry: DiaryEntry,
    stickerPlacements: List<DiaryStickerPlacement>,
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 1.dp,
        shadowElevation = 4.dp
    ) {
        DiaryStickerOverlayReadOnly(
            placements = stickerPlacements,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ScrapbookMetaRow(entry = entry)

                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (imagePaths.isNotEmpty()) {
                    ScrapbookPhotoStrip(
                        imagePaths = imagePaths,
                        onPreviewImage = onPreviewImage
                    )
                }

                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 31.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )

                if (imagePaths.size > 2) {
                    QuietAttachmentGrid(
                        imagePaths = imagePaths.drop(2),
                        onPreviewImage = onPreviewImage
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrapbookMetaRow(entry: DiaryEntry) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = entry.diaryDate.toDisplayDate(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
            modifier = Modifier.align(Alignment.TopStart)
        )

        val metaLine = entry.toMetaLine()
        if (metaLine.isNotBlank()) {
            Text(
                text = metaLine,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .widthIn(max = 196.dp)
            )
        }
    }
}

@Composable
private fun ScrapbookPhotoStrip(
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    val topImages = imagePaths.take(2)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        topImages.forEachIndexed { index, imagePath ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .rotate(if (index % 2 == 0) -2.5f else 2.5f),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
                tonalElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailThumbnailCard(
                        imagePath = imagePath,
                        onPreviewClick = { onPreviewImage(imagePath) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        showZoomBadge = false
                    )
                    Text(
                        text = if (index == 0) "붙여둔 장면" else "남겨둔 한 컷",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuietAttachmentGrid(
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "더 남겨둔 사진",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                    if (rightImage != null) {
                        DetailThumbnailCard(
                            imagePath = rightImage,
                            onPreviewClick = { onPreviewImage(rightImage) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
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
                        .clickable(onClick = onPreviewClick)
                )
            }
        }
    }
}
