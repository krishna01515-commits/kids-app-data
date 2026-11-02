package com.kidslearning.chutki.data

import kotlin.random.Random

data class ProceduralItem(
    val id: String,
    val emoji: String,
    val nameHindi: String,
    val nameEnglish: String,
    val category: String,
    val phonicsPrompt: String = "",
    val charKey: String = ""
)

data class BalloonRound(
    val targetChar: String,
    val soundPrompt: String,
    val hintHindi: String,
    val hintEnglish: String,
    val options: List<String>
)

data class WordMatchRound(
    val emoji: String,
    val correctWord: String,
    val hintPrompt: String,
    val options: List<String>
)

data class MissingLetterRound(
    val emoji: String,
    val beforeChar: String,
    val missingChar: String,
    val afterChar: String,
    val fullWordHindi: String,
    val fullWordEnglish: String,
    val options: List<String>
)

data class CountCollectRound(
    val emoji: String,
    val nameHindi: String,
    val nameEnglish: String,
    val count: Int,
    val options: List<Int>
)

data class TrainRound(
    val numbers: List<Int?>, // null represents missing wagon
    val missingIndex: Int,
    val correctNumber: Int,
    val options: List<Int>
)

enum class SizePromptType {
    BIGGEST,
    SMALLEST
}

data class SizeItem(
    val emoji: String,
    val scaleFactor: Float,
    val isTarget: Boolean
)

data class SizeComparisonRound(
    val promptType: SizePromptType,
    val titleHindi: String,
    val titleEnglish: String,
    val items: List<SizeItem>,
    val correctIndex: Int
)

data class OddOneOutRound(
    val items: List<ProceduralItem>,
    val oddIndex: Int,
    val promptHindi: String,
    val promptEnglish: String,
    val explanationHindi: String,
    val explanationEnglish: String
)

data class ShadowRound(
    val targetItem: ProceduralItem,
    val options: List<ProceduralItem>
)

data class MemoryCardData(
    val id: Int,
    val pairKey: String,
    val emoji: String,
    val labelHindi: String,
    val labelEnglish: String
)

object ProceduralGameEngine {

