package com.example.photodiary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.max
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

private const val MAX_STICKER_COUNT = 8

val DiaryPageCornerRadius = 28.dp
val DiaryPageHorizontalPadding = 20.dp
val DiaryPageVerticalPadding = 22.dp
val DiaryPageMinHeight = 480.dp
val DiaryPageBodyMinHeight = 220.dp

data class DiaryStickerOption(
    val key: String,
    val emoji: String,
    val label: String,
    val rotation: Float
)

data class DiaryStickerPlacement(
    val key: String,
    val xRatio: Float,
    val yRatio: Float
)

val stickerOptions = listOf(
    DiaryStickerOption("tape_heart", "🤍", "하트 테이프", -4f),
    DiaryStickerOption("luck_clover", "🍀", "행운 클로버", 3f),
    DiaryStickerOption("quiet_moon", "🌙", "조용한 밤", -6f),
    DiaryStickerOption("spark_star", "✨", "반짝 장면", 4f),
    DiaryStickerOption("coffee_break", "☕", "커피 메모", -2f),
    DiaryStickerOption("music_note", "🎵", "오늘의 노래", 5f),
    DiaryStickerOption("book_day", "📖", "책갈피 하루", -5f),
    DiaryStickerOption("flower_memo", "🌼", "꽃 메모", 2f)
)

private val legacyStickerKeyMap = mapOf(
    "clover" to "luck_clover",
    "heart" to "tape_heart",
    "coffee" to "coffee_break",
    "music" to "music_note",
    "star" to "spark_star",
    "moon" to "quiet_moon"
)

private fun String.normalizeStickerKey(): String {
    return legacyStickerKeyMap[this] ?: this
}

fun String.toStickerPlacements(): List<DiaryStickerPlacement> {
    if (isBlank()) return emptyList()
    return runCatching {
        if (!trim().startsWith("[")) {
            listOf(
                DiaryStickerPlacement(
                    key = normalizeStickerKey(),
                    xRatio = 0.14f,
                    yRatio = 0.16f
                )
            )
        } else {
            val array = JSONArray(this)
            buildList {
                repeat(array.length()) { index ->
                    val item = array.optJSONObject(index) ?: return@repeat
                    add(
                        DiaryStickerPlacement(
                            key = item.optString("key").normalizeStickerKey(),
                            xRatio = item.optDouble("x", 0.15).toFloat().coerceIn(0f, 1f),
                            yRatio = item.optDouble("y", 0.16).toFloat().coerceIn(0f, 1f)
                        )
                    )
                }
            }.filter { it.key.toStickerOptionOrNull() != null }
        }
    }.getOrElse { emptyList() }
}

fun List<DiaryStickerPlacement>.toStickerPayload(): String {
    if (isEmpty()) return ""
    return JSONArray().apply {
        forEach { placement ->
            put(
                JSONObject()
                    .put("key", placement.key)
                    .put("x", placement.xRatio.toDouble())
                    .put("y", placement.yRatio.toDouble())
            )
        }
    }.toString()
}

fun String.toStickerOptionOrNull(): DiaryStickerOption? {
    if (isBlank()) return null
    return stickerOptions.firstOrNull { it.key == this }
}

fun nextStickerPlacement(
    key: String,
    existingCount: Int
): DiaryStickerPlacement {
    val seeds = listOf(
        Offset(0.12f, 0.16f),
        Offset(0.64f, 0.12f),
        Offset(0.22f, 0.62f),
        Offset(0.68f, 0.54f),
        Offset(0.42f, 0.28f),
        Offset(0.52f, 0.72f)
    )
    val seed = seeds[existingCount % seeds.size]
    return DiaryStickerPlacement(key = key, xRatio = seed.x, yRatio = seed.y)
}

