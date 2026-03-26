package com.appletea.photodiary

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.annotation.DrawableRes
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import org.json.JSONArray
import org.json.JSONObject

private const val MAX_STICKER_COUNT = 8
private const val STICKER_NODE_HEIGHT_PX = 44f
private const val MIN_STICKER_SCALE = 1f
private const val MAX_STICKER_SCALE = 2.8f
private const val STICKER_ROTATION_SENSITIVITY = 0.4f

val DiaryPageCornerRadius = 22.dp
val DiaryPageHorizontalPadding = 18.dp
val DiaryPageVerticalPadding = 20.dp
val DiaryPageMinHeight = 480.dp
val DiaryPageBodyMinHeight = 220.dp

data class DiaryStickerOption(
    val key: String,
    @DrawableRes val imageResId: Int,
    val label: String,
    val rotation: Float,
    val visualScale: Float = 1f
)

data class DiaryStickerPlacement(
    val key: String,
    val xRatio: Float,
    val yRatio: Float,
    val scale: Float = 1f,
    val rotation: Float? = null
)

private data class StickerPayloadData(
    val placements: List<DiaryStickerPlacement>,
    val bodyMinHeightDp: Float? = null
)

private val allStickerOptions = listOf(
    DiaryStickerOption("tape_heart", R.drawable.sticker_heart, "하트", -4f, visualScale = 0.9f),
    DiaryStickerOption("luck_clover", R.drawable.sticker_clover, "클로버", 3f, visualScale = 0.94f),
    DiaryStickerOption("quiet_moon", R.drawable.sticker_moon, "달", -6f, visualScale = 0.82f),
    DiaryStickerOption("spark_star", R.drawable.sticker_sparkles, "반짝", 4f, visualScale = 0.92f),
    DiaryStickerOption("coffee_break", R.drawable.sticker_coffee, "커피", -2f, visualScale = 0.9f),
    DiaryStickerOption("music_note", R.drawable.sticker_music, "음표", 5f, visualScale = 0.8f),
    DiaryStickerOption("book_day", R.drawable.sticker_book, "책", -5f, visualScale = 0.92f),
    DiaryStickerOption("flower_memo", R.drawable.sticker_flower, "꽃", 2f, visualScale = 0.92f),
    DiaryStickerOption("soft_bear", R.drawable.sticker_bear, "곰", -3f, visualScale = 0.94f),
    DiaryStickerOption("pink_pig", R.drawable.sticker_pig, "돼지", 3f, visualScale = 0.94f),
    DiaryStickerOption("white_cat", R.drawable.sticker_cat, "고양이", -2f, visualScale = 1.08f),
    DiaryStickerOption("blue_whale", R.drawable.sticker_whale, "고래", 4f, visualScale = 0.94f),
    DiaryStickerOption("tulip_bloom", R.drawable.sticker_tulip, "튤립", -2f, visualScale = 0.84f),
    DiaryStickerOption("watermelon_slice", R.drawable.sticker_watermelon, "수박", 2f),
    DiaryStickerOption("red_apple", R.drawable.sticker_apple, "사과", -3f),
    DiaryStickerOption("sweet_cherries", R.drawable.sticker_cherries, "체리", 3f),
    DiaryStickerOption("tomato_pop", R.drawable.sticker_tomato, "토마토", -2f),
    DiaryStickerOption("sushi_bite", R.drawable.sticker_sushi, "초밥", 2f),
    DiaryStickerOption("beer_cheers", R.drawable.sticker_beer, "맥주", -3f),
    DiaryStickerOption("milk_pudding", R.drawable.sticker_pudding, "푸딩", 3f),
    DiaryStickerOption("soft_icecream", R.drawable.sticker_icecream, "아이스크림", -4f),
    DiaryStickerOption("party_cake", R.drawable.sticker_cake, "케이크", 2f),
    DiaryStickerOption("pink_donut", R.drawable.sticker_donut, "도넛", -3f)
)

private val stickerPaletteKeys = listOf(
    "tape_heart",
    "quiet_moon",
    "coffee_break",
    "music_note",
    "tulip_bloom",
    "white_cat"
)

private val stickerPaletteOptions = stickerPaletteKeys.mapNotNull { key ->
    allStickerOptions.firstOrNull { it.key == key }
}

private fun Dp.scaledBy(scale: Float): Dp = this * scale

private fun rotateOffset(offset: Offset, degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    val cosTheta = cos(radians).toFloat()
    val sinTheta = sin(radians).toFloat()
    return Offset(
        x = offset.x * cosTheta - offset.y * sinTheta,
        y = offset.x * sinTheta + offset.y * cosTheta
    )
}

