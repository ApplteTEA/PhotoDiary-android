package com.example.photodiary

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

data class DiaryStickerOption(
    val key: String,
    val emoji: String,
    val label: String
)

val stickerOptions = listOf(
    DiaryStickerOption("clover", "🍀", "작은 행운"),
    DiaryStickerOption("heart", "🤍", "마음 보관"),
    DiaryStickerOption("coffee", "☕", "커피 한 잔"),
    DiaryStickerOption("music", "🎵", "오늘의 플레이리스트"),
    DiaryStickerOption("star", "✨", "반짝 장면"),
    DiaryStickerOption("moon", "🌙", "조용한 밤")
)

fun String.toStickerOptionOrNull(): DiaryStickerOption? {
    if (isBlank()) return null
    return stickerOptions.firstOrNull { it.key == this }
}

@Composable
fun DiaryStickerBadge(
    stickerKey: String,
    modifier: Modifier = Modifier
) {
    val stickerOption = stickerKey.toStickerOptionOrNull() ?: return

    Surface(
        modifier = modifier.rotate(-3f),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
        tonalElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stickerOption.emoji,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = stickerOption.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun DiaryStickerChoiceChip(
    option: DiaryStickerOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                width = if (selected) 1.2.dp else 0.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        },
        tonalElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = option.emoji,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
