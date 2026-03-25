package com.appletea.photodiary

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReflectionScreen(
    monthKey: String,
    entriesCount: Int,
    imagePaths: List<String>,
    moodSummary: List<String>,
    weatherSummary: List<String>,
    tagSummary: List<String>,
    initialCoverImagePath: String,
    initialReflectionText: String,
    onBackClick: () -> Unit,
    onSaveClick: (coverImagePath: String, reflectionText: String) -> Unit
) {
    val initialSelectedCoverImagePath = remember(initialCoverImagePath, imagePaths) {
        when {
            initialCoverImagePath.isNotBlank() -> initialCoverImagePath
            imagePaths.size == 1 -> imagePaths.first()
            else -> ""
        }
    }

    var selectedCoverImagePath by remember(initialSelectedCoverImagePath) {
        mutableStateOf(initialSelectedCoverImagePath)
    }
    var reflectionText by remember(initialReflectionText) {
        mutableStateOf(initialReflectionText)
    }
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val normalizedReflectionText = remember(reflectionText) {
        reflectionText.trim()
    }
    val hasChanges = remember(
        selectedCoverImagePath,
        normalizedReflectionText,
        initialSelectedCoverImagePath,
        initialReflectionText
    ) {
        selectedCoverImagePath != initialSelectedCoverImagePath ||
            normalizedReflectionText != initialReflectionText
    }
    val hasSelectedContent = remember(selectedCoverImagePath, normalizedReflectionText) {
        selectedCoverImagePath.isNotBlank() || normalizedReflectionText.isNotBlank()
    }
    val hasSelectableImages = imagePaths.isNotEmpty()
    val hasExistingReflection = remember(initialSelectedCoverImagePath, initialReflectionText) {
        initialSelectedCoverImagePath.isNotBlank() || initialReflectionText.isNotBlank()
    }
    val attemptExit = {
        if (isSaving) {
            Unit
        } else if (hasChanges) {
            showExitConfirmDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(onBack = attemptExit)

    if (!previewImagePath.isNullOrBlank()) {
        Dialog(onDismissRequest = { previewImagePath = null }) {
            Surface(shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    AsyncImage(
                        model = Uri.parse(previewImagePath),
                        contentDescription = "대표 사진 크게 보기",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = screenHeight * 0.7f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
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
            title = { Text("작성 중인 회고를 나갈까요?") },
            text = { Text("저장하지 않으면 선택한 대표 사진과 회고 내용이 사라집니다.") },
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

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = monthKey.toMonthlyReflectionTitle(),
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
                            if (isSaving) return@TextButton
                            isSaving = true
                            showExitConfirmDialog = false
                            onSaveClick(
                                selectedCoverImagePath,
                                normalizedReflectionText
                            )
                        },
                        enabled = hasSelectedContent && !isSaving
                    ) {
                        Text(if (hasExistingReflection) "수정" else "저장")
                    }
                },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp, bottom = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (selectedCoverImagePath.isNotBlank()) 248.dp else 164.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .then(
                                if (selectedCoverImagePath.isNotBlank()) {
                                    Modifier.clickable { previewImagePath = selectedCoverImagePath }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        if (selectedCoverImagePath.isNotBlank()) {
                            AsyncImage(
                                model = Uri.parse(selectedCoverImagePath),
                                contentDescription = "대표 사진",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "대표 사진이 아직 없어요",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (hasSelectableImages) {
                                        "이번 달을 닮은 사진을 골라보세요."
                                    } else {
                                        "사진 없이도 한 줄 회고를 남길 수 있어요."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (imagePaths.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            imagePaths.forEach { imagePath ->
                                val isSelected = selectedCoverImagePath == imagePath
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
                                            } else {
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                            },
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable { selectedCoverImagePath = imagePath }
                                ) {
                                    AsyncImage(
                                        model = Uri.parse(imagePath),
                                        contentDescription = "월 회고 대표 사진",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    ReflectionSectionHeader(
                        title = "한 줄 회고",
                        subtitle = null
                    )
                    OutlinedTextField(
                        value = reflectionText,
                        onValueChange = { reflectionText = it.take(120) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        minLines = 3,
                        placeholder = {
                            Text("예: 마음이 자주 바빠도, 소중한 장면은 분명히 남아 있던 달")
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text("${reflectionText.length}/120")
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.03f)
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            }

            val summaryLines = buildList {
                if (moodSummary.isNotEmpty()) add("자주 남긴 분위기 ${moodSummary.joinToString(", ")}")
                if (weatherSummary.isNotEmpty()) add("기억에 남은 날씨 ${weatherSummary.joinToString(", ")}")
                if (tagSummary.isNotEmpty()) add("기억에 남은 태그 ${tagSummary.joinToString(" ")}")
            }
            if (summaryLines.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "이번 달의 조각들",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f)
                    )
                    summaryLines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReflectionSectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
        }
    }
}

private fun String.toMonthlyReflectionTitle(): String {
    val parts = split("-")
    if (parts.size != 2) return this
    val year = parts[0].toIntOrNull() ?: return this
    val month = parts[1].toIntOrNull() ?: return this
    return "${year}년 ${month}월"
}