    // 1. MASTER POOL OF 50+ DIVERSE ITEMS
    val masterItems = listOf(
        // Fruits
        ProceduralItem("apple", "🍎", "सेब", "Apple", "FRUIT", "A says 'ए'", "A"),
        ProceduralItem("mango", "🥭", "आम", "Mango", "FRUIT", "M says 'म'", "M"),
        ProceduralItem("banana", "🍌", "केला", "Banana", "FRUIT", "B says 'ब'", "B"),
        ProceduralItem("grapes", "🍇", "अंगूर", "Grapes", "FRUIT", "G says 'ग'", "G"),
        ProceduralItem("orange", "🍊", "संतरा", "Orange", "FRUIT", "O says 'ओ'", "O"),
        ProceduralItem("strawberry", "🍓", "स्ट्रॉबेरी", "Strawberry", "FRUIT", "S says 'स'", "S"),
        ProceduralItem("watermelon", "🍉", "तरबूज", "Watermelon", "FRUIT", "W says 'व'", "W"),
        ProceduralItem("pineapple", "🍍", "अनानास", "Pineapple", "FRUIT", "P says 'प'", "P"),
        ProceduralItem("coconut", "🥥", "नारियल", "Coconut", "FRUIT", "C says 'क'", "C"),

        // Animals
        ProceduralItem("lion", "🦁", "शेर", "Lion", "ANIMAL", "L says 'ल'", "L"),
        ProceduralItem("elephant", "🐘", "हाथी", "Elephant", "ANIMAL", "E says 'ए'", "E"),
        ProceduralItem("tiger", "🐯", "बाघ", "Tiger", "ANIMAL", "T says 'ट'", "T"),
        ProceduralItem("cat", "🐱", "बिल्ली", "Cat", "ANIMAL", "C says 'क'", "C"),
        ProceduralItem("dog", "🐶", "कुत्ता", "Dog", "ANIMAL", "D says 'ड'", "D"),
        ProceduralItem("monkey", "🐵", "बंदर", "Monkey", "ANIMAL", "M says 'म'", "M"),
        ProceduralItem("rabbit", "🐰", "खरगोश", "Rabbit", "ANIMAL", "R says 'र'", "R"),
        ProceduralItem("bear", "🐻", "भालू", "Bear", "ANIMAL", "B says 'ब'", "B"),
        ProceduralItem("panda", "🐼", "पांडा", "Panda", "ANIMAL", "P says 'प'", "P"),
        ProceduralItem("cow", "🐮", "गाय", "Cow", "ANIMAL", "C says 'क'", "C"),
        ProceduralItem("horse", "🐴", "घोड़ा", "Horse", "ANIMAL", "H says 'ह'", "H"),
        ProceduralItem("duck", "🦆", "बतख", "Duck", "ANIMAL", "D says 'ड'", "D"),
        ProceduralItem("owl", "🦉", "उल्लू", "Owl", "ANIMAL", "O says 'ओ'", "O"),
        ProceduralItem("frog", "🐸", "मेंढक", "Frog", "ANIMAL", "F says 'फ'", "F"),

        // Vehicles
        ProceduralItem("car", "🚗", "कार", "Car", "VEHICLE", "C says 'क'", "C"),
        ProceduralItem("bus", "🚌", "बस", "Bus", "VEHICLE", "B says 'ब'", "B"),
        ProceduralItem("train", "🚂", "रेलगाड़ी", "Train", "VEHICLE", "T says 'ट'", "T"),
        ProceduralItem("airplane", "✈️", "हवाई जहाज", "Airplane", "VEHICLE", "A says 'ए'", "A"),
        ProceduralItem("rocket", "🚀", "रॉकेट", "Rocket", "VEHICLE", "R says 'र'", "R"),
        ProceduralItem("bicycle", "🚲", "साइकिल", "Bicycle", "VEHICLE", "B says 'ब'", "B"),
        ProceduralItem("boat", "⛵", "नाव", "Boat", "VEHICLE", "B says 'ब'", "B"),
        ProceduralItem("helicopter", "🚁", "हेलिकॉप्टर", "Helicopter", "VEHICLE", "H says 'ह'", "H"),

        // Nature & Fun Objects
        ProceduralItem("sun", "☀️", "सूरज", "Sun", "NATURE", "S says 'स'", "S"),
        ProceduralItem("moon", "🌙", "चांद", "Moon", "NATURE", "M says 'म'", "M"),
        ProceduralItem("star", "⭐", "सितारा", "Star", "NATURE", "S says 'स'", "S"),
        ProceduralItem("flower", "🌸", "फूल", "Flower", "NATURE", "F says 'फ'", "F"),
        ProceduralItem("tree", "🌳", "पेड़", "Tree", "NATURE", "T says 'ट'", "T"),
        ProceduralItem("balloon", "🎈", "गुब्बारा", "Balloon", "OBJECT", "B says 'ब'", "B"),
        ProceduralItem("kite", "🪁", "पतंग", "Kite", "OBJECT", "K says 'क'", "K"),
        ProceduralItem("ball", "⚽", "गेंद", "Ball", "OBJECT", "B says 'ब'", "B"),
        ProceduralItem("gift", "🎁", "उपहार", "Gift", "OBJECT", "G says 'ग'", "G"),
        ProceduralItem("bell", "🔔", "घंटी", "Bell", "OBJECT", "B says 'ब'", "B"),
        ProceduralItem("butterfly", "🦋", "तितली", "Butterfly", "NATURE", "B says 'ब'", "B"),
        ProceduralItem("fish", "🐟", "मछली", "Fish", "NATURE", "F says 'फ'", "F")
    )