@Composable
fun DiaryStickerPalette(
    onAddSticker: (String) -> Unit,
    canAddMore: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        stickerOptions.forEach { option ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(enabled = canAddMore) { onAddSticker(option.key) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (canAddMore) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                    },
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = option.emoji, style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DiaryStickerCanvasEditor(
    placements: List<DiaryStickerPlacement>,
    titlePreview: String,
    contentPreview: String,
    onMoveSticker: (index: Int, xRatio: Float, yRatio: Float) -> Unit,
    onRemoveSticker: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    DiaryStickerCanvas(
        placements = placements,
        titlePreview = titlePreview,
        contentPreview = contentPreview,
        onMoveSticker = onMoveSticker,
        onRemoveSticker = onRemoveSticker,
        modifier = modifier
    )
}

@Composable
fun DiaryStickerCanvasReadOnly(
    placements: List<DiaryStickerPlacement>,
    titlePreview: String,
    contentPreview: String,
    modifier: Modifier = Modifier
) {
    DiaryStickerCanvas(
        placements = placements,
        titlePreview = titlePreview,
        contentPreview = contentPreview,
        onMoveSticker = null,
        onRemoveSticker = null,
        modifier = modifier
    )
}

@Composable
fun DiaryStickerWritingSurfaceEditor(
    placements: List<DiaryStickerPlacement>,
    onMoveSticker: (index: Int, xRatio: Float, yRatio: Float) -> Unit,
    onRemoveSticker: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit
) {
    DiaryStickerWritingSurface(
        placements = placements,
        onMoveSticker = onMoveSticker,
        onRemoveSticker = onRemoveSticker,
        modifier = modifier,
        content = content
    )
}

@Composable
fun DiaryStickerWritingSurfaceReadOnly(
    placements: List<DiaryStickerPlacement>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit
) {
    DiaryStickerWritingSurface(
        placements = placements,
        onMoveSticker = null,
        onRemoveSticker = null,
        modifier = modifier,
        content = content
    )
}

@Composable
fun DiaryStickerOverlayReadOnly(
    placements: List<DiaryStickerPlacement>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val stickerWidthPx = 104f
    val stickerHeightPx = 44f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { canvasSize = it }
    ) {
        content()

        placements.forEachIndexed { index, placement ->
            DiaryStickerPlacementNode(
                index = index,
                placement = placement,
                canvasSize = canvasSize,
                stickerWidthPx = stickerWidthPx,
                stickerHeightPx = stickerHeightPx,
                editable = false,
                onMoveSticker = null,
                onRemoveSticker = null
            )
        }
    }
}

@Composable
private fun DiaryStickerCanvas(
    placements: List<DiaryStickerPlacement>,
    titlePreview: String,
    contentPreview: String,
    onMoveSticker: ((index: Int, xRatio: Float, yRatio: Float) -> Unit)?,
    onRemoveSticker: ((index: Int) -> Unit)?,
    modifier: Modifier = Modifier
) {
    DiaryStickerWritingSurface(
        placements = placements,
        onMoveSticker = onMoveSticker,
        onRemoveSticker = onRemoveSticker,
        modifier = modifier.height(236.dp)
    ) { contentModifier ->
        Column(
            modifier = contentModifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "나만의 다이어리 조각",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
            Text(
                text = titlePreview.ifBlank { "오늘의 제목을 적어보세요" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contentPreview.ifBlank { "스티커를 붙이며 오늘의 기록을 조금 더 다정하게 꾸며볼 수 있어요." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (onMoveSticker != null) {
                    "스티커를 길게 끌어 원하는 위치로 옮겨보세요."
                } else {
                    "남겨둔 스티커 위치를 그대로 보여주는 페이지예요."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun DiaryStickerWritingSurface(
    placements: List<DiaryStickerPlacement>,
    onMoveSticker: ((index: Int, xRatio: Float, yRatio: Float) -> Unit)?,
    onRemoveSticker: ((index: Int) -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit
) {
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    val stickerWidthPx = 104f
    val stickerHeightPx = 44f
    val editable = onMoveSticker != null
    val canvasSize = remember(contentSize, viewportSize) {
        IntSize(
            width = max(contentSize.width, viewportSize.width),
            height = max(contentSize.height, viewportSize.height)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = DiaryPageMinHeight),
        shape = RoundedCornerShape(DiaryPageCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = DiaryPageHorizontalPadding,
                        vertical = DiaryPageVerticalPadding
                    )
            ) {
                content(
                    Modifier.onSizeChanged { measuredSize ->
                        if (measuredSize.width == 0 || measuredSize.height == 0) return@onSizeChanged
                        contentSize = IntSize(
                            width = measuredSize.width,
                            height = max(contentSize.height, measuredSize.height)
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = DiaryPageHorizontalPadding,
                        vertical = DiaryPageVerticalPadding
                    )
                    .onSizeChanged { viewportSize = it }
                    .zIndex(1f)
            ) {
                placements.forEachIndexed { index, placement ->
                    DiaryStickerPlacementNode(
                        index = index,
                        placement = placement,
                        canvasSize = canvasSize,
                        stickerWidthPx = stickerWidthPx,
                        stickerHeightPx = stickerHeightPx,
                        editable = editable,
                        onMoveSticker = onMoveSticker,
                        onRemoveSticker = onRemoveSticker
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaryStickerPlacementNode(
    index: Int,
    placement: DiaryStickerPlacement,
    canvasSize: IntSize,
    stickerWidthPx: Float,
    stickerHeightPx: Float,
    editable: Boolean,
    onMoveSticker: ((index: Int, xRatio: Float, yRatio: Float) -> Unit)?,
    onRemoveSticker: ((index: Int) -> Unit)?
) {
    val option = placement.key.toStickerOptionOrNull() ?: return
    val horizontalRange = (canvasSize.width - stickerWidthPx).coerceAtLeast(1f)
    val verticalRange = (canvasSize.height - stickerHeightPx).coerceAtLeast(1f)
    val xOffset = (horizontalRange * placement.xRatio).roundToInt()
    val yOffset = (verticalRange * placement.yRatio).roundToInt()
    val latestPlacement by rememberUpdatedState(placement)

    Surface(
        modifier = Modifier
            .offset { IntOffset(xOffset, yOffset) }
            .rotate(option.rotation)
            .then(
                if (editable) {
                    Modifier.pointerInput(index, canvasSize) {
                        var dragX = 0f
                        var dragY = 0f

                        detectDragGestures(
                            onDragStart = {
                                dragX = horizontalRange * latestPlacement.xRatio
                                dragY = verticalRange * latestPlacement.yRatio
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            dragX = (dragX + dragAmount.x).coerceIn(0f, horizontalRange)
                            dragY = (dragY + dragAmount.y).coerceIn(0f, verticalRange)
                            onMoveSticker?.invoke(
                                index,
                                (dragX / horizontalRange).coerceIn(0f, 1f),
                                (dragY / verticalRange).coerceIn(0f, 1f)
                            )
                        }
                    }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(18.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = option.emoji, style = MaterialTheme.typography.titleLarge)
        }
    }

    if (editable) {
        Surface(
            modifier = Modifier.offset { IntOffset(xOffset + 28, yOffset - 6) },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 0.5.dp,
            shadowElevation = 0.5.dp
        ) {
            IconButton(
                onClick = { onRemoveSticker?.invoke(index) },
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "스티커 삭제",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun canAddMoreStickers(placements: List<DiaryStickerPlacement>): Boolean {
    return placements.size < MAX_STICKER_COUNT
}
