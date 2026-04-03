package com.play.emojireactionchain.model

import com.play.emojireactionchain.R

enum class GameRule(val ruleName: String, val hintRes: Int) {
    SEQUENTIAL("Sequential in Category", R.string.hint_sequential_category),
    MIX_UP("Category Mix-Up", R.string.hint_category_mixup),
    OPPOSITE("Opposite Meaning", R.string.hint_opposite_meaning),
    SYNONYM("Synonym Chain", R.string.hint_synonym_chain)
}

data class EmojiCategory(
    val name: String,
    val emojis: List<String>,
    val iconEmoji: String
)

object EmojiData {
    val categories: List<EmojiCategory> = listOf(
        EmojiCategory("Fruits", listOf("🍎", "🍌", "🍇", "🍓", "🍉", "🥝", "🍍", "🥭", "🍑", "🍒", "🍈", "🥥"), "🍎"),
        EmojiCategory("Animals", listOf("🐶", "🐱", "🐻", "🐼", "🐸", "🐒", "🦁", "🐯", "🦊", "🦝", "🐷", "🐮"), "🐶"),
        EmojiCategory("Faces", listOf("😀", "😊", "😂", "😎", "😍", "🤯", "🤨", "🤔", "🤩", "🥳", "😳", "🥺"), "😀"),
        EmojiCategory("Emotions", listOf("😀", "😢", "😊", "😠", "😂", "😥", "😨", "😰", "😱", "🥵", "🥶", "😳"), "🎭"),
        EmojiCategory("Vehicles", listOf("🚗", "🚕", "🚌", "🚑", "🚓", "🚒", "✈️", "🚀", "🚢", "⛵️", "🚁", "🚲"), "🚗"),
        EmojiCategory("Clothing", listOf("👕", "👚", "👗", "👖", "👔", "🧣", "🧤", "🧦", "🧢", "👒", "🎩", "👟"), "👕"),
        EmojiCategory("Sports", listOf("⚽️", "🏀", "🏈", "⚾️", "🎾", "🏐", "🏓", "🏸", "🏒", "🥍", "🏏", "⛳️"), "⚽️"),
        EmojiCategory("Food", listOf("🍰", "🎂", "🥨", "🥪", "🌮", "🍜", "🍕", "🍔", "🍟", "🍦", "🍩", "🍪"), "🍔"),
        EmojiCategory("Drinks", listOf("☕", "🍵", "🍶", "🍺", "🍷", "🍹", "🥛", "🧃", "🥤", "🧉", "🧊", "🫗"), "🥤"),
        EmojiCategory("Places", listOf("⛰️", "🏖️", "🏕️", "🗽", "🗼", "🕌", "⛩️", "🏞️", "🏟️", "🏛️", "🏘️", "🏙️"), "✈️"),
        EmojiCategory("Weather", listOf("⏰", "🗓️", "☀️", "🌧️", "❄️", "🌈", "🌪️", "⚡️", "☔️", "🌬️", "📅", "⏱️"), "☀️"),
        EmojiCategory("Household", listOf("🛋️", "🛏️", "🚪", "🪑", "💡", "🧸", "🪞", "🧽", "🪣", "🔑", "🖼️", "🚽"), "🏠"),
        EmojiCategory("Technology", listOf("📱", "💻", "⌨️", "🖱️", "🎧", "📺", "⌚️", "📷", "📹", "🕹️", "💾", "💽"), "💻"),
        EmojiCategory("Tools", listOf("🔨", "🔧", "🧰", "🧪", "🔬", "🔭", "🪛", "🪚", "🪓", "🪤", "🧲", "🔦"), "🛠️"),
        EmojiCategory("Music", listOf("🎵", "🎶", "🎤", "🎧", "🎼", "🎹", "🎸", "🎻", "🎺", "🥁", "🎷", "📻"), "🎵"),
        EmojiCategory("School", listOf("📚", "📓", "📐", "📏", "🖇️", "✏️", "📝", "📁", "📂", "📅", "📊", "📈"), "📚")
    )

    val oppositeEmojiMap = mapOf(
        "😀" to "😢", "😢" to "😀", "😊" to "😠", "😠" to "😊", "😂" to "😥", "😥" to "😂",
        "☀️" to "🌧️", "🌧️" to "☀️", "🔥" to "🧊", "🧊" to "🔥", "⬆️" to "⬇️", "⬇️" to "⬆️",
        "❤️" to "💔", "💔" to "❤️", "✅" to "❌", "❌" to "✅"
    )

    val synonymPairs = listOf(
        listOf("😀", "😊", "😄", "😁", "😆", "😅"),
        listOf("😢", "😥", "😓", "😔", "😟", "🙁"),
        listOf("😠", "😡", "😤", "🤬"),
        listOf("😨", "😱", "😰"),
        listOf("😴", "😪", "💤"),
        listOf("🚗", "🚕", "🚙", "🚓"),
        listOf("🏠", "🏡", "🏘️", "🏢"),
        listOf("☀️", "🌤️", "⛅️", "🔆"),
        listOf("🌧️", "☔️", "⛈️")
    )
}