    // Master Phonics pool (English A-Z and Hindi Swar-Vyanjan)
    val phonicsList = listOf(
        Pair("A", "A says 'ए' (Apple 🍎)"),
        Pair("B", "B says 'ब' (Ball ⚽)"),
        Pair("C", "C says 'क' (Cat 🐱)"),
        Pair("D", "D says 'ड' (Duck 🦆)"),
        Pair("E", "E says 'ए' (Elephant 🐘)"),
        Pair("F", "F says 'फ' (Fish 🐟)"),
        Pair("G", "G says 'ग' (Grapes 🍇)"),
        Pair("H", "H says 'ह' (Horse 🐴)"),
        Pair("I", "I says 'इ' (Ice Cream 🍦)"),
        Pair("J", "J says 'ज' (Jug 🏺)"),
        Pair("K", "K says 'क' (Kite 🪁)"),
        Pair("L", "L says 'ल' (Lion 🦁)"),
        Pair("M", "M says 'म' (Mango 🥭)"),
        Pair("N", "N says 'न' (Nest 🪺)"),
        Pair("O", "O says 'ओ' (Orange 🍊)"),
        Pair("P", "P says 'प' (Parrot 🦜)"),
        Pair("Q", "Q says 'क्व' (Queen 👑)"),
        Pair("R", "R says 'र' (Rabbit 🐰)"),
        Pair("S", "S says 'स' (Sun ☀️)"),
        Pair("T", "T says 'ट' (Tiger 🐯)"),
        Pair("U", "U says 'अ' (Umbrella ☂️)"),
        Pair("V", "V says 'व' (Van 🚐)"),
        Pair("W", "W says 'व' (Watch ⌚)"),
        Pair("X", "X says 'क्स' (Xylophone 🎵)"),
        Pair("Y", "Y says 'य' (Yak 🐂)"),
        Pair("Z", "Z says 'ज़' (Zebra 🦓)"),

        // Hindi
        Pair("अ", "अ से 'अनार' 🍎"),
        Pair("आ", "आ से 'आम' 🥭"),
        Pair("इ", "इ से 'इमली' 🌿"),
        Pair("ई", "ई से 'ईख' 🎋"),
        Pair("उ", "उ से 'उल्लू' 🦉"),
        Pair("ऊ", "ऊ से 'ऊन' 🧶"),
        Pair("क", "क से 'कबूतर' 🕊️"),
        Pair("ख", "ख से 'खरगोश' 🐰"),
        Pair("ग", "ग से 'गमला' 🪴"),
        Pair("घ", "घ से 'घड़ी' ⏰"),
        Pair("च", "च से 'चम्मच' 🥄"),
        Pair("छ", "छ से 'छतरी' ☂️"),
        Pair("ज", "ज से 'जहाज' 🚢"),
        Pair("झ", "झ से 'झंडा' 🇮🇳"),
        Pair("ट", "ट से 'टमाटर' 🍅"),
        Pair("त", "त से 'तितली' 🦋"),
        Pair("द", "द से 'दवात' ✒️"),
        Pair("न", "न से 'नल' 🚰"),
        Pair("प", "प से 'पतंग' 🪁"),
        Pair("फ", "फ से 'फल' 🍇"),
        Pair("ब", "ब से 'बतख' 🦆"),
        Pair("भ", "भ से 'भालू' 🐻"),
        Pair("म", "म से 'मछली' 🐟"),
        Pair("र", "र से 'रथ' 🎪"),
        Pair("ल", "ल से 'लड्डू' 🟡"),
        Pair("व", "व से 'वक' 🦩"),
        Pair("श", "श से 'शेर' 🦁"),
        Pair("स", "स से 'सेब' 🍎"),
        Pair("ह", "ह से 'हाथी' 🐘")
    )

    // Master 3-letter Words for Missing Letters
    val missingWordPool = listOf(
        Triple("🐱", "C_T", "A"),
        Triple("☀️", "S_N", "U"),
        Triple("🐶", "D_G", "O"),
        Triple("🚗", "C_R", "A"),
        Triple("🦇", "B_T", "A"),
        Triple("🖊️", "P_N", "E"),
        Triple("🎩", "H_T", "A"),
        Triple("🐖", "P_G", "I"),
        Triple("🥛", "M_G", "U"),
        Triple("🪷", "क_ल", "म"),
        Triple("🏰", "म_ल", "ह"),
        Triple("🦆", "ब_ख", "त"),
        Triple("🏡", "घ_र", ""),
        Triple("🍯", "श_द", "ह"),
        Triple("👁️", "न_न", "य"),
        Triple("🫖", "क_प", "प"),
        Triple("🍎", "A_E", "P"),
        Triple("🦁", "L_N", "I"),
        Triple("🐟", "F_H", "I"),
        Triple("🕊️", "B_D", "I")
    )

