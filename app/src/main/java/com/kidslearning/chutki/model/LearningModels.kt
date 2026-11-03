package com.kidslearning.chutki.model

enum class SubjectType(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val iconEmoji: String,
    val themeColorHex: Long
) {
    HINDI(
        id = "hindi",
        titleHindi = "हिंदी वर्णमाला",
        titleEnglish = "Hindi Letters & Words",
        iconEmoji = "🔤",
        themeColorHex = 0xFFFF7A00
    ),
    ENGLISH(
        id = "english",
        titleHindi = "अंग्रेजी ABC",
        titleEnglish = "English Phonics & Words",
        iconEmoji = "🅰️",
        themeColorHex = 0xFF00B0FF
    ),
    MATH(
        id = "math",
        titleHindi = "गणित 123",
        titleEnglish = "Math & Counting Fun",
        iconEmoji = "🔢",
        themeColorHex = 0xFF00E676
    )
}

data class LevelInfo(
    val levelNumber: Int,
    val titleHindi: String,
    val subtitleHindi: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val starsEarned: Int, // 0 to 3
    val xpReward: Int,
    val completionPercent: Int
)

data class AlphabetCard(
    val id: String,
    val char: String,
    val wordHindi: String,
    val wordEnglish: String,
    val emoji: String,
    val phonetic: String,
    val hindiAudioSentence: String,
    val funFactHindi: String
)

data class TracingLetter(
    val char: String,
    val subjectId: String,
    val titleHindi: String,
    val hintHindi: String,
    val guideDots: List<Pair<Float, Float>>, // normalized 0f to 1f coordinates
    val audioPromptHindi: String,
    val strokePaths: List<List<Pair<Float, Float>>> = emptyList()
)

data class WordBlend(
    val id: String,
    val subjectId: String,
    val char1: String,
    val char2: String,
    val operatorSymbol: String = "+",
    val resultWord: String,
    val meaningHindi: String,
    val emoji: String,
    val audioExplanationHindi: String
)

data class QuizQuestion(
    val id: Int,
    val subjectId: String,
    val questionHindi: String,
    val promptEmoji: String,
    val options: List<String>,
    val correctIndex: Int,
    val praiseHindi: String,
    val explanationHindi: String
)

data class Badge(
    val id: String,
    val titleHindi: String,
    val descriptionHindi: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val unlockedDate: String = ""
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val xp: Int,
    val streakDays: Int,
    val isCurrentUser: Boolean = false
)

