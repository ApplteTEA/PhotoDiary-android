package com.example.photodiary

data class DiaryOption(
    val key: String,
    val label: String
)

val moodOptions = listOf(
    DiaryOption("happy", "😊 행복"),
    DiaryOption("calm", "😌 평온"),
    DiaryOption("excited", "✨ 설렘"),
    DiaryOption("tired", "🥱 피곤"),
    DiaryOption("down", "😔 우울"),
    DiaryOption("energetic", "🤩 신남"),
    DiaryOption("moved", "🥹 감동"),
    DiaryOption("neutral", "🙂 무난")
)

val weatherOptions = listOf(
    DiaryOption("sunny", "☀️ 맑음"),
    DiaryOption("cloudy", "☁️ 흐림"),
    DiaryOption("rain", "🌧 비"),
    DiaryOption("snow", "❄️ 눈"),
    DiaryOption("windy", "🌬 바람"),
    DiaryOption("hot", "🔥 더움")
)

fun DiaryEntry.toMetaLine(): String = listOfNotNull(
    mood.toMetaLabelOrNull(moodOptions),
    weather.toMetaLabelOrNull(weatherOptions),
    tag.takeIf { it.isNotBlank() }?.toMetaTagDisplay()
).joinToString(" · ")

fun String.toMetaLabelOrNull(options: List<DiaryOption>): String? {
    if (isBlank()) return null
    return options.firstOrNull { it.key == this }?.label
}

private fun String.toMetaTagDisplay(): String {
    return trim()
        .replace("#", " ")
        .split(Regex("\\s+"))
        .map { it.trim().trim(',') }
        .filter { it.isNotBlank() }
        .joinToString(" ") { "#$it" }
}