    /**
     * Procedural Balloon Pop Generator with No-Repeat Buffer
     */
    fun generateBalloonPopRound(level: Int, recentChars: List<String>): BalloonRound {
        val available = phonicsList.filterNot { recentChars.contains(it.first) }
        val target = if (available.isNotEmpty()) available.random() else phonicsList.random()

        val isEnglishChar = target.first[0].isLetter() && target.first[0] in 'A'..'Z'
        val distractorPool = phonicsList.filter {
            it.first != target.first && (isEnglishChar == (it.first[0] in 'A'..'Z'))
        }

        val optionCount = when {
            level < 3 -> 2
            level < 6 -> 3
            else -> 4
        }

        val distractors = distractorPool.shuffled().take(optionCount - 1).map { it.first }
        val options = (distractors + target.first).shuffled()

        val hintHi = "‘${target.first}’ अक्षर वाला गुब्बारा फोड़ो!"
        val hintEn = "Pop the balloon with letter '${target.first}'!"

        return BalloonRound(
            targetChar = target.first,
            soundPrompt = target.second,
            hintHindi = hintHi,
            hintEnglish = hintEn,
            options = options
        )
    }

    /**
     * Procedural Word Match Generator backed by ContentRepository
     */
    fun generateWordMatchRound(level: Int, recentIds: List<String>, isEnglish: Boolean): WordMatchRound {
        val repoItems = ContentRepository.getAllItems()
        val pool: List<ProceduralItem> = if (repoItems.isNotEmpty()) {
            repoItems.map { c ->
                ProceduralItem(
                    id = "${c.id}",
                    emoji = ContentRepository.getEmojiForName(c.name_en),
                    nameHindi = c.name_hi,
                    nameEnglish = c.name_en,
                    category = "CONTENT",
                    phonicsPrompt = c.audio_text_en,
                    charKey = c.name_en.firstOrNull()?.toString() ?: ""
                )
            }
        } else {
            masterItems
        }

        val available = pool.filterNot { recentIds.contains(it.id) }
        val target = if (available.isNotEmpty()) available.random() else pool.random()

        val optionCount = when {
            level < 3 -> 2
            level < 6 -> 3
            else -> 4
        }

        val distractors = pool.filter { it.id != target.id }
            .shuffled()
            .take(optionCount - 1)
            .map { if (isEnglish) it.nameEnglish else it.nameHindi }

        val correctWord = if (isEnglish) target.nameEnglish else target.nameHindi
        val options = (distractors + correctWord).shuffled()

        val prompt = if (isEnglish) "Look at the picture and match the correct word!" else "तस्वीर पहचानकर सही शब्द चुनो!"

        return WordMatchRound(
            emoji = target.emoji,
            correctWord = correctWord,
            hintPrompt = prompt,
            options = options
        )
    }

    /**
     * Procedural Missing Letters Generator
     */
    fun generateMissingLettersRound(level: Int, recentWords: List<String>): MissingLetterRound {
        val available = missingWordPool.filterNot { recentWords.contains(it.second) }
        val puzzle = if (available.isNotEmpty()) available.random() else missingWordPool.random()

        val missing = puzzle.third
        val parts = puzzle.second.split("_")
        val before = parts.getOrNull(0) ?: ""
        val after = parts.getOrNull(1) ?: ""

        val isEnglishChar = missing.isNotEmpty() && missing[0] in 'A'..'Z'
        val distractorSet = mutableSetOf<String>()

        val englishLetters = ('A'..'Z').map { it.toString() }
        val hindiLetters = listOf("क", "म", "ल", "र", "स", "त", "ह", "न", "प", "य", "च")

        while (distractorSet.size < (if (level < 4) 2 else 3)) {
            val candidate = if (isEnglishChar) englishLetters.random() else hindiLetters.random()
            if (candidate != missing) {
                distractorSet.add(candidate)
            }
        }

        val options = (distractorSet + missing).shuffled()

        return MissingLetterRound(
            emoji = puzzle.first,
            beforeChar = before,
            missingChar = missing,
            afterChar = after,
            fullWordHindi = "$before$missing$after",
            fullWordEnglish = "$before$missing$after",
            options = options
        )
    }