private fun angleDeltaDegrees(from: Float, to: Float): Float {
    var delta = Math.toDegrees((to - from).toDouble()).toFloat()
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f
    return delta
}

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
    return toStickerPayloadData().placements
}

fun String.toStickerBodyMinHeightDp(): Float? {
    return toStickerPayloadData().bodyMinHeightDp
}

private fun String.toStickerPayloadData(): StickerPayloadData {
    if (isBlank()) return StickerPayloadData(emptyList())
    return runCatching {
        when {
            trim().startsWith("[") -> {
                StickerPayloadData(
                    placements = JSONArray(this).toStickerPlacementsList()
                )
            }

            trim().startsWith("{") -> {
                val payload = JSONObject(this)
                val placementsArray = payload.optJSONArray("placements")
                StickerPayloadData(
                    placements = placementsArray?.toStickerPlacementsList().orEmpty(),
                    bodyMinHeightDp = payload.optDouble("bodyMinHeightDp").takeIf { !it.isNaN() }?.toFloat()
                )
            }

            else -> {
                StickerPayloadData(
                    placements = listOf(
                        DiaryStickerPlacement(
                            key = normalizeStickerKey(),
                            xRatio = 0.14f,
                            yRatio = 0.16f
                        )
                    )
                )
            }
        }
    }.getOrElse { StickerPayloadData(emptyList()) }
}

private fun JSONArray.toStickerPlacementsList(): List<DiaryStickerPlacement> {
    return buildList {
        repeat(length()) { index ->
            val item = optJSONObject(index) ?: return@repeat
            add(
                DiaryStickerPlacement(
                    key = item.optString("key").normalizeStickerKey(),
                    xRatio = item.optDouble("x", 0.15).toFloat().coerceIn(0f, 1f),
                    yRatio = item.optDouble("y", 0.16).toFloat().coerceIn(0f, 1f),
                    scale = item.optDouble("scale", 1.0).toFloat().coerceIn(MIN_STICKER_SCALE, MAX_STICKER_SCALE),
                    rotation = item.opt("rotation")
                        .takeUnless { it == null || it == JSONObject.NULL }
                        ?.toString()
                        ?.toFloatOrNull()
                )
            )
        }
    }.filter { it.key.toStickerOptionOrNull() != null }
}

fun List<DiaryStickerPlacement>.toStickerPayload(bodyMinHeightDp: Float? = null): String {
    if (isEmpty()) return ""
    val placementsArray = JSONArray().apply {
        forEach { placement ->
            put(
                JSONObject()
                    .put("key", placement.key)
                    .put("x", placement.xRatio.toDouble())
                    .put("y", placement.yRatio.toDouble())
                    .put("scale", placement.scale.toDouble())
                    .put("rotation", placement.rotation?.toDouble() ?: JSONObject.NULL)
            )
        }
    }
    return JSONObject().apply {
        put("placements", placementsArray)
        bodyMinHeightDp?.let { put("bodyMinHeightDp", it.toDouble()) }
    }.toString()
}

fun String.toStickerOptionOrNull(): DiaryStickerOption? {
    if (isBlank()) return null
    return allStickerOptions.firstOrNull { it.key == this }
}

fun nextStickerPlacement(
    key: String,
    existingCount: Int,
    scrollOffsetPx: Int = 0,
    viewportHeightPx: Int = 0,
    canvasHeightPx: Int = 0
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
    if (canvasHeightPx <= STICKER_NODE_HEIGHT_PX || viewportHeightPx <= 0) {
        return DiaryStickerPlacement(key = key, xRatio = seed.x, yRatio = seed.y)
    }

    val verticalRange = (canvasHeightPx - STICKER_NODE_HEIGHT_PX).coerceAtLeast(1f)
    val visibleHeight = (viewportHeightPx.toFloat() - STICKER_NODE_HEIGHT_PX)
        .coerceAtLeast(0f)
        .coerceAtMost(verticalRange)
    val anchoredY = (scrollOffsetPx.toFloat() + visibleHeight * seed.y).coerceIn(0f, verticalRange)

    return DiaryStickerPlacement(
        key = key,
        xRatio = seed.x,
        yRatio = (anchoredY / verticalRange).coerceIn(0f, 1f)
    )
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
        stickerPaletteOptions.forEach { option ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .alpha(if (canAddMore) 1f else 0.45f)
                        .clickable(enabled = canAddMore) { onAddSticker(option.key) },
                    contentAlignment = Alignment.Center
                ) {
                    StickerImage(
                        option = option,
                        modifier = Modifier.size(44.dp.scaledBy(option.visualScale))
                    )
                }
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
    onTransformSticker: (index: Int, xRatio: Float, yRatio: Float, scale: Float, rotation: Float) -> Unit,
    onRemoveSticker: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    DiaryStickerCanvas(
        placements = placements,
        titlePreview = titlePreview,
        contentPreview = contentPreview,
        onMoveSticker = onMoveSticker,
        onTransformSticker = onTransformSticker,
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
        onTransformSticker = null,
        onRemoveSticker = null,
        modifier = modifier
    )
}

