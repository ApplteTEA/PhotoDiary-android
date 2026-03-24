package com.appletea.photodiary

import org.json.JSONArray
import org.json.JSONObject

private const val DIARY_DOCUMENT_PREFIX = "__PDDOC_V1__"

sealed interface DiaryDocumentBlock {
    data class Text(val value: String) : DiaryDocumentBlock
    data class Image(val path: String) : DiaryDocumentBlock
}

fun parseDiaryDocument(content: String, legacyImagePaths: List<String>): List<DiaryDocumentBlock> {
    val parsed = if (content.startsWith(DIARY_DOCUMENT_PREFIX)) {
        runCatching {
            val json = JSONArray(content.removePrefix(DIARY_DOCUMENT_PREFIX))
            buildList {
                repeat(json.length()) { index ->
                    val item = json.optJSONObject(index) ?: return@repeat
                    when (item.optString("type")) {
                        "text" -> add(DiaryDocumentBlock.Text(item.optString("value")))
                        "image" -> item.optString("path")
                            .takeIf { it.isNotBlank() }
                            ?.let { add(DiaryDocumentBlock.Image(it)) }
                    }
                }
            }
        }.getOrDefault(emptyList())
    } else {
        buildList {
            add(DiaryDocumentBlock.Text(content))
            legacyImagePaths.forEach { path ->
                if (path.isNotBlank()) add(DiaryDocumentBlock.Image(path))
            }
        }
    }

    return parsed.normalizeDiaryDocument()
}

fun List<DiaryDocumentBlock>.toPersistedDiaryContent(): String {
    val json = JSONArray()
    normalizeDiaryDocument().forEach { block ->
        when (block) {
            is DiaryDocumentBlock.Text -> {
                json.put(
                    JSONObject()
                        .put("type", "text")
                        .put("value", block.value)
                )
            }

            is DiaryDocumentBlock.Image -> {
                json.put(
                    JSONObject()
                        .put("type", "image")
                        .put("path", block.path)
                )
            }
        }
    }
    return DIARY_DOCUMENT_PREFIX + json.toString()
}

fun List<DiaryDocumentBlock>.toDocumentImagePaths(): List<String> {
    return normalizeDiaryDocument().mapNotNull { block ->
        (block as? DiaryDocumentBlock.Image)?.path
    }
}

fun List<DiaryDocumentBlock>.toPlainTextPreview(): String {
    return normalizeDiaryDocument()
        .mapNotNull { block ->
            (block as? DiaryDocumentBlock.Text)?.value
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
        .joinToString("\n\n")
        .trim()
}

private fun List<DiaryDocumentBlock>.normalizeDiaryDocument(): List<DiaryDocumentBlock> {
    val normalized = mutableListOf<DiaryDocumentBlock>()

    forEach { block ->
        when (block) {
            is DiaryDocumentBlock.Text -> {
                val last = normalized.lastOrNull()
                if (last is DiaryDocumentBlock.Text) {
                    normalized[normalized.lastIndex] = DiaryDocumentBlock.Text(last.value + block.value)
                } else {
                    normalized.add(block)
                }
            }

            is DiaryDocumentBlock.Image -> normalized.add(block)
        }
    }

    if (normalized.isEmpty()) {
        normalized.add(DiaryDocumentBlock.Text(""))
    }

    if (normalized.none { it is DiaryDocumentBlock.Text }) {
        normalized.add(DiaryDocumentBlock.Text(""))
    }

    return normalized
}
