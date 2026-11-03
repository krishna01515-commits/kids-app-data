package com.kidslearning.chutki.model

data class UserProfile(
    val id: String = "guest_kid_1",
    val name: String = "आरव (नन्हे उस्ताद)",
    val age: Int = 5,
    val avatarEmoji: String = "🦁", // Lion, Girl 👧, Boy 👦, Astronaut 🚀, Kitten 🐱, Robot 🤖
    val totalXp: Int = 0,
    val totalStars: Int = 0,
    val currentStreakDays: Int = 1,
    val highestStreak: Int = 1,
    val currentSubjectId: String = "hindi",
    val isLoggedIn: Boolean = false,
    val parentEmailOrPhone: String = "",
    val isParentVerified: Boolean = false,
    val isDarkMode: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val speechPitch: Float = 1.15f, // Cheerful slightly high pitch for kids tutor
    val speechSpeed: Float = 0.88f, // Slightly slower, clear enunciation for young learners

    // --- Bilingual Language Support ("hi" or "en") ---
    val appLanguage: String = "hi",

    // --- Dynamic Gamification, Progress & Leaderboard Fields ---
    val currentRank: Int = 8,
    val hindiProgressPercent: Int = 0,
    val englishProgressPercent: Int = 0,
    val mathProgressPercent: Int = 0,
    val overallProgressPercent: Int = 0,

    val hindiUnlockedLevel: Int = 1,
    val englishUnlockedLevel: Int = 1,
    val mathUnlockedLevel: Int = 1,

    val cardsLearnedCount: Int = 0,
    val lettersTracedCount: Int = 0,
    val wordsBuiltCount: Int = 0,
    val quizzesSolvedCount: Int = 0,
    val unlockedBadgesCount: Int = 0,

    // --- Last Active Learning & Gaming Progress (For Auto-Resume) ---
    val lastActiveType: String = "LEVEL_1_CARDS", // "LEVEL_1_CARDS", "LEVEL_2_TRACING", "LEVEL_3_WORD_BUILDER", "GAME", "QUIZ"
    val lastActiveSubjectId: String = "hindi",
    val lastActiveLevel: Int = 1,
    val lastActiveItemLabel: String = "",
    val lastActiveGameKey: String? = null
) {
    val isEnglish: Boolean get() = appLanguage.equals("en", ignoreCase = true)
}
