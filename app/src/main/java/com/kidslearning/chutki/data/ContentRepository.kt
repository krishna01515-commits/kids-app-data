package com.kidslearning.chutki.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.kidslearning.chutki.model.AlphabetCard
import com.kidslearning.chutki.model.GameContent
import com.kidslearning.chutki.model.QuizQuestion
import com.kidslearning.chutki.model.WordBlend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

// Typealias for convenient import from either data or model package
typealias GameContent = com.kidslearning.chutki.model.GameContent

/**
 * Data structures for dynamically generated rounds from ContentRepository
 */
data class TracingRoundData(
    val targetItem: GameContent,
    val charToTrace: String,
    val nameHindi: String,
    val nameEnglish: String,
    val emoji: String,
    val promptHindi: String,
    val promptEnglish: String
)

data class BalloonRoundData(
    val targetItem: GameContent,
    val targetChar: String,
    val soundPrompt: String,
    val hintHindi: String,
    val hintEnglish: String,
    val options: List<String>
)

data class WordMatchRoundData(
    val targetItem: GameContent,
    val emoji: String,
    val correctWord: String,
    val hintPrompt: String,
    val options: List<String>
)

data class CountCollectRoundData(
    val targetItem: GameContent,
    val emoji: String,
    val nameHindi: String,
    val nameEnglish: String,
    val count: Int,
    val options: List<Int>
)

/**
 * Master Data Class representing the complete remote app structure:
 * 1. game_items (20 bilingual fruits, animals, numbers)
 * 2. alphabet_cards (Hindi vowels/consonants, English A-Z, Math 1-100)
 * 3. word_blends (Hindi, English, Math word builder blends)
 * 4. quiz_questions (Quizzes and riddles)
 */
data class MasterAppContent(
    val game_items: List<GameContent>? = null,
    val alphabet_cards: Map<String, List<AlphabetCard>>? = null,
    val word_blends: Map<String, List<WordBlend>>? = null,
    val quiz_questions: Map<String, List<QuizQuestion>>? = null
)

/**
 * Singleton repository responsible for managing bilingual learning content.
 *
 * Capabilities:
 * 1. Asynchronously syncs dynamic learning content from remote GitHub raw JSON URL.
 * 2. Caches fetched data in SharedPreferences under "cached_game_data".
 * 3. Fallbacks gracefully to cached data or local `assets/game_content.json` when offline.
 * 4. Exposes query methods to retrieve items/questions filtered by difficulty level.
 */
object ContentRepository {

    private const val TAG = "ContentRepository"

    const val RAW_CONTENT_URL =
        "https://raw.githubusercontent.com/krishna01515-commits/kids-app-data/main/game_content.json"

    const val PREFS_NAME = "cached_game_data"
    const val KEY_CACHED_GAME_DATA = "cached_game_data"
    const val ASSET_FILE_NAME = "game_content.json"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val gson: Gson by lazy {
        GsonBuilder().create()
    }

    private var appContext: Context? = null

    @Volatile
    private var inMemoryItems: List<GameContent> = emptyList()

    @Volatile
    private var inMemoryAlphabetCards: Map<String, List<AlphabetCard>> = emptyMap()

    @Volatile
    private var inMemoryWordBlends: Map<String, List<WordBlend>> = emptyMap()

    @Volatile
    private var inMemoryQuizQuestions: Map<String, List<QuizQuestion>> = emptyMap()

    private val _contentFlow = MutableStateFlow<List<GameContent>>(emptyList())
    val contentFlow: StateFlow<List<GameContent>> = _contentFlow.asStateFlow()

    /**
     * Initializes the repository with Application Context, loads initial local/cached
     * content immediately, and begins asynchronous network synchronization.
     */
    fun initialize(context: Context) {
        val resolved = context.applicationContext ?: context
        appContext = resolved

        // 1. Immediately load available local data (Cache -> Assets)
        loadCachedOrAssetFallback(resolved)

        // 2. Perform background sync from remote raw URL
        fetchContentAsync(resolved)
    }