@Composable
fun DiaryStickerWritingSurfaceEditor(
    placements: List<DiaryStickerPlacement>,
    onMoveSticker: (index: Int, xRatio: Float, yRatio: Float) -> Unit,
    onTransformSticker: (index: Int, xRatio: Float, yRatio: Float, scale: Float, rotation: Float) -> Unit,
    onRemoveSticker: (index: Int) -> Unit,
    onCanvasSizeChanged: ((IntSize) -> Unit)? = null,
    contentHorizontalPadding: Dp = DiaryPageHorizontalPadding,
    contentVerticalPadding: Dp = DiaryPageVerticalPadding,
    surfaceMinHeight: Dp = DiaryPageMinHeight,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    flat: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit
) {
    DiaryStickerWritingSurface(
        placements = placements,
        onMoveSticker = onMoveSticker,
        onTransformSticker = onTransformSticker,
        onRemoveSticker = onRemoveSticker,
        onCanvasSizeChanged = onCanvasSizeChanged,
        contentHorizontalPadding = contentHorizontalPadding,
        contentVerticalPadding = contentVerticalPadding,
        surfaceMinHeight = surfaceMinHeight,
        surfaceColor = surfaceColor,
        flat = flat,
        modifier = modifier,
        content = content
    )
}

@Composable
fun DiaryStickerWritingSurfaceReadOnly(
    placements: List<DiaryStickerPlacement>,
    contentHorizontalPadding: Dp = DiaryPageHorizontalPadding,
    contentVerticalPadding: Dp = DiaryPageVerticalPadding,
    surfaceMinHeight: Dp = DiaryPageMinHeight,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    flat: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit
) {
    DiaryStickerWritingSurface(
        placements = placements,
        onMoveSticker = null,
        onTransformSticker = null,
        onRemoveSticker = null,
        contentHorizontalPadding = contentHorizontalPadding,
        contentVerticalPadding = contentVerticalPadding,
        surfaceMinHeight = surfaceMinHeight,
        surfaceColor = surfaceColor,
        flat = flat,
        modifier = modifier,
        content = content
    )
}

