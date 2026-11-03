package com.kidslearning.chutki.data

import android.content.Context
import android.content.SharedPreferences
import com.kidslearning.chutki.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("chutki_kids_prefs", Context.MODE_PRIVATE)

    private val _userProfileState = MutableStateFlow(buildProfile())
    val userProfileState: StateFlow<UserProfile> = _userProfileState.asStateFlow()

    init {
        checkAndUpdateDailyStreak()
        _userProfileState.value = buildProfile()
    }

    /**
     * Automatic Daily Streak Tracking
     * Runs every time app opens:
     * - Same day: preserves streak
     * - Next day (consecutive): increments streak & updates highest streak
     * - Gap of > 1 day: resets current streak to 1
     */
    private fun checkAndUpdateDailyStreak() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        val lastActive = prefs.getString("last_active_date", "") ?: ""

        if (lastActive == today) {
            return
        }

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(cal.time)

        var currentStreak = prefs.getInt("current_streak", 1)
        var highestStreak = prefs.getInt("highest_streak", 1)

        if (lastActive == yesterday) {
            currentStreak += 1
            if (currentStreak > highestStreak) {
                highestStreak = currentStreak
            }
        } else if (lastActive.isNotEmpty()) {
            currentStreak = 1
        } else {
            currentStreak = 1
        }

        prefs.edit()
            .putString("last_active_date", today)
            .putInt("current_streak", currentStreak)
            .putInt("highest_streak", highestStreak)
            .apply()
    }

    private fun calculateRank(xp: Int): Int {
        return when {
            xp >= 620 -> 1 // Gold Champion
            xp >= 520 -> 2 // Silver
            xp >= 420 -> 3 // Bronze
            xp >= 310 -> 4
            xp >= 220 -> 5
            xp >= 120 -> 6
            xp >= 50 -> 7
            else -> 8
        }
    }

    private fun getCompletedSet(key: String): MutableSet<String> {
        val set = prefs.getStringSet(key, null)
        return set?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveCompletedSet(key: String, set: Set<String>) {
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun computeSubjectPercent(subjectId: String): Int {
        val sub = subjectId.lowercase()
        val cards = getCompletedSet("${sub}_cards_completed").size
        val tracing = getCompletedSet("${sub}_tracing_completed").size
        val words = getCompletedSet("${sub}_words_completed").size
        val quizzes = getCompletedSet("${sub}_quiz_completed").size

        val totalActivities = when (sub) {
            "hindi" -> 49 + 49 + 200 + 8
            "english" -> 26 + 26 + 200 + 8
            "math" -> 100 + 100 + 200 + 8
            else -> 100
        }

        val completed = cards + tracing + words + quizzes
        return ((completed.toFloat() / totalActivities.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    private fun buildProfile(): UserProfile {
        val xp = prefs.getInt("total_xp", 0)
        val stars = prefs.getInt("total_stars", 0)
        val streak = prefs.getInt("current_streak", 1)
        val highest = prefs.getInt("highest_streak", 1)

        val hindiPct = computeSubjectPercent("hindi")
        val englishPct = computeSubjectPercent("english")
        val mathPct = computeSubjectPercent("math")
        val overallPct = ((hindiPct + englishPct + mathPct) / 3).coerceIn(0, 100)

        val hindiCards = getCompletedSet("hindi_cards_completed").size
        val englishCards = getCompletedSet("english_cards_completed").size
        val mathCards = getCompletedSet("math_cards_completed").size
        val totalCards = hindiCards + englishCards + mathCards

        val hindiTracing = getCompletedSet("hindi_tracing_completed").size
        val englishTracing = getCompletedSet("english_tracing_completed").size
        val mathTracing = getCompletedSet("math_tracing_completed").size
        val totalTracing = hindiTracing + englishTracing + mathTracing

        val hindiWords = getCompletedSet("hindi_words_completed").size
        val englishWords = getCompletedSet("english_words_completed").size
        val mathWords = getCompletedSet("math_words_completed").size
        val totalWords = hindiWords + englishWords + mathWords

        val hindiQuiz = getCompletedSet("hindi_quiz_completed").size
        val englishQuiz = getCompletedSet("english_quiz_completed").size
        val mathQuiz = getCompletedSet("math_quiz_completed").size
        val totalQuiz = hindiQuiz + englishQuiz + mathQuiz

        val rank = calculateRank(xp)

        // Count unlocked badges
        var badges = 0
        if (xp > 0 || totalCards > 0) badges++
        if (totalTracing >= 3 || stars >= 9) badges++
        if (streak >= 3) badges++
        if (totalWords >= 2 || xp >= 200) badges++
        if (totalQuiz >= 3 || xp >= 350) badges++
        if (mathPct > 0 || xp >= 500) badges++
        if (hindiPct >= 20 || xp >= 600) badges++
        if (rank <= 3 && xp >= 700) badges++

        val lastType = prefs.getString("last_active_program_type", "LEVEL_1_CARDS") ?: "LEVEL_1_CARDS"
        val lastSubject = prefs.getString("last_active_subject", "hindi") ?: "hindi"
        val lastLevel = prefs.getInt("last_active_level", 1)
        val lastLabel = prefs.getString("last_active_item_label", "") ?: ""
        val lastGame = prefs.getString("last_active_game_key", null)

        return UserProfile(
            id = prefs.getString("user_id", "kid_default") ?: "kid_default",
            name = prefs.getString("kid_name", "आरव (नन्हे उस्ताद)") ?: "आरव (नन्हे उस्ताद)",
            age = prefs.getInt("kid_age", 5),
            avatarEmoji = prefs.getString("kid_avatar", "girl") ?: "girl",
            totalXp = xp,
            totalStars = stars,
            currentStreakDays = streak,
            highestStreak = highest,
            currentSubjectId = prefs.getString("current_subject", "hindi") ?: "hindi",
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            parentEmailOrPhone = prefs.getString("parent_contact", "") ?: "",
            isParentVerified = prefs.getBoolean("parent_verified", false),
            isDarkMode = prefs.getBoolean("dark_mode", false),
            soundEffectsEnabled = prefs.getBoolean("sound_effects", true),
            appLanguage = prefs.getString("app_language", "hi") ?: "hi",

            currentRank = rank,
            hindiProgressPercent = hindiPct,
            englishProgressPercent = englishPct,
            mathProgressPercent = mathPct,
            overallProgressPercent = overallPct,

            hindiUnlockedLevel = prefs.getInt("hindi_unlocked_level", 1),
            englishUnlockedLevel = prefs.getInt("english_unlocked_level", 1),
            mathUnlockedLevel = prefs.getInt("math_unlocked_level", 1),

            cardsLearnedCount = totalCards,
            lettersTracedCount = totalTracing,
            wordsBuiltCount = totalWords,
            quizzesSolvedCount = totalQuiz,
            unlockedBadgesCount = badges,

            lastActiveType = lastType,
            lastActiveSubjectId = lastSubject,
            lastActiveLevel = lastLevel,
            lastActiveItemLabel = lastLabel,
            lastActiveGameKey = lastGame
        )
    }

    fun addXpAndStars(xpToAdd: Int, starsToAdd: Int) {
        val current = _userProfileState.value
        val newXp = current.totalXp + xpToAdd
        val newStars = current.totalStars + starsToAdd

        prefs.edit()
            .putInt("total_xp", newXp)
            .putInt("total_stars", newStars)
            .apply()

        _userProfileState.value = buildProfile()
    }

    /**
     * Automatic card learning tracking:
     * - Records learned card
     * - Awards +10 XP on first learn
     * - Automatically unlocks Level 2 (Tracing) once 2+ cards are learned
     */
    fun recordCardLearned(subjectId: String, cardId: String) {
        val sub = subjectId.lowercase()
        val key = "${sub}_cards_completed"
        val set = getCompletedSet(key)
        val isFirstTime = set.add(cardId)

        if (isFirstTime) {
            saveCompletedSet(key, set)
            val currentUnlocked = prefs.getInt("${sub}_unlocked_level", 1)
            val newUnlocked = if (set.size >= 2) maxOf(currentUnlocked, 2) else currentUnlocked

            val currentXp = prefs.getInt("total_xp", 0) + 10
            val currentStars = prefs.getInt("total_stars", 0) + 1

            prefs.edit()
                .putInt("total_xp", currentXp)
                .putInt("total_stars", currentStars)
                .putInt("${sub}_unlocked_level", newUnlocked)
                .apply()

            _userProfileState.value = buildProfile()
        }
    }

    /**
     * Automatic Tracing letter completion:
     * - Records letter traced
     * - Awards +50 XP and +3 Stars
     * - Automatically unlocks Level 3 (Word Builder)
     */
    fun recordLetterTraced(subjectId: String, char: String) {
        val sub = subjectId.lowercase()
        val key = "${sub}_tracing_completed"
        val set = getCompletedSet(key)
        val isFirstTime = set.add(char)

        saveCompletedSet(key, set)

        val currentUnlocked = prefs.getInt("${sub}_unlocked_level", 1)
        val newUnlocked = maxOf(currentUnlocked, 3) // Tracing unlocks Level 3

        val xpBonus = if (isFirstTime) 50 else 20
        val starBonus = if (isFirstTime) 3 else 1

        val currentXp = prefs.getInt("total_xp", 0) + xpBonus
        val currentStars = prefs.getInt("total_stars", 0) + starBonus

        prefs.edit()
            .putInt("total_xp", currentXp)
            .putInt("total_stars", currentStars)
            .putInt("${sub}_unlocked_level", newUnlocked)
            .apply()

        _userProfileState.value = buildProfile()
    }

    /**
     * Automatic Word Builder completion:
     * - Records word blend
     * - Awards +30 XP and +2 Stars
     * - Automatically unlocks Level 4 (Quiz)
     */
    fun recordWordBuilt(subjectId: String, wordId: String) {
        val sub = subjectId.lowercase()
        val key = "${sub}_words_completed"
        val set = getCompletedSet(key)
        val isFirstTime = set.add(wordId)

        saveCompletedSet(key, set)

        val currentUnlocked = prefs.getInt("${sub}_unlocked_level", 1)
        val newUnlocked = maxOf(currentUnlocked, 4) // Word Builder unlocks Level 4 Quiz

        val xpBonus = if (isFirstTime) 30 else 15
        val starBonus = if (isFirstTime) 2 else 1

        val currentXp = prefs.getInt("total_xp", 0) + xpBonus
        val currentStars = prefs.getInt("total_stars", 0) + starBonus

        prefs.edit()
            .putInt("total_xp", currentXp)
            .putInt("total_stars", currentStars)
            .putInt("${sub}_unlocked_level", newUnlocked)
            .apply()

        _userProfileState.value = buildProfile()
    }

    /**
     * Automatic Quiz answered:
     * - Records quiz question solved
     * - Awards +20 XP and +1 Star
     * - If all subject questions solved, unlocks Level 5 (Mega Challenge)
     */
    fun recordQuizAnswered(subjectId: String, questionId: Int) {
        val sub = subjectId.lowercase()
        val key = "${sub}_quiz_completed"
        val set = getCompletedSet(key)
        val isFirstTime = set.add(questionId.toString())

        saveCompletedSet(key, set)

        val currentUnlocked = prefs.getInt("${sub}_unlocked_level", 1)
        val newUnlocked = if (set.size >= 4) maxOf(currentUnlocked, 5) else currentUnlocked

        val xpBonus = if (isFirstTime) 20 else 10
        val starBonus = 1

        val currentXp = prefs.getInt("total_xp", 0) + xpBonus
        val currentStars = prefs.getInt("total_stars", 0) + starBonus

        prefs.edit()
            .putInt("total_xp", currentXp)
            .putInt("total_stars", currentStars)
            .putInt("${sub}_unlocked_level", newUnlocked)
            .apply()

        _userProfileState.value = buildProfile()
    }

    fun unlockLevel(subjectId: String, levelNumber: Int) {
        val sub = subjectId.lowercase()
        val current = prefs.getInt("${sub}_unlocked_level", 1)
        if (levelNumber > current) {
            prefs.edit().putInt("${sub}_unlocked_level", levelNumber).apply()
            _userProfileState.value = buildProfile()
        }
    }

    fun getUnlockedLevelForSubject(subjectId: String): Int {
        val sub = subjectId.lowercase()
        return prefs.getInt("${sub}_unlocked_level", 1)
    }

    fun getLevelStars(subjectId: String, levelNumber: Int): Int {
        val sub = subjectId.lowercase()
        val unlocked = prefs.getInt("${sub}_unlocked_level", 1)
        if (levelNumber > unlocked) return 0

        return when (levelNumber) {
            1 -> {
                val count = getCompletedSet("${sub}_cards_completed").size
                if (count >= 10) 3 else if (count >= 3) 2 else 1
            }
            2 -> {
                val count = getCompletedSet("${sub}_tracing_completed").size
                if (count >= 5) 3 else if (count >= 1) 2 else 0
            }
            3 -> {
                val count = getCompletedSet("${sub}_words_completed").size
                if (count >= 4) 3 else if (count >= 1) 2 else 0
            }
            4 -> {
                val count = getCompletedSet("${sub}_quiz_completed").size
                if (count >= 4) 3 else if (count >= 2) 2 else 1
            }
            5 -> {
                if (unlocked >= 5) 3 else 0
            }
            else -> 0
        }
    }

    fun getLevelPercent(subjectId: String, levelNumber: Int): Int {
        val sub = subjectId.lowercase()
        return when (levelNumber) {
            1 -> {
                val max = when (sub) { "hindi" -> 49; "english" -> 26; else -> 100 }
                val count = getCompletedSet("${sub}_cards_completed").size
                val lastIdx = getLastCardIndex(sub) + 1
                val effective = maxOf(count, lastIdx)
                ((effective.toFloat() / max) * 100).toInt().coerceIn(0, 100)
            }
            2 -> {
                val max = when (sub) { "hindi" -> 49; "english" -> 26; else -> 100 }
                val count = getCompletedSet("${sub}_tracing_completed").size
                val lastIdx = getLastTracingIndex(sub) + 1
                val effective = maxOf(count, lastIdx)
                ((effective.toFloat() / max) * 100).toInt().coerceIn(0, 100)
            }
            3 -> {
                val count = getCompletedSet("${sub}_words_completed").size
                val lastIdx = getLastBlendIndex(sub) + 1
                val effective = maxOf(count, lastIdx)
                ((effective.toFloat() / 200) * 100).toInt().coerceIn(0, 100)
            }
            4 -> {
                val count = getCompletedSet("${sub}_quiz_completed").size
                ((count.toFloat() / 8) * 100).toInt().coerceIn(0, 100)
            }
            5 -> {
                val unlocked = prefs.getInt("${sub}_unlocked_level", 1)
                if (unlocked >= 5) 100 else 0
            }
            else -> 0
        }
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
        _userProfileState.value = _userProfileState.value.copy(isDarkMode = isDark)
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _userProfileState.value = buildProfile()
    }

    fun updateProfile(name: String, avatar: String, age: Int) {
        prefs.edit()
            .putString("kid_name", name)
            .putString("kid_avatar", avatar)
            .putInt("kid_age", age)
            .apply()

        _userProfileState.value = buildProfile()
    }

    fun setParentLogin(contact: String, verified: Boolean) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("parent_contact", contact)
            .putBoolean("parent_verified", verified)
            .apply()

        _userProfileState.value = buildProfile()
    }

    fun logoutParent() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("parent_contact", "")
            .putBoolean("parent_verified", false)
            .apply()

        _userProfileState.value = buildProfile()
    }

    fun setCurrentSubject(subjectId: String) {
        prefs.edit().putString("current_subject", subjectId).apply()
        _userProfileState.value = _userProfileState.value.copy(currentSubjectId = subjectId)
    }

    fun resetProgress() {
        prefs.edit()
            .putInt("total_xp", 0)
            .putInt("total_stars", 0)
            .putInt("current_streak", 1)
            .putInt("hindi_unlocked_level", 1)
            .putInt("english_unlocked_level", 1)
            .putInt("math_unlocked_level", 1)
            .remove("hindi_cards_completed")
            .remove("hindi_tracing_completed")
            .remove("hindi_words_completed")
            .remove("hindi_quiz_completed")
            .remove("english_cards_completed")
            .remove("english_tracing_completed")
            .remove("english_words_completed")
            .remove("english_quiz_completed")
            .remove("math_cards_completed")
            .remove("math_tracing_completed")
            .remove("math_words_completed")
            .remove("math_quiz_completed")
            .apply()

        _userProfileState.value = buildProfile()
    }

    // =========================================================================
    // 1. EXACT LEARNING ITEM PROGRESS (Per Subject & Level)
    // =========================================================================

    // Level 1: Last Card Index per subject
    fun getLastCardIndex(subjectId: String): Int {
        return prefs.getInt("last_card_index_${subjectId.lowercase()}", 0)
    }

    fun setLastCardIndex(subjectId: String, index: Int, cardLabel: String = "") {
        val sub = subjectId.lowercase()
        prefs.edit()
            .putInt("last_card_index_$sub", index)
            .putString("last_active_program_type", "LEVEL_1_CARDS")
            .putString("last_active_subject", sub)
            .putInt("last_active_level", 1)
            .putInt("last_active_item_index", index)
            .putString("last_active_item_label", cardLabel)
            .apply()
        _userProfileState.value = buildProfile()
    }

    // Level 2: Last Tracing Letter Index per subject
    fun getLastTracingIndex(subjectId: String): Int {
        return prefs.getInt("last_tracing_index_${subjectId.lowercase()}", 0)
    }

    fun setLastTracingIndex(subjectId: String, index: Int, char: String = "") {
        val sub = subjectId.lowercase()
        prefs.edit()
            .putInt("last_tracing_index_$sub", index)
            .putString("last_tracing_char_$sub", char)
            .putString("last_active_program_type", "LEVEL_2_TRACING")
            .putString("last_active_subject", sub)
            .putInt("last_active_level", 2)
            .putInt("last_active_item_index", index)
            .putString("last_active_item_label", char)
            .apply()
        _userProfileState.value = buildProfile()
    }

    // Level 3: Last Word Blend Index per subject (out of 200 words!)
    fun getLastBlendIndex(subjectId: String): Int {
        return prefs.getInt("last_blend_index_${subjectId.lowercase()}", 0)
    }

    fun setLastBlendIndex(subjectId: String, index: Int, word: String = "") {
        val sub = subjectId.lowercase()
        prefs.edit()
            .putInt("last_blend_index_$sub", index)
            .putString("last_blend_word_$sub", word)
            .putString("last_active_program_type", "LEVEL_3_WORD_BUILDER")
            .putString("last_active_subject", sub)
            .putInt("last_active_level", 3)
            .putInt("last_active_item_index", index)
            .putString("last_active_item_label", word)
            .apply()
        _userProfileState.value = buildProfile()
    }

    // Level 4: Last Quiz Question Index per subject
    fun getLastQuizIndex(subjectId: String): Int {
        return prefs.getInt("last_quiz_index_${subjectId.lowercase()}", 0)
    }

    fun setLastQuizIndex(subjectId: String, index: Int, questionText: String = "") {
        val sub = subjectId.lowercase()
        prefs.edit()
            .putInt("last_quiz_index_$sub", index)
            .putString("last_quiz_question_$sub", questionText)
            .putString("last_active_program_type", "LEVEL_4_QUIZ")
            .putString("last_active_subject", sub)
            .putInt("last_active_level", 4)
            .putInt("last_active_item_index", index)
            .putString("last_active_item_label", questionText.take(24))
            .apply()
        _userProfileState.value = buildProfile()
    }

    // =========================================================================
    // 2. GAMING PROGRESS (All Games)
    // =========================================================================

    fun getGameProgress(gameKey: String): Pair<Int, Int> {
        val key = gameKey.lowercase()
        val level = prefs.getInt("game_level_$key", 1)
        val score = prefs.getInt("game_score_$key", 0)
        return Pair(level, score)
    }

    fun saveGameProgress(gameKey: String, level: Int, score: Int, roundCount: Int = 1) {
        val key = gameKey.lowercase()
        prefs.edit()
            .putInt("game_level_$key", level)
            .putInt("game_score_$key", score)
            .putInt("game_round_$key", roundCount)
            .putString("last_played_game", gameKey)
            .putString("last_active_program_type", "GAME")
            .putString("last_active_game_key", gameKey)
            .putString("last_active_item_label", "Level $level • Score $score")
            .apply()
        _userProfileState.value = buildProfile()
    }

    fun getLastPlayedGame(): String? {
        return prefs.getString("last_played_game", null)
    }

    fun setLastPlayedGame(gameKey: String) {
        prefs.edit()
            .putString("last_played_game", gameKey)
            .putString("last_active_program_type", "GAME")
            .putString("last_active_game_key", gameKey)
            .apply()
        _userProfileState.value = buildProfile()
    }

    fun resetGameProgress(gameKey: String) {
        val key = gameKey.lowercase()
        prefs.edit()
            .remove("game_level_$key")
            .remove("game_score_$key")
            .remove("game_round_$key")
            .apply()
        _userProfileState.value = buildProfile()
    }

    fun resetLevelProgress(subjectId: String, levelNumber: Int) {
        val sub = subjectId.lowercase()
        val editor = prefs.edit()
        when (levelNumber) {
            1 -> editor.remove("last_card_index_$sub")
            2 -> {
                editor.remove("last_tracing_index_$sub")
                editor.remove("last_tracing_char_$sub")
            }
            3 -> {
                editor.remove("last_blend_index_$sub")
                editor.remove("last_blend_word_$sub")
            }
        }
        editor.apply()
        _userProfileState.value = buildProfile()
    }

    data class LastActiveProgram(
        val type: String, // "LEVEL_1_CARDS", "LEVEL_2_TRACING", "LEVEL_3_WORD_BUILDER", "GAME", "QUIZ"
        val subjectId: String,
        val level: Int,
        val itemIndex: Int,
        val itemLabel: String,
        val gameKey: String?
    )

    fun getLastActiveProgram(): LastActiveProgram {
        val type = prefs.getString("last_active_program_type", "LEVEL_1_CARDS") ?: "LEVEL_1_CARDS"
        val subject = prefs.getString("last_active_subject", "hindi") ?: "hindi"
        val level = prefs.getInt("last_active_level", 1)
        val itemIndex = prefs.getInt("last_active_item_index", 0)
        val itemLabel = prefs.getString("last_active_item_label", "") ?: ""
        val gameKey = prefs.getString("last_active_game_key", null)
        return LastActiveProgram(type, subject, level, itemIndex, itemLabel, gameKey)
    }
}