    /**
     * Asynchronously fetches JSON from raw URL using OkHttp enqueue callback.
     * Caches successful responses in SharedPreferences ("cached_game_data").
     * Falls back to cache or assets/game_content.json if network is unavailable.
     */
    fun fetchContentAsync(
        context: Context? = null,
        onComplete: ((items: List<GameContent>, isFromNetwork: Boolean) -> Unit)? = null
    ) {
        val resolved = resolveContext(context)

        val request = Request.Builder()
            .url(RAW_CONTENT_URL)
            .header("Cache-Control", "no-cache")
            .get()
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "Network fetch failed (${e.message}). Falling back to cache/assets.", e)
                val fallbackItems = loadCachedOrAssetFallback(resolved)
                onComplete?.invoke(fallbackItems, false)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val jsonString = resp.body?.string()
                        if (!jsonString.isNullOrBlank()) {
                            val parsedItems = parseJson(jsonString)
                            if (parsedItems.isNotEmpty()) {
                                if (resolved != null) {
                                    saveToCache(resolved, jsonString)
                                }
                                updateInMemory(parsedItems)
                                Log.i(TAG, "Successfully synced ${parsedItems.size} items from remote URL")
                                onComplete?.invoke(parsedItems, true)
                                return
                            }
                        }
                    }

                    Log.w(TAG, "Unsuccessful response (${resp.code}). Falling back to local data.")
                    val fallbackItems = loadCachedOrAssetFallback(resolved)
                    onComplete?.invoke(fallbackItems, false)
                }
            }
        })
    }

    /**
     * Suspending coroutine to fetch content with network -> cache -> assets fallback.
     */
    suspend fun fetchContent(
        context: Context? = null,
        forceRefresh: Boolean = false
    ): List<GameContent> = withContext(Dispatchers.IO) {
        val resolved = resolveContext(context)

        if (!forceRefresh && inMemoryItems.isNotEmpty()) {
            return@withContext inMemoryItems
        }

        try {
            val request = Request.Builder()
                .url(RAW_CONTENT_URL)
                .header("Cache-Control", "no-cache")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.use { resp ->
                if (resp.isSuccessful) {
                    val jsonStr = resp.body?.string()
                    if (!jsonStr.isNullOrBlank()) {
                        val parsed = parseJson(jsonStr)
                        if (parsed.isNotEmpty()) {
                            if (resolved != null) {
                                saveToCache(resolved, jsonStr)
                            }
                            updateInMemory(parsed)
                            return@withContext parsed
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote sync exception: ${e.message}")
        }

        // Fallback to cache or assets
        return@withContext loadCachedOrAssetFallback(resolved)
    }

    /**
     * Saves JSON string into SharedPreferences under "cached_game_data".
     */
    fun saveToCache(context: Context, jsonString: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_CACHED_GAME_DATA, jsonString).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save data to SharedPreferences", e)
        }
    }

    /**
     * Reads cached JSON string from SharedPreferences under "cached_game_data".
     */
    fun getFromCache(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_CACHED_GAME_DATA, null)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read cached data from SharedPreferences", e)
            null
        }
    }

    /**
     * Reads fallback content from `assets/game_content.json`.
     */
    fun getFromAssets(context: Context): String? {
        return try {
            context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read $ASSET_FILE_NAME from assets", e)
            null
        }
    }

    /**
     * Loads items from SharedPreferences cache, or falls back to assets if cache is missing.
     */
    fun loadCachedOrAssetFallback(context: Context? = null): List<GameContent> {
        val resolved = resolveContext(context)
        if (resolved == null) {
            return inMemoryItems
        }

        // 1. Try SharedPreferences cache
        val cachedJson = getFromCache(resolved)
        if (!cachedJson.isNullOrBlank()) {
            val cachedItems = parseJson(cachedJson)
            if (cachedItems.isNotEmpty()) {
                updateInMemory(cachedItems)
                return cachedItems
            }
        }

        // 2. Fallback to assets/game_content.json
        val assetJson = getFromAssets(resolved)
        if (!assetJson.isNullOrBlank()) {
            val assetItems = parseJson(assetJson)
            if (assetItems.isNotEmpty()) {
                updateInMemory(assetItems)
                return assetItems
            }
        }

        return inMemoryItems
    }

    /**
     * Parses a JSON string.
     * Supports both:
     * 1. MasterAppContent JSON Object: contains game_items, alphabet_cards, word_blends, quiz_questions.
     * 2. Direct JSON Array: contains a list of GameContent items.
     */
    fun parseJson(jsonString: String?): List<GameContent> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val trimmed = jsonString.trim()
        return try {
            if (trimmed.startsWith("{")) {
                val masterType = object : TypeToken<MasterAppContent>() {}.type
                val master: MasterAppContent? = gson.fromJson(trimmed, masterType)
                if (master != null) {
                    master.alphabet_cards?.let { inMemoryAlphabetCards = it }
                    master.word_blends?.let { inMemoryWordBlends = it }
                    master.quiz_questions?.let { inMemoryQuizQuestions = it }
                    master.game_items ?: emptyList()
                } else {
                    emptyList()
                }
            } else {
                val listType = object : TypeToken<List<GameContent>>() {}.type
                gson.fromJson<List<GameContent>>(trimmed, listType) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Dynamic Learning Content Query Methods (Bilingual Alphabets, Word Blends, Quizzes)
     */
    fun getDynamicAlphabetCards(subjectId: String): List<AlphabetCard> {
        ensureLoaded(appContext)
        return inMemoryAlphabetCards[subjectId.lowercase()] ?: emptyList()
    }

    fun getDynamicWordBlends(subjectId: String): List<WordBlend> {
        ensureLoaded(appContext)
        return inMemoryWordBlends[subjectId.lowercase()] ?: emptyList()
    }

    fun getDynamicQuizzes(subjectId: String): List<QuizQuestion> {
        ensureLoaded(appContext)
        return inMemoryQuizQuestions[subjectId.lowercase()] ?: emptyList()
    }

    // -------------------------------------------------------------
    // Content Query Methods for Games
    // -------------------------------------------------------------

    /**
     * Retrieves items filtered by level.
     * If cache or memory is empty, loads local data from cache/assets.
     * If no items match the exact level, returns all available items.
     */
    fun getItemsByLevel(level: Int, context: Context? = null): List<GameContent> {
        ensureLoaded(context)
        val filtered = inMemoryItems.filter { it.level == level }
        return if (filtered.isNotEmpty()) filtered else inMemoryItems
    }

    /**
     * Alias for getItemsByLevel for game question providers.
     */
    fun getQuestionsByLevel(level: Int, context: Context? = null): List<GameContent> {
        return getItemsByLevel(level, context)
    }

    /**
     * Retrieves all loaded learning content items.
     */
    fun getAllItems(context: Context? = null): List<GameContent> {
        ensureLoaded(context)
        return inMemoryItems
    }

    /**
     * Retrieves a single item by its ID.
     */
    fun getItemById(id: Int, context: Context? = null): GameContent? {
        ensureLoaded(context)
        return inMemoryItems.firstOrNull { it.id == id }
    }

    /**
     * Generates QuizQuestion objects based on GameContent for the quiz game screen.
     */
    fun getQuizQuestionsForGame(
        level: Int,
        isEnglish: Boolean = false,
        context: Context? = null
    ): List<QuizQuestion> {
        val levelItems = getItemsByLevel(level, context)
        if (levelItems.isEmpty()) return emptyList()

        val allNames = inMemoryItems.map { if (isEnglish) it.name_en else it.name_hi }.distinct()

        return levelItems.mapIndexed { index, item ->
            val correctName = if (isEnglish) item.name_en else item.name_hi
            val wrongOptions = allNames.filter { it != correctName }.shuffled().take(3)
            val options = (wrongOptions + correctName).shuffled()
            val correctIndex = options.indexOf(correctName).coerceAtLeast(0)

            val questionText = if (isEnglish) {
                "What is this called in ${if (isEnglish) "English" else "Hindi"}?"
            } else {
                "इसे क्या कहते हैं?"
            }

            QuizQuestion(
                id = item.id.takeIf { it > 0 } ?: (index + 1),
                subjectId = "content_game",
                questionHindi = questionText,
                promptEmoji = getEmojiForName(item.name_en),
                options = options,
                correctIndex = correctIndex,
                praiseHindi = if (isEnglish) "Great job! That's correct!" else "शाबाश! बिल्कुल सही जवाब!",
                explanationHindi = if (isEnglish) item.audio_text_en else item.audio_text_hi
            )
        }
    }

    /**
     * Extracts a clean tracing character from an item.
     * Numbers (One..Six) return "1".."6".
     * English returns uppercase first letter ("A", "B", ...).
     * Hindi returns first character/vowel/consonant.
     */
    fun getTracingChar(item: GameContent, isEnglish: Boolean): String {
        return when (item.name_en.trim().lowercase()) {
            "one" -> "1"
            "two" -> "2"
            "three" -> "3"
            "four" -> "4"
            "five" -> "5"
            "six" -> "6"
            else -> {
                if (isEnglish) {
                    item.name_en.firstOrNull()?.uppercase() ?: "A"
                } else {
                    item.name_hi.firstOrNull()?.toString() ?: "अ"
                }
            }
        }
    }

    /**
     * Procedural Tracing Round generator using ContentRepository pool.
     * Avoids recently used items via recentItemIds buffer (size 4).
     */
    fun generateTracingRound(
        level: Int,
        recentItemIds: List<Int>,
        isEnglish: Boolean,
        context: Context? = null
    ): TracingRoundData {
        ensureLoaded(context)
        val pool = getAllItems(context)
        val available = pool.filterNot { it.id in recentItemIds }
        val target = if (available.isNotEmpty()) available.random() else pool.randomOrNull()
            ?: GameContent(1, "Apple", "सेब", "A is for Apple", "अ से सेब", 1)

        val charToTrace = getTracingChar(target, isEnglish)
        val emoji = getEmojiForName(target.name_en)

        val promptHi = "उंगली से '$charToTrace' लिखो! ${target.name_hi} ($emoji)"
        val promptEn = "Trace '$charToTrace' with your finger! ${target.name_en} ($emoji)"

        return TracingRoundData(
            targetItem = target,
            charToTrace = charToTrace,
            nameHindi = target.name_hi,
            nameEnglish = target.name_en,
            emoji = emoji,
            promptHindi = promptHi,
            promptEnglish = promptEn
        )
    }

    /**
     * Procedural Balloon Pop Round generator using ContentRepository pool.
     * Increases balloon options dynamically as level increases.
     */
    fun generateBalloonPopRound(
        level: Int,
        recentItemIds: List<Int>,
        isEnglish: Boolean,
        context: Context? = null
    ): BalloonRoundData {
        ensureLoaded(context)
        val pool = getAllItems(context)
        val available = pool.filterNot { it.id in recentItemIds }
        val target = if (available.isNotEmpty()) available.random() else pool.randomOrNull()
            ?: GameContent(1, "Apple", "सेब", "A is for Apple", "अ से सेब", 1)

        val targetChar = getTracingChar(target, isEnglish)

        // Dynamic difficulty: option count scales with level
        val optionCount = when {
            level < 3 -> 3
            level < 5 -> 4
            else -> 5
        }.coerceAtMost(pool.size.coerceAtLeast(3))

        val otherItems = pool.filter { it.id != target.id }.shuffled()
        val distractors = mutableListOf<String>()
        for (item in otherItems) {
            val char = getTracingChar(item, isEnglish)
            if (char != targetChar && char !in distractors) {
                distractors.add(char)
                if (distractors.size >= optionCount - 1) break
            }
        }
        val alphabetFallback = if (isEnglish) ('A'..'Z').map { it.toString() } else listOf("क", "ख", "ग", "घ", "च", "म", "न", "प", "स", "ह")
        while (distractors.size < optionCount - 1) {
            val candidate = alphabetFallback.random()
            if (candidate != targetChar && candidate !in distractors) {
                distractors.add(candidate)
            }
        }

        val options = (distractors + targetChar).shuffled()
        val emoji = getEmojiForName(target.name_en)

        val soundPrompt = if (isEnglish) {
            "Pop letter '$targetChar' for ${target.name_en} $emoji"
        } else {
            "${target.name_hi} $emoji के लिए '$targetChar' खोजो!"
        }

        val hintHi = "‘$targetChar’ अक्षर वाला गुब्बारा फोड़ो!"
        val hintEn = "Pop the balloon with letter '$targetChar'!"

        return BalloonRoundData(
            targetItem = target,
            targetChar = targetChar,
            soundPrompt = soundPrompt,
            hintHindi = hintHi,
            hintEnglish = hintEn,
            options = options
        )
    }

    /**
     * Procedural Word Match Round generator using ContentRepository pool.
     */
    fun generateWordMatchRound(
        level: Int,
        recentItemIds: List<Int>,
        isEnglish: Boolean,
        context: Context? = null
    ): WordMatchRoundData {
        ensureLoaded(context)
        val pool = getAllItems(context)
        val available = pool.filterNot { it.id in recentItemIds }
        val target = if (available.isNotEmpty()) available.random() else pool.randomOrNull()
            ?: GameContent(1, "Apple", "सेब", "Apple is sweet", "सेब मीठा है", 1)

        val correctWord = if (isEnglish) target.name_en else target.name_hi
        val emoji = getEmojiForName(target.name_en)

        // Option count scales dynamically with level
        val optionCount = when {
            level < 3 -> 2
            level < 5 -> 3
            else -> 4
        }.coerceAtMost(pool.size.coerceAtLeast(2))

        val distractors = pool.filter { it.id != target.id }
            .shuffled()
            .take(optionCount - 1)
            .map { if (isEnglish) it.name_en else it.name_hi }

        val options = (distractors + correctWord).shuffled()

        val prompt = if (isEnglish) {
            "Look at $emoji and choose the right word!"
        } else {
            "तस्वीर $emoji पहचानकर सही शब्द चुनो!"
        }

        return WordMatchRoundData(
            targetItem = target,
            emoji = emoji,
            correctWord = correctWord,
            hintPrompt = prompt,
            options = options
        )
    }

    /**
     * Procedural Math / Counting Round generator using ContentRepository pool.
     * Displays target item emojis and scales difficulty by level.
     */
    fun generateCountCollectRound(
        level: Int,
        recentItemIds: List<Int>,
        isEnglish: Boolean,
        context: Context? = null
    ): CountCollectRoundData {
        ensureLoaded(context)
        val pool = getAllItems(context)
        // Prefer countable fruit/animal items
        val countablePool = pool.filter {
            it.name_en.lowercase() !in listOf("one", "two", "three", "four", "five", "six")
        }.ifEmpty { pool }

        val available = countablePool.filterNot { it.id in recentItemIds }
        val target = if (available.isNotEmpty()) available.random() else countablePool.randomOrNull()
            ?: GameContent(1, "Apple", "सेब", "", "", 1)

        val emoji = getEmojiForName(target.name_en)

        // Target count dynamically scales with level (level 1: 1..5, level 2: 1..8, level 3+: 1..10)
        val maxCount = when {
            level <= 1 -> 5
            level <= 2 -> 8
            else -> 10
        }
        val count = (1..maxCount).random()

        // Generate options (correct + smart distractors)
        val optionCount = if (level <= 2) 3 else 4
        val distractorSet = mutableSetOf<Int>()
        val candidates = listOf(-2, -1, 1, 2, 3, -3).shuffled()
        for (delta in candidates) {
            val num = count + delta
            if (num in 1..(maxCount + 2) && num != count) {
                distractorSet.add(num)
                if (distractorSet.size >= optionCount - 1) break
            }
        }
        while (distractorSet.size < optionCount - 1) {
            val candidate = (1..(maxCount + 3)).random()
            if (candidate != count) distractorSet.add(candidate)
        }

        val options = (distractorSet + count).shuffled()

        return CountCollectRoundData(
            targetItem = target,
            emoji = emoji,
            nameHindi = target.name_hi,
            nameEnglish = target.name_en,
            count = count,
            options = options
        )
    }

    fun getEmojiForContent(nameEn: String): String = getEmojiForName(nameEn)

    fun getEmojiForName(nameEn: String): String {
        return when (nameEn.trim().lowercase()) {
            "apple" -> "🍎"
            "banana" -> "🍌"
            "mango" -> "🥭"
            "orange" -> "🍊"
            "grapes" -> "🍇"
            "watermelon" -> "🍉"
            "papaya" -> "🍈"
            "cow" -> "🐮"
            "dog" -> "🐶"
            "cat" -> "🐱"
            "lion" -> "🦁"
            "elephant" -> "🐘"
            "tiger" -> "🐯"
            "monkey" -> "🐵"
            "one" -> "1️⃣"
            "two" -> "2️⃣"
            "three" -> "3️⃣"
            "four" -> "4️⃣"
            "five" -> "5️⃣"
            "six" -> "6️⃣"
            else -> "⭐"
        }
    }

    private fun ensureLoaded(context: Context?) {
        if (inMemoryItems.isEmpty()) {
            val resolved = resolveContext(context)
            if (resolved != null) {
                loadCachedOrAssetFallback(resolved)
            }
        }
    }

    private fun updateInMemory(items: List<GameContent>) {
        inMemoryItems = items
        _contentFlow.value = items
    }

    private fun resolveContext(context: Context?): Context? {
        val resolved = context?.applicationContext ?: context ?: appContext
        if (appContext == null && resolved != null) {
            appContext = resolved
        }
        return resolved
    }
}