@Composable
fun DiaryStickerOverlayReadOnly(
    placements: List<DiaryStickerPlacement>,
    stickerWidthPx: Float = 104f,
    stickerHeightPx: Float = 44f,
    stickerVisualSize: Dp = 44.dp,
    stickerImageSize: Dp = 30.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

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
                stickerVisualSize = stickerVisualSize,
                stickerImageSize = stickerImageSize,
                editable = false,
                onMoveSticker = null,
                onTransformSticker = null,
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
    onTransformSticker: ((index: Int, xRatio: Float, yRatio: Float, scale: Float, rotation: Float) -> Unit)?,
    onRemoveSticker: ((index: Int) -> Unit)?,
    modifier: Modifier = Modifier
) {
    DiaryStickerWritingSurface(
        placements = placements,
        onMoveSticker = onMoveSticker,
        onTransformSticker = onTransformSticker,
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
    onTransformSticker: ((index: Int, xRatio: Float, yRatio: Float, scale: Float, rotation: Float) -> Unit)?,
    onRemoveSticker: ((index: Int) -> Unit)?,
    onCanvasSizeChanged: ((IntSize) -> Unit)? = null,
    contentHorizontalPadding: Dp = DiaryPageHorizontalPadding,
    contentVerticalPadding: Dp = DiaryPageVerticalPadding,
    surfaceMinHeight: Dp = DiaryPageMinHeight,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    flat: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit
) {
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val stickerWidthPx = 104f
    val stickerHeightPx = 44f
    val editable = onMoveSticker != null
    val canvasSize = remember(contentSize, containerSize) {
        IntSize(
            width = max(contentSize.width, containerSize.width),
            height = max(contentSize.height, containerSize.height)
        )
    }

    LaunchedEffect(canvasSize) {
        if (canvasSize != IntSize.Zero) {
            onCanvasSizeChanged?.invoke(canvasSize)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = surfaceMinHeight),
        shape = RoundedCornerShape(if (flat) 0.dp else DiaryPageCornerRadius),
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = if (flat) 0.dp else 0.5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(surfaceColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        horizontal = contentHorizontalPadding,
                        vertical = contentVerticalPadding
                    )
            ) {
                content(
                    Modifier
                        .fillMaxWidth()
                        .onSizeChanged { measuredSize ->
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
                    .matchParentSize()
                    .padding(
                        horizontal = contentHorizontalPadding,
                        vertical = contentVerticalPadding
                    )
                    .onSizeChanged { containerSize = it }
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
                        onTransformSticker = onTransformSticker,
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
    stickerVisualSize: Dp = 44.dp,
    stickerImageSize: Dp = 30.dp,
    editable: Boolean,
    onMoveSticker: ((index: Int, xRatio: Float, yRatio: Float) -> Unit)?,
    onTransformSticker: ((index: Int, xRatio: Float, yRatio: Float, scale: Float, rotation: Float) -> Unit)?,
    onRemoveSticker: ((index: Int) -> Unit)?
) {
    val option = placement.key.toStickerOptionOrNull() ?: return
    val density = LocalDensity.current
    val baseSelectionBoxSizePx = with(density) { stickerVisualSize.toPx() }
    var topLeftPx by remember(index, canvasSize) { mutableStateOf(Offset.Zero) }
    var liveScale by remember(index) { mutableStateOf(placement.scale.coerceIn(MIN_STICKER_SCALE, MAX_STICKER_SCALE)) }
    var liveRotation by remember(index) { mutableStateOf(placement.rotation ?: option.rotation) }
    val latestPlacement by rememberUpdatedState(placement)

    LaunchedEffect(
        placement.xRatio,
        placement.yRatio,
        placement.scale,
        placement.rotation,
        canvasSize,
        option.rotation
    ) {
        val syncedScale = placement.scale.coerceIn(MIN_STICKER_SCALE, MAX_STICKER_SCALE)
        val syncedRotation = placement.rotation ?: option.rotation
        val syncedSelectionBoxSizePx = baseSelectionBoxSizePx * syncedScale
        val syncedHorizontalRange = (canvasSize.width - syncedSelectionBoxSizePx).coerceAtLeast(1f)
        val syncedVerticalRange = (canvasSize.height - syncedSelectionBoxSizePx).coerceAtLeast(1f)
        liveScale = syncedScale
        liveRotation = syncedRotation
        topLeftPx = Offset(
            x = syncedHorizontalRange * placement.xRatio,
            y = syncedVerticalRange * placement.yRatio
        )
    }

    val selectionBoxSizePx = baseSelectionBoxSizePx * liveScale
    val selectionBoxSize = stickerVisualSize.scaledBy(liveScale)
    val horizontalRange = (canvasSize.width - selectionBoxSizePx).coerceAtLeast(1f)
    val verticalRange = (canvasSize.height - selectionBoxSizePx).coerceAtLeast(1f)
    val clampedTopLeftPx = Offset(
        x = topLeftPx.x.coerceIn(0f, horizontalRange),
        y = topLeftPx.y.coerceIn(0f, verticalRange)
    )
    val xOffset = clampedTopLeftPx.x.roundToInt()
    val yOffset = clampedTopLeftPx.y.roundToInt()
    val center = Offset(
        x = clampedTopLeftPx.x + selectionBoxSizePx / 2f,
        y = clampedTopLeftPx.y + selectionBoxSizePx / 2f
    )
    val cornerVector = Offset(selectionBoxSizePx / 2f, selectionBoxSizePx / 2f)
    val rotatedTopLeftCorner = center + rotateOffset(Offset(-cornerVector.x, -cornerVector.y), liveRotation)
    val rotatedBottomRightCorner = center + rotateOffset(cornerVector, liveRotation)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(xOffset, yOffset) }
                .size(selectionBoxSize)
                .graphicsLayer {
                    rotationZ = liveRotation
                    transformOrigin = TransformOrigin.Center
                }
                .then(
                    if (editable) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            StickerImage(
                option = option,
                modifier = Modifier.size(
                    stickerImageSize.scaledBy(option.visualScale * liveScale)
                )
            )
        }

        if (editable) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffset, yOffset) }
                    .size(selectionBoxSize)
                    .zIndex(2f)
                    .pointerInput(index, canvasSize) {
                        var dragStartTopLeft = Offset.Zero
                        detectDragGestures(
                            onDragStart = {
                                dragStartTopLeft = topLeftPx
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val newTopLeft = Offset(
                                x = (dragStartTopLeft.x + dragAmount.x).coerceIn(0f, horizontalRange),
                                y = (dragStartTopLeft.y + dragAmount.y).coerceIn(0f, verticalRange)
                            )
                            topLeftPx = newTopLeft
                            dragStartTopLeft = newTopLeft
                            onMoveSticker?.invoke(
                                index,
                                (newTopLeft.x / horizontalRange).coerceIn(0f, 1f),
                                (newTopLeft.y / verticalRange).coerceIn(0f, 1f)
                            )
                        }
                    }
            )

            Surface(
                modifier = Modifier.offset {
                    IntOffset(
                        (rotatedTopLeftCorner.x - 8f).roundToInt(),
                        (rotatedTopLeftCorner.y - 8f).roundToInt()
                    )
                }.zIndex(3f),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                tonalElevation = 0.dp,
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
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (rotatedBottomRightCorner.x - 14f).roundToInt(),
                            (rotatedBottomRightCorner.y - 14f).roundToInt()
                        )
                    }
                    .zIndex(3f)
                    .pointerInput(index, canvasSize) {
                        var startScale = 1f
                        var startRotation = 0f
                        var startTopLeft = Offset.Zero
                        var startCenter = Offset.Zero
                        var startVector = Offset.Zero
                        var accumulatedDrag = Offset.Zero

                        detectDragGestures(
                            onDragStart = {
                                startScale = liveScale
                                startRotation = liveRotation
                                accumulatedDrag = Offset.Zero
                                val startSelectionBoxSizePx = baseSelectionBoxSizePx * startScale
                                startTopLeft = topLeftPx
                                startCenter = startTopLeft + Offset(
                                    startSelectionBoxSizePx / 2f,
                                    startSelectionBoxSizePx / 2f
                                )
                                startVector = rotateOffset(
                                    offset = Offset(
                                        startSelectionBoxSizePx / 2f,
                                        startSelectionBoxSizePx / 2f
                                    ),
                                    degrees = startRotation
                                )
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += Offset(dragAmount.x, dragAmount.y)
                            val currentVector = startVector + accumulatedDrag
                            val startDistance = hypot(startVector.x, startVector.y).coerceAtLeast(1f)
                            val currentDistance = hypot(currentVector.x, currentVector.y).coerceAtLeast(1f)
                            val startAngle = atan2(startVector.y, startVector.x)
                            val currentAngle = atan2(currentVector.y, currentVector.x)
                            val newScale = (startScale * (currentDistance / startDistance))
                                .coerceIn(MIN_STICKER_SCALE, MAX_STICKER_SCALE)
                            val rotationDelta = angleDeltaDegrees(startAngle, currentAngle) *
                                STICKER_ROTATION_SENSITIVITY
                            val newRotation = startRotation + rotationDelta
                            val newSelectionBoxSizePx = baseSelectionBoxSizePx * newScale
                            val newHorizontalRange = (canvasSize.width - newSelectionBoxSizePx).coerceAtLeast(1f)
                            val newVerticalRange = (canvasSize.height - newSelectionBoxSizePx).coerceAtLeast(1f)
                            val newTopLeftX = (startCenter.x - newSelectionBoxSizePx / 2f)
                                .coerceIn(0f, newHorizontalRange)
                            val newTopLeftY = (startCenter.y - newSelectionBoxSizePx / 2f)
                                .coerceIn(0f, newVerticalRange)
                            val newTopLeft = Offset(newTopLeftX, newTopLeftY)
                            liveScale = newScale
                            liveRotation = newRotation
                            topLeftPx = newTopLeft

                            onTransformSticker?.invoke(
                                index,
                                (newTopLeftX / newHorizontalRange).coerceIn(0f, 1f),
                                (newTopLeftY / newVerticalRange).coerceIn(0f, 1f),
                                newScale,
                                newRotation
                            )
                        }
                    },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 0.dp,
                shadowElevation = 0.25.dp
            ) {
                Box(
                    modifier = Modifier.size(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sticker_transform),
                        contentDescription = "스티커 크기와 회전 조절",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StickerImage(
    option: DiaryStickerOption,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = option.imageResId),
        contentDescription = option.label,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

fun canAddMoreStickers(placements: List<DiaryStickerPlacement>): Boolean {
    return placements.size < MAX_STICKER_COUNT
}
