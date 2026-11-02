package com.kidslearning.chutki.model

enum class GameCategory(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val iconEmoji: String,
    val themeColorHex: Long
) {
    LITERACY(
        id = "literacy",
        titleHindi = "भाषा और अक्षर ज्ञान",
        titleEnglish = "Language & Literacy",
        iconEmoji = "🔤",
        themeColorHex = 0xFFFF7A00
    ),
    MATH_LOGIC(
        id = "math_logic",
        titleHindi = "गणित और तार्किक सोच",
        titleEnglish = "Math & Logic",
        iconEmoji = "🔢",
        themeColorHex = 0xFF00E676
    ),
    COGNITIVE_MEMORY(
        id = "cognitive_memory",
        titleHindi = "मेमोरी और फोकस",
        titleEnglish = "Memory & Focus",
        iconEmoji = "🧠",
        themeColorHex = 0xFF00B0FF
    ),
    CREATIVITY_MOTOR(
        id = "creativity_motor",
        titleHindi = "रचनात्मकता और कला",
        titleEnglish = "Creativity & Motor Skills",
        iconEmoji = "🎨",
        themeColorHex = 0xFFE040FB
    )
}

enum class GameType(
    val id: String,
    val category: GameCategory,
    val titleHindi: String,
    val subtitleHindi: String,
    val iconEmoji: String,
    val xpReward: Int,
    val starReward: Int,
    val titleEnglish: String = "",
    val subtitleEnglish: String = ""
) {
    // 1. भाषा और अक्षर ज्ञान (Language & Literacy)
    FINGER_TRACING(
        id = "tracing",
        category = GameCategory.LITERACY,
        titleHindi = "अक्षर ट्रेसिंग (Finger Tracing)",
        subtitleHindi = "स्क्रीन पर जादुई उंगली से अक्षर लिखो",
        iconEmoji = "✍️",
        xpReward = 40,
        starReward = 3,
        titleEnglish = "Finger Tracing",
        subtitleEnglish = "Trace letters smoothly with your magical finger"
    ),
    BALLOON_POP(
        id = "balloon_pop",
        category = GameCategory.LITERACY,
        titleHindi = "गुब्बारे फोड़ो (Phonics Pop)",
        subtitleHindi = "आवाज़ सुनकर सही अक्षर वाला गुब्बारा फोड़ो",
        iconEmoji = "🎈",
        xpReward = 30,
        starReward = 2,
        titleEnglish = "Phonics Balloon Pop",
        subtitleEnglish = "Listen to the letter sound and pop the matching balloon"
    ),
    WORD_MATCH(
        id = "word_match",
        category = GameCategory.LITERACY,
        titleHindi = "चित्र व शब्द मिलान (Word Match)",
        subtitleHindi = "तस्वीर पहचानकर सही अक्षर/शब्द मिलाओ",
        iconEmoji = "🍎",
        xpReward = 35,
        starReward = 2,
        titleEnglish = "Word & Picture Match",
        subtitleEnglish = "Look at the picture and match with the right word"
    ),
    MISSING_LETTERS(
        id = "missing_letters",
        category = GameCategory.LITERACY,
        titleHindi = "अक्षर पहेली (Missing Letters)",
        subtitleHindi = "खाली जगह में सही अक्षर चुनकर शब्द पूरा करो",
        iconEmoji = "🔠",
        xpReward = 35,
        starReward = 2,
        titleEnglish = "Missing Letters Puzzle",
        subtitleEnglish = "Pick the missing letter to complete the cute word"
    ),

    // 2. गणित और तार्किक सोच (Math & Logic)
    COUNT_COLLECT(
        id = "count_collect",
        category = GameCategory.MATH_LOGIC,
        titleHindi = "गिनती खेल (Count & Collect)",
        subtitleHindi = "स्क्रीन पर मछलियां व फल गिनो और सही संख्या चुनो",
        iconEmoji = "🐟",
        xpReward = 30,
        starReward = 2,
        titleEnglish = "Count & Collect",
        subtitleEnglish = "Count swimming fishes and fruits to pick the number"
    ),
    SHAPE_COLOR_SORTER(
        id = "shape_sorter",
        category = GameCategory.MATH_LOGIC,
        titleHindi = "आकार व रंग छंटाई (Shape Sorter)",
        subtitleHindi = "सही आकार वाले डिब्बे में आकृति डालो",
        iconEmoji = "🔷",
        xpReward = 35,
        starReward = 2,
        titleEnglish = "Shape & Color Sorter",
        subtitleEnglish = "Sort shapes and colors into their matching boxes"
    ),
    NUMBER_TRAIN(
        id = "number_train",
        category = GameCategory.MATH_LOGIC,
        titleHindi = "नंबर रेलगाड़ी (Sequence Train)",
        subtitleHindi = "छुक-छुक ट्रेन के खाली डिब्बे में सही क्रम लगाओ",
        iconEmoji = "🚂",
        xpReward = 35,
        starReward = 2,
        titleEnglish = "Number Sequence Train",
        subtitleEnglish = "Fill missing numbers in train wagons to make it go"
    ),
    SIZE_COMPARISON(
        id = "size_comparison",
        category = GameCategory.MATH_LOGIC,
        titleHindi = "बड़ा या छोटा? (Size Comparison)",
        subtitleHindi = "वस्तुओं में से सबसे बड़ा या सबसे छोटा पहचानो",
        iconEmoji = "🐘",
        xpReward = 25,
        starReward = 1,
        titleEnglish = "Big vs Small",
        subtitleEnglish = "Identify which object is the biggest or smallest"
    ),

    // 3. मेमोरी और फोकस (Cognitive & Memory)
    CARD_MEMORY(
        id = "card_memory",
        category = GameCategory.COGNITIVE_MEMORY,
        titleHindi = "मेमोरी कार्ड मैच (Flip Cards)",
        subtitleHindi = "उल्टे कार्ड्स में से एक जैसे दो कार्ड्स खोजो",
        iconEmoji = "🃏",
        xpReward = 45,
        starReward = 3,
        titleEnglish = "Card Flip Memory",
        subtitleEnglish = "Flip cards and find pairs of identical cute items"
    ),
    ODD_ONE_OUT(
        id = "odd_one_out",
        category = GameCategory.COGNITIVE_MEMORY,
        titleHindi = "अलग पहचानो (Odd One Out)",
        subtitleHindi = "चार तस्वीरों में से जो सबसे अलग है उसे खोजो",
        iconEmoji = "🔍",
        xpReward = 30,
        starReward = 2,
        titleEnglish = "Odd One Out",
        subtitleEnglish = "Find the one object that does not belong to the group"
    ),
    SHADOW_MATCHING(
        id = "shadow_matching",
        category = GameCategory.COGNITIVE_MEMORY,
        titleHindi = "परछाई मिलाओ (Shadow Match)",
        subtitleHindi = "चित्र को उसकी सही परछाई से जोड़ो",
        iconEmoji = "👥",
        xpReward = 35,
        starReward = 2,
        titleEnglish = "Shadow Matching",
        subtitleEnglish = "Match animals and vehicles with their dark silhouettes"
    ),

    // 4. रचनात्मकता और मोटर स्किल्स (Creativity & Motor Skills)
    DIGITAL_COLORING(
        id = "digital_coloring",
        category = GameCategory.CREATIVITY_MOTOR,
        titleHindi = "रंग भरो स्लेट (Coloring Book)",
        subtitleHindi = "सुंदर चित्रों में मनपसंद रंग भरो",
        iconEmoji = "🎨",
        xpReward = 40,
        starReward = 3,
        titleEnglish = "Digital Coloring Book",
        subtitleEnglish = "Fill cheerful colors into cute outlines and sketch freely"
    ),
    JIGSAW_PUZZLE(
        id = "jigsaw_puzzle",
        category = GameCategory.CREATIVITY_MOTOR,
        titleHindi = "चित्र पहेली (Jigsaw Puzzle)",
        subtitleHindi = "टुकड़ों को सही जोड़कर पूरी तस्वीर बनाओ",
        iconEmoji = "🧩",
        xpReward = 40,
        starReward = 3,
        titleEnglish = "Jigsaw Puzzle",
        subtitleEnglish = "Put puzzle tiles in place to reveal the full picture"
    ),
    MUSICAL_PIANO(
        id = "musical_piano",
        category = GameCategory.CREATIVITY_MOTOR,
        titleHindi = "जादुई पियानो (Musical Piano)",
        subtitleHindi = "सुर बजाओ और जानवरों की प्यारी आवाजें सुनो",
        iconEmoji = "🎹",
        xpReward = 35,
        starReward = 2,
        titleEnglish = "Musical Piano",
        subtitleEnglish = "Play musical notes, rhymes, and listen to animal sounds"
    )
}

