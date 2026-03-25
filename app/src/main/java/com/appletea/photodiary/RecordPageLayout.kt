package com.appletea.photodiary

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
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
