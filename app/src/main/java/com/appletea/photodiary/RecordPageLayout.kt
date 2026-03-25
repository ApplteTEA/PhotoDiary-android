package com.appletea.photodiary

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun rememberRecordPageScrollState(): ScrollState = rememberScrollState()

@Composable
fun RecordPageViewport(
    innerPadding: PaddingValues,
    onViewportSizeChanged: (IntSize) -> Unit,
    scrollState: ScrollState,
    bottomPadding: Dp = 0.dp,
    scrollContent: @Composable ColumnScope.() -> Unit,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(
                horizontal = RecordCanvasOuterHorizontalPadding,
                vertical = RecordCanvasOuterVerticalPadding
            )
            .onSizeChanged(onViewportSizeChanged)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = bottomPadding),
            content = scrollContent
        )

        overlayContent()
    }
}

@Composable
fun RecordPageSurfaceContent(
    contentModifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = contentModifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
fun RecordPageInfoHeader(
    diaryDate: String,
    weatherLabel: String?,
    moodLabel: String?,
    tagLabel: String? = null,
    onDateClick: (() -> Unit)? = null,
    onWeatherClick: (() -> Unit)? = null,
    onMoodClick: (() -> Unit)? = null,
    showEmptyMetaSlots: Boolean = true,
    modifier: Modifier = Modifier
) {
    val weatherMeta = weatherLabel.toMetaHeaderParts()
    val moodMeta = moodLabel.toMetaHeaderParts()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RecordTextInset),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaHeaderSlot(
                label = diaryDate,
                caption = null,
                selected = true,
                onClick = onDateClick,
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordPageMetaSlot(
                    meta = weatherMeta,
                    emptyCaption = "날씨",
                    selected = weatherLabel != null,
                    onClick = onWeatherClick,
                    showEmptySlot = showEmptyMetaSlots,
                    modifier = Modifier.widthIn(min = 48.dp)
                )

                RecordPageMetaSlot(
                    meta = moodMeta,
                    emptyCaption = "기분",
                    selected = moodLabel != null,
                    onClick = onMoodClick,
                    showEmptySlot = showEmptyMetaSlots,
                    modifier = Modifier.widthIn(min = 48.dp)
                )
            }
        }

        if (!tagLabel.isNullOrBlank()) {
            Text(
                text = tagLabel,
                modifier = Modifier.padding(horizontal = RecordTextInset),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun String.toTagMetaLabelOrNull(): String? {
    return trim()
        .takeIf { it.isNotBlank() }
        ?.split(Regex("\\s+"))
        ?.joinToString(" ") { token -> "#${token.trimStart('#')}" }
}

@Composable
private fun RecordPageMetaSlot(
    meta: Pair<String?, String>?,
    emptyCaption: String,
    selected: Boolean,
    onClick: (() -> Unit)?,
    showEmptySlot: Boolean,
    modifier: Modifier = Modifier
) {
    if (meta != null) {
        MetaHeaderSlot(
            label = meta.first ?: "",
            caption = meta.second,
            selected = selected,
            onClick = onClick,
            contentAlignment = Alignment.CenterEnd,
            modifier = modifier
        )
    } else if (showEmptySlot) {
        MetaHeaderSlot(
            label = "",
            caption = emptyCaption,
            selected = false,
            onClick = onClick,
            contentAlignment = Alignment.CenterEnd,
            modifier = modifier
        )
    }
}