    /**
     * Procedural Math Count & Collect Generator (Infinite Numbers backed by ContentRepository)
     */
    fun generateCountCollectRound(level: Int): CountCollectRound {
        val maxLimit = (level * 2 + 3).coerceIn(4, 12)
        val targetCount = (1..maxLimit).random()

        val repoItems = ContentRepository.getAllItems()
        val countItemCandidates: List<Pair<String, Pair<String, String>>> = if (repoItems.isNotEmpty()) {
            repoItems.filter { it.name_en.lowercase() !in listOf("one", "two", "three", "four", "five", "six") }
                .map { c ->
                    Pair(ContentRepository.getEmojiForName(c.name_en), Pair(c.name_hi, c.name_en))
                }
        } else {
            listOf(
                Pair("🐟", Pair("मछलियां", "Fishes")),
                Pair("🍎", Pair("सेब", "Apples")),
                Pair("🦋", Pair("तितलियां", "Butterflies")),
                Pair("⭐", Pair("सितारे", "Stars")),
                Pair("🎈", Pair("गुब्बारे", "Balloons")),
                Pair("🚗", Pair("कारें", "Cars")),
                Pair("🌸", Pair("फूल", "Flowers")),
                Pair("🦆", Pair("बतख", "Ducks")),
                Pair("🥭", Pair("आम", "Mangoes")),
                Pair("🍬", Pair("टॉफियां", "Candies"))
            )
        }

        val item = countItemCandidates.random()
        val optionCount = when {
            level < 3 -> 3
            else -> 4
        }

        val wrongSet = mutableSetOf<Int>()
        while (wrongSet.size < optionCount - 1) {
            val delta = (-3..3).random()
            val candidate = (targetCount + delta).coerceIn(1, maxLimit + 3)
            if (candidate != targetCount) {
                wrongSet.add(candidate)
            }
        }

        val options = (wrongSet + targetCount).shuffled()

        return CountCollectRound(
            emoji = item.first,
            nameHindi = item.second.first,
            nameEnglish = item.second.second,
            count = targetCount,
            options = options
        )
    }

    /**
     * Procedural Number Train Generator (Infinite Sequences)
     */
    fun generateNumberTrainRound(level: Int): TrainRound {
        val step = if (level >= 5 && Random.nextBoolean()) 2 else 1
        val start = (1..(level * 3 + 2)).random()

        val fullSeq = listOf(start, start + step, start + 2 * step, start + 3 * step)
        val missingIndex = (0..3).random()
        val correctNum = fullSeq[missingIndex]

        val maskedSeq = fullSeq.mapIndexed { idx, v -> if (idx == missingIndex) null else v }

        val wrongSet = mutableSetOf<Int>()
        val optionsCount = when {
            level < 3 -> 3
            else -> 4
        }

        while (wrongSet.size < optionsCount - 1) {
            val delta = (-4..4).random()
            val candidate = (correctNum + delta).coerceAtLeast(1)
            if (candidate != correctNum) {
                wrongSet.add(candidate)
            }
        }

        val options = (wrongSet + correctNum).shuffled()

        return TrainRound(
            numbers = maskedSeq,
            missingIndex = missingIndex,
            correctNumber = correctNum,
            options = options
        )
    }

