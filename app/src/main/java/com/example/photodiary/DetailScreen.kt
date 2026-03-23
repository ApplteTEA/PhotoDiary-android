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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScrapbookMetaRow(entry = entry)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 3.dp
        ) {
            DiaryStickerOverlayReadOnly(
                placements = stickerPlacements,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 31.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        if (imagePaths.isNotEmpty()) {
            PhotoSection(
                imagePaths = imagePaths.take(5),
                onPreviewImage = onPreviewImage
            )
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
private fun PhotoSection(
    imagePaths: List<String>,
    onPreviewImage: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            imagePaths.forEachIndexed { index, imagePath ->
                DetailThumbnailCard(
                    imagePath = imagePath,
                    onPreviewClick = { onPreviewImage(imagePath) },
                    modifier = Modifier
                        .width(164.dp)
                        .rotate(if (index % 2 == 0) -2.5f else 2.5f),
                    shape = RoundedCornerShape(20.dp),
                    showZoomBadge = false
                )
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
            .aspectRatio(1f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f))
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
