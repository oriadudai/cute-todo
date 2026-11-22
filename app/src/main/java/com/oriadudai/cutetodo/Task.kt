package com.oriadudai.cutetodo

data class Task(
    val id: Long,
    val title: String,
    val emoji: String,
    val colorIndex: Int,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val doneAt: Long = 0L,
)

object Palette {
    val EMOJIS = listOf("🌸", "⭐", "📚", "🛒", "💪", "💡", "🎀", "☕", "🧺", "💌", "🐣", "🍓")

    // Pastel card colors (app) – matching drawables bg_pill_0..4 are used in the widget
    val CARD_COLORS = intArrayOf(
        0xFFFFE3EC.toInt(), // pink
        0xFFEDE4FF.toInt(), // lavender
        0xFFDDF5EA.toInt(), // mint
        0xFFFFF1D6.toInt(), // peach
        0xFFDDEFFF.toInt(), // sky
    )

    val PILL_DRAWABLES = intArrayOf(
        R.drawable.bg_pill_0,
        R.drawable.bg_pill_1,
        R.drawable.bg_pill_2,
        R.drawable.bg_pill_3,
        R.drawable.bg_pill_4,
    )
}