    /**
     * Procedural Size Comparison Generator
     */
    fun generateSizeComparisonRound(level: Int): SizeComparisonRound {
        val promptType = if (Random.nextBoolean()) SizePromptType.BIGGEST else SizePromptType.SMALLEST
        val emojiPool = listOf("🐘", "🦁", "🚗", "🍎", "🎈", "⭐", "🦋", "🐟", "🏠", "🚀")
        val chosenEmoji = emojiPool.random()

        // 3 items with distinct scale factors: Small, Medium, Big
        val scales = listOf(0.65f, 1.0f, 1.45f).shuffled()
        val targetScale = if (promptType == SizePromptType.BIGGEST) 1.45f else 0.65f
        val correctIdx = scales.indexOf(targetScale)

        val sizeItems = scales.mapIndexed { idx, s ->
            SizeItem(emoji = chosenEmoji, scaleFactor = s, isTarget = idx == correctIdx)
        }

        val titleHi = if (promptType == SizePromptType.BIGGEST) "सबसे बड़ा कौन सा है? 🐘" else "सबसे छोटा कौन सा है? 🐜"
        val titleEn = if (promptType == SizePromptType.BIGGEST) "Which one is the BIGGEST? 🐘" else "Which one is the SMALLEST? 🐜"

        return SizeComparisonRound(
            promptType = promptType,
            titleHindi = titleHi,
            titleEnglish = titleEn,
            items = sizeItems,
            correctIndex = correctIdx
        )
    }

    /**
     * Procedural Odd One Out Generator
     */
    fun generateOddOneOutRound(level: Int, recentOddIds: List<String>): OddOneOutRound {
        val categories = listOf("FRUIT", "ANIMAL", "VEHICLE", "NATURE")
        val mainCat = categories.random()
        val oddCat = categories.filter { it != mainCat }.random()

        val mainPool = masterItems.filter { it.category == mainCat }.shuffled().take(3)
        val oddItem = masterItems.filter { it.category == oddCat && !recentOddIds.contains(it.id) }.shuffled().firstOrNull()
            ?: masterItems.filter { it.category == oddCat }.shuffled().first()

        val combined = (mainPool + oddItem).shuffled()
        val oddIdx = combined.indexOf(oddItem)

        val hiPrompt = "इनमें से कौन सा सबसे अलग है? पहचानो!"
        val enPrompt = "Which one is different from the rest? Spot it!"

        val hiExpl = "${oddItem.nameHindi} (${oddItem.category}) अलग है, बाकी सब $mainCat हैं!"
        val enExpl = "${oddItem.nameEnglish} is a ${oddItem.category}, while others are ${mainCat}s!"

        return OddOneOutRound(
            items = combined,
            oddIndex = oddIdx,
            promptHindi = hiPrompt,
            promptEnglish = enPrompt,
            explanationHindi = hiExpl,
            explanationEnglish = enExpl
        )
    }

    /**
     * Procedural Shadow Matching Generator
     */
    fun generateShadowMatchingRound(level: Int, recentIds: List<String>): ShadowRound {
        val available = masterItems.filterNot { recentIds.contains(it.id) }
        val target = if (available.isNotEmpty()) available.random() else masterItems.random()

        val optionCount = when {
            level < 3 -> 2
            level < 6 -> 3
            else -> 4
        }

        val distractors = masterItems.filter { it.id != target.id }.shuffled().take(optionCount - 1)
        val options = (distractors + target).shuffled()

        return ShadowRound(targetItem = target, options = options)
    }

    /**
     * Procedural Flip Card Memory Round (scales with level: 2 pairs -> 3 pairs -> 4 pairs)
     */
    fun generateCardMemoryCards(level: Int): List<MemoryCardData> {
        val pairsCount = when {
            level < 2 -> 2 // 4 cards
            level < 4 -> 3 // 6 cards
            else -> 4      // 8 cards
        }

        val pickedItems = masterItems.shuffled().take(pairsCount)
        val cardList = mutableListOf<MemoryCardData>()

        var cardId = 0
        pickedItems.forEach { item ->
            // Pair card 1
            cardList.add(
                MemoryCardData(
                    id = cardId++,
                    pairKey = item.id,
                    emoji = item.emoji,
                    labelHindi = item.nameHindi,
                    labelEnglish = item.nameEnglish
                )
            )
            // Pair card 2
            cardList.add(
                MemoryCardData(
                    id = cardId++,
                    pairKey = item.id,
                    emoji = item.emoji,
                    labelHindi = item.nameHindi,
                    labelEnglish = item.nameEnglish
                )
            )
        }

        return cardList.shuffled()
    }
}

