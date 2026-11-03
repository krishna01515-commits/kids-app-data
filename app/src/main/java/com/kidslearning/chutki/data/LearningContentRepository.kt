package com.kidslearning.chutki.data

import com.kidslearning.chutki.model.AlphabetCard
import com.kidslearning.chutki.model.Badge
import com.kidslearning.chutki.model.LeaderboardUser
import com.kidslearning.chutki.model.LevelInfo
import com.kidslearning.chutki.model.QuizQuestion
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.model.TracingLetter
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.model.WordBlend

object LearningContentRepository {

    // 1. Levels for Subjects (Dynamic levels based on user unlocked status and stars)
    fun getLevelsForSubject(
        subjectId: String,
        unlockedLevel: Int = 1,
        getStars: (Int) -> Int = { 0 },
        getPercent: (Int) -> Int = { 0 }
    ): List<LevelInfo> {
        val isMath = subjectId.lowercase() == "math"
        return listOf(
            LevelInfo(
                levelNumber = 1,
                titleHindi = if (isMath) "लेवल 1: संख्या 1 से 100 और उदाहरण" else "लेवल 1: संपूर्ण वर्णमाला कार्ड्स",
                subtitleHindi = if (isMath) "प्रिया दीदी के साथ 1 से 100 तक गिनती और मजेदार उदाहरण सीखो" else "प्रिया दीदी के साथ स्वर व व्यंजन पहचानो और बोलो",
                iconEmoji = if (isMath) "🔢" else "🎴",
                isUnlocked = true,
                starsEarned = getStars(1),
                xpReward = 50,
                completionPercent = getPercent(1)
            ),
            LevelInfo(
                levelNumber = 2,
                titleHindi = if (isMath) "लेवल 2: जादुई स्लेट - 1 से 100 लिखना सीखो" else "लेवल 2: जादुई स्लेट ✍️",
                subtitleHindi = "स्क्रीन पर जादुई स्लेट पर अक्षर व संख्या लिखना सीखो",
                iconEmoji = "✍️",
                isUnlocked = unlockedLevel >= 2,
                starsEarned = getStars(2),
                xpReward = 80,
                completionPercent = getPercent(2)
            ),
            LevelInfo(
                levelNumber = 3,
                titleHindi = "लेवल 3: दो अक्षर जोड़कर शब्द बनाओ",
                subtitleHindi = "दो अक्षरों को मिलाओ और नया शब्द खोजो",
                iconEmoji = "🧩",
                isUnlocked = unlockedLevel >= 3,
                starsEarned = getStars(3),
                xpReward = 100,
                completionPercent = getPercent(3)
            ),
            LevelInfo(
                levelNumber = 4,
                titleHindi = "लेवल 4: मजेदार क्विज और पहेलियाँ",
                subtitleHindi = "सवालों के सही जवाब दो और सितारे जीतो",
                iconEmoji = "🎯",
                isUnlocked = unlockedLevel >= 4,
                starsEarned = getStars(4),
                xpReward = 120,
                completionPercent = getPercent(4)
            ),
            LevelInfo(
                levelNumber = 5,
                titleHindi = "लेवल 5: महा मुकाबला और ट्रॉफी",
                subtitleHindi = "सब कुछ याद करके मास्टर बैज अनलॉक करो",
                iconEmoji = "🏆",
                isUnlocked = unlockedLevel >= 5,
                starsEarned = getStars(5),
                xpReward = 200,
                completionPercent = getPercent(5)
            )
        )
    }

    // 2. Alphabet Cards (Level 1)
    fun getAlphabetCards(subjectId: String): List<AlphabetCard> {
        val dynamicCards = ContentRepository.getDynamicAlphabetCards(subjectId)
        if (dynamicCards.isNotEmpty()) {
            return dynamicCards
        }
        return when (subjectId.lowercase()) {
            "hindi" -> listOf(
                // ---- स्वर (Vowels) ----
                AlphabetCard(
                    id = "hi_a",
                    char = "अ",
                    wordHindi = "अनार",
                    wordEnglish = "Pomegranate",
                    emoji = "🍎",
                    phonetic = "A",
                    hindiAudioSentence = "अ से अनार! अनार के दाने लाल लाल और बहुत मीठे होते हैं!",
                    funFactHindi = "अनार खाने से सेहत अच्छी होती है और खून बढ़ता है।"
                ),
                AlphabetCard(
                    id = "hi_aa",
                    char = "आ",
                    wordHindi = "आम",
                    wordEnglish = "Mango",
                    emoji = "🥭",
                    phonetic = "Aa",
                    hindiAudioSentence = "आ से आम! आम फलों का राजा है!",
                    funFactHindi = "गर्मियों में मीठा-मीठा रसीला आम सबको पसंद आता है।"
                ),
                AlphabetCard(
                    id = "hi_i",
                    char = "इ",
                    wordHindi = "इमली",
                    wordEnglish = "Tamarind",
                    emoji = "🌿",
                    phonetic = "I",
                    hindiAudioSentence = "इ से इमली! खट्टी मीठी इमली!",
                    funFactHindi = "इमली की चटनी बहुत स्वादिष्ट बनती है।"
                ),
                AlphabetCard(
                    id = "hi_ee",
                    char = "ई",
                    wordHindi = "ईख",
                    wordEnglish = "Sugarcane",
                    emoji = "🎋",
                    phonetic = "Ee",
                    hindiAudioSentence = "ई से ईख! ईख मतलब मीठा गन्ना!",
                    funFactHindi = "ईख के रस से मीठा गुड़ और चीनी बनती है।"
                ),
                AlphabetCard(
                    id = "hi_u",
                    char = "उ",
                    wordHindi = "उल्लू",
                    wordEnglish = "Owl",
                    emoji = "🦉",
                    phonetic = "U",
                    hindiAudioSentence = "उ से उल्लू! रात को जागे उल्लू भाई!",
                    funFactHindi = "उल्लू रात के अंधेरे में भी बहुत साफ देख सकता है।"
                ),
                AlphabetCard(
                    id = "hi_oo",
                    char = "ऊ",
                    wordHindi = "ऊन",
                    wordEnglish = "Wool",
                    emoji = "🧶",
                    phonetic = "Oo",
                    hindiAudioSentence = "ऊ से ऊन! ऊन से बनते हैं गर्म-गर्म स्वेटर!",
                    funFactHindi = "सर्दियों में ऊनी कपड़े हमें ठंड से बचाते हैं।"
                ),
                AlphabetCard(
                    id = "hi_ri",
                    char = "ऋ",
                    wordHindi = "ऋषि",
                    wordEnglish = "Sage",
                    emoji = "🧘",
                    phonetic = "Ri",
                    hindiAudioSentence = "ऋ से ऋषि! ज्ञान और तपस्या की मूरत ऋषि मुनि!",
                    funFactHindi = "ऋषि मुनि प्रकृति के बीच शांति से ध्यान लगाते हैं।"
                ),
                AlphabetCard(
                    id = "hi_e",
                    char = "ए",
                    wordHindi = "एड़ी",
                    wordEnglish = "Heel",
                    emoji = "🦶",
                    phonetic = "E",
                    hindiAudioSentence = "ए से एड़ी! हमारे पैरों की मजबूत एड़ी!",
                    funFactHindi = "एड़ी के सहारे हम सीधे खड़े होते हैं और दौड़ते हैं।"
                ),
                AlphabetCard(
                    id = "hi_ai",
                    char = "ऐ",
                    wordHindi = "ऐनक",
                    wordEnglish = "Spectacles",
                    emoji = "👓",
                    phonetic = "Ai",
                    hindiAudioSentence = "ऐ से ऐनक! दादा जी की प्यारी ऐनक!",
                    funFactHindi = "ऐनक लगाने से छोटी चीजें भी साफ दिखाई देती हैं।"
                ),
                AlphabetCard(
                    id = "hi_o",
                    char = "ओ",
                    wordHindi = "ओखली",
                    wordEnglish = "Mortar",
                    emoji = "🥣",
                    phonetic = "O",
                    hindiAudioSentence = "ओ से ओखली! मसाले कूटने वाली पत्थर की ओखली!",
                    funFactHindi = "ओखली में कुटे मसाले खाने में स्वाद बढ़ाते हैं।"
                ),
                AlphabetCard(
                    id = "hi_au",
                    char = "औ",
                    wordHindi = "औरत",
                    wordEnglish = "Woman",
                    emoji = "👩",
                    phonetic = "Au",
                    hindiAudioSentence = "औ से औरत! सबकी देखभाल करने वाली प्यारी मां!",
                    funFactHindi = "माता-पिता का आदर करना सबसे अच्छी आदत है।"
                ),
                AlphabetCard(
                    id = "hi_am",
                    char = "अं",
                    wordHindi = "अंगूर",
                    wordEnglish = "Grapes",
                    emoji = "🍇",
                    phonetic = "Am",
                    hindiAudioSentence = "अं से अंगूर! गुच्छों में लटके मीठे-मीठे अंगूर!",
                    funFactHindi = "अंगूर काले और हरे दोनों रंगों के होते हैं।"
                ),
                AlphabetCard(
                    id = "hi_ah",
                    char = "अः",
                    wordHindi = "अः (खाली)",
                    wordEnglish = "Aha (Empty)",
                    emoji = "👏",
                    phonetic = "Ah",
                    hindiAudioSentence = "अः से होता है खाली! मिलकर बजाओ बच्चों ताली!",
                    funFactHindi = "यहाँ हमारे सभी स्वर पूरे होते हैं!"
                ),

                // ---- व्यंजन (Consonants) - क वर्ग ----
                AlphabetCard(
                    id = "hi_k",
                    char = "क",
                    wordHindi = "कबूतर",
                    wordEnglish = "Pigeon",
                    emoji = "🕊️",
                    phonetic = "Ka",
                    hindiAudioSentence = "क से कबूतर! गुटर गूँ करता शांत कबूतर!",
                    funFactHindi = "कबूतर आसमान में बहुत ऊँची और दूर तक उड़ान भरता है।"
                ),
                AlphabetCard(
                    id = "hi_kh",
                    char = "ख",
                    wordHindi = "खरगोश",
                    wordEnglish = "Rabbit",
                    emoji = "🐰",
                    phonetic = "Kha",
                    hindiAudioSentence = "ख से खरगोश! प्यारा, सफेद और फुर्तीला खरगोश!",
                    funFactHindi = "खरगोश को लाल-लाल गाजर खाना बहुत पसंद है।"
                ),
                AlphabetCard(
                    id = "hi_g",
                    char = "ग",
                    wordHindi = "गमला",
                    wordEnglish = "Flower Pot",
                    emoji = "🪴",
                    phonetic = "Ga",
                    hindiAudioSentence = "ग से गमला! गमले में खिले सुंदर रंग-बिरंगे फूल!",
                    funFactHindi = "पेड़-पौधे हमें ताजी और शुद्ध ऑक्सीजन देते हैं।"
                ),
                AlphabetCard(
                    id = "hi_gh",
                    char = "घ",
                    wordHindi = "घर",
                    wordEnglish = "House",
                    emoji = "🏠",
                    phonetic = "Gha",
                    hindiAudioSentence = "घ से घर! हमारा प्यारा और खुशहाल घर!",
                    funFactHindi = "घर में हम अपने पूरे परिवार के साथ मिल-जुलकर रहते हैं।"
                ),
                AlphabetCard(
                    id = "hi_nga",
                    char = "ङ",
                    wordHindi = "ङ (खाली)",
                    wordEnglish = "Nga",
                    emoji = "🔔",
                    phonetic = "Nga",
                    hindiAudioSentence = "ङ से होता है खाली! क वर्ग हुआ पूरा, बजाओ ताली!",
                    funFactHindi = "ङ पंचमाक्षर कहलाता है।"
                ),

                // ---- व्यंजन - च वर्ग ----
                AlphabetCard(
                    id = "hi_ch",
                    char = "च",
                    wordHindi = "चम्मच",
                    wordEnglish = "Spoon",
                    emoji = "🥄",
                    phonetic = "Cha",
                    hindiAudioSentence = "च से चम्मच! खीर और हलवा खाने वाली चम्मच!",
                    funFactHindi = "चम्मच से खाना खाने से हाथ साफ रहते हैं।"
                ),
                AlphabetCard(
                    id = "hi_chh",
                    char = "छ",
                    wordHindi = "छतरी",
                    wordEnglish = "Umbrella",
                    emoji = "☂️",
                    phonetic = "Chha",
                    hindiAudioSentence = "छ से छतरी! बारिश और धूप से बचाने वाली रंगीन छतरी!",
                    funFactHindi = "बरसात में छतरी लगाने से हम भीगने से बचते हैं।"
                ),
                AlphabetCard(
                    id = "hi_j",
                    char = "ज",
                    wordHindi = "जहाज",
                    wordEnglish = "Ship",
                    emoji = "🚢",
                    phonetic = "Ja",
                    hindiAudioSentence = "ज से जहाज! समंदर की लहरों पर तैरता पानी का जहाज!",
                    funFactHindi = "पानी का जहाज बहुत बड़ा होता है और भारी सामान ले जाता है।"
                ),
                AlphabetCard(
                    id = "hi_jh",
                    char = "झ",
                    wordHindi = "झंडा",
                    wordEnglish = "Flag",
                    emoji = "🇮🇳",
                    phonetic = "Jha",
                    hindiAudioSentence = "झ से झंडा! हमारा प्यारा तिरंगा झंडा!",
                    funFactHindi = "हमारे राष्ट्रीय ध्वज में तीन रंग होते हैं: केसरिया, सफेद और हरा।"
                ),
                AlphabetCard(
                    id = "hi_nya",
                    char = "ञ",
                    wordHindi = "ञ (खाली)",
                    wordEnglish = "Nya",
                    emoji = "✨",
                    phonetic = "Nya",
                    hindiAudioSentence = "ञ से होता है खाली! च वर्ग हुआ पूरा, बजाओ ताली!",
                    funFactHindi = "ञ भी पंचम वर्ण है।"
                ),

                // ---- व्यंजन - ट वर्ग ----
                AlphabetCard(
                    id = "hi_t_hard",
                    char = "ट",
                    wordHindi = "टमाटर",
                    wordEnglish = "Tomato",
                    emoji = "🍅",
                    phonetic = "Ta",
                    hindiAudioSentence = "ट से टमाटर! गोल मटोल लाल टमाटर!",
                    funFactHindi = "टमाटर में विटामिन सी होता है जो इम्युनिटी बढ़ाता है।"
                ),
                AlphabetCard(
                    id = "hi_th_hard",
                    char = "ठ",
                    wordHindi = "ठठेरा",
                    wordEnglish = "Coppersmith",
                    emoji = "🔨",
                    phonetic = "Tha",
                    hindiAudioSentence = "ठ से ठठेरा! ठक-ठक बर्तन बनाने वाले ठठेरा!",
                    funFactHindi = "ठठेरा धातु के बर्तन बनाते हैं।"
                ),
                AlphabetCard(
                    id = "hi_d_hard",
                    char = "ड",
                    wordHindi = "डमरू",
                    wordEnglish = "Damru",
                    emoji = "🪘",
                    phonetic = "Da",
                    hindiAudioSentence = "ड से डमरू! डम-डम बाजे प्यारा डमरू!",
                    funFactHindi = "डमरू बजाने से बहुत मधुर संगीत निकलता है।"
                ),
                AlphabetCard(
                    id = "hi_dh_hard",
                    char = "ढ",
                    wordHindi = "ढोलक",
                    wordEnglish = "Dholak",
                    emoji = "🥁",
                    phonetic = "Dha",
                    hindiAudioSentence = "ढ से ढोलक! ढम-ढम बजती खुशियों की ढोलक!",
                    funFactHindi = "त्योहारों और शादी में ढोलक बजाई जाती है।"
                ),
                AlphabetCard(
                    id = "hi_na_hard",
                    char = "ण",
                    wordHindi = "बाण (ण)",
                    wordEnglish = "Arrow",
                    emoji = "🏹",
                    phonetic = "Nna",
                    hindiAudioSentence = "ण से बाण! तीर-कमान का लक्ष्य भेदने वाला बाण!",
                    funFactHindi = "ण वर्ण शब्दों के बीच या अंत में आता है।"
                ),

                // ---- व्यंजन - त वर्ग ----
                AlphabetCard(
                    id = "hi_t_soft",
                    char = "त",
                    wordHindi = "तरबूज",
                    wordEnglish = "Watermelon",
                    emoji = "🍉",
                    phonetic = "Ta",
                    hindiAudioSentence = "त से तरबूज! गर्मियों का ठंडा-ठंडा मीठा तरबूज!",
                    funFactHindi = "तरबूज में 90% पानी होता है जो शरीर को हाइड्रेटेड रखता है।"
                ),
                AlphabetCard(
                    id = "hi_th_soft",
                    char = "थ",
                    wordHindi = "थर्मस",
                    wordEnglish = "Thermos",
                    emoji = "🍶",
                    phonetic = "Tha",
                    hindiAudioSentence = "थ से थर्मस! गर्म दूध को गर्म रखे, ठंडे पानी को ठंडा!",
                    funFactHindi = "थर्मस में तापमान लंबे समय तक वैसा ही रहता है।"
                ),
                AlphabetCard(
                    id = "hi_d_soft",
                    char = "द",
                    wordHindi = "दीपक",
                    wordEnglish = "Diya",
                    emoji = "🪔",
                    phonetic = "Da",
                    hindiAudioSentence = "द से दीपक! अंधकार मिटाकर रोशनी फैलाने वाला दीपक!",
                    funFactHindi = "दिवाली पर हम पूरे घर में मिट्टी के दीपक जलाते हैं।"
                ),
                AlphabetCard(
                    id = "hi_dh_soft",
                    char = "ध",
                    wordHindi = "धनुष",
                    wordEnglish = "Bow",
                    emoji = "🏹",
                    phonetic = "Dha",
                    hindiAudioSentence = "ध से धनुष! मर्यादा पुरुषोत्तम श्री राम जी का धनुष!",
                    funFactHindi = "धनुष से एकाग्रता और लक्ष्य साधना सीखते हैं।"
                ),
                AlphabetCard(
                    id = "hi_n_soft",
                    char = "न",
                    wordHindi = "नल",
                    wordEnglish = "Tap",
                    emoji = "🚰",
                    phonetic = "Na",
                    hindiAudioSentence = "न से नल! पानी देता प्यारा नल, जल को कभी मत बहाओ!",
                    funFactHindi = "जल ही जीवन है, इसलिए नल बंद रखना चाहिए।"
                ),

                // ---- व्यंजन - प वर्ग ----
                AlphabetCard(
                    id = "hi_p",
                    char = "प",
                    wordHindi = "पतंग",
                    wordEnglish = "Kite",
                    emoji = "🪁",
                    phonetic = "Pa",
                    hindiAudioSentence = "प से पतंग! हवा में गोते लगाती रंग-बिरंगी पतंग!",
                    funFactHindi = "मकर संक्रांति पर आसमान में हजारों पतंगें उड़ती हैं।"
                ),
                AlphabetCard(
                    id = "hi_ph",
                    char = "फ",
                    wordHindi = "फल",
                    wordEnglish = "Fruits",
                    emoji = "🍎",
                    phonetic = "Pha",
                    hindiAudioSentence = "फ से फल! ताजे-ताजे फल खाओ, तंदुरुस्त बन जाओ!",
                    funFactHindi = "रोज ताजे फल खाने से शरीर को सभी विटामिन्स मिलते हैं।"
                ),
                AlphabetCard(
                    id = "hi_b",
                    char = "ब",
                    wordHindi = "बस",
                    wordEnglish = "Bus",
                    emoji = "🚌",
                    phonetic = "Ba",
                    hindiAudioSentence = "ब से बस! स्कूल ले जाने वाली प्यारी पीली बस!",
                    funFactHindi = "बस में हम दोस्तों के साथ सफर का आनंद लेते हैं।"
                ),
                AlphabetCard(
                    id = "hi_bh",
                    char = "भ",
                    wordHindi = "भालू",
                    wordEnglish = "Bear",
                    emoji = "🐻",
                    phonetic = "Bha",
                    hindiAudioSentence = "भ से भालू! जंगल में झूमता हुआ प्यारा मोटा भालू!",
                    funFactHindi = "भालू को मीठा-मीठा शहद खाना बहुत पसंद होता है।"
                ),
                AlphabetCard(
                    id = "hi_m",
                    char = "म",
                    wordHindi = "मछली",
                    wordEnglish = "Fish",
                    emoji = "🐟",
                    phonetic = "Ma",
                    hindiAudioSentence = "म से मछली! मछली जल की रानी है, जीवन उसका पानी है!",
                    funFactHindi = "मछलियां गलफड़ों (gills) से पानी के अंदर सांस लेती हैं।"
                ),

                // ---- अन्तःस्थ व ऊष्म व्यंजन ----
                AlphabetCard(
                    id = "hi_y",
                    char = "य",
                    wordHindi = "यज्ञ",
                    wordEnglish = "Yajna",
                    emoji = "🔥",
                    phonetic = "Ya",
                    hindiAudioSentence = "य से यज्ञ! पवित्र अग्नि और मंत्रों वाला पावन यज्ञ!",
                    funFactHindi = "यज्ञ करने से वातावरण शुद्ध और सुगंधित होता है।"
                ),
                AlphabetCard(
                    id = "hi_r",
                    char = "र",
                    wordHindi = "रथ",
                    wordEnglish = "Chariot",
                    emoji = "🎠",
                    phonetic = "Ra",
                    hindiAudioSentence = "र से रथ! घोड़ों से चलने वाला राजाओं का सुंदर रथ!",
                    funFactHindi = "प्राचीन काल में राजा रथ पर सवार होकर चलते थे।"
                ),
                AlphabetCard(
                    id = "hi_l",
                    char = "ल",
                    wordHindi = "लट्टू",
                    wordEnglish = "Spinning Top",
                    emoji = "🪀",
                    phonetic = "La",
                    hindiAudioSentence = "ल से लट्टू! जमीन पर गोल-गोल फिरकी की तरह नाचता लट्टू!",
                    funFactHindi = "लट्टू घुमाना बच्चों का बहुत पसंदीदा खेल है।"
                ),
                AlphabetCard(
                    id = "hi_v",
                    char = "व",
                    wordHindi = "वट (वृक्ष)",
                    wordEnglish = "Banyan Tree",
                    emoji = "🌳",
                    phonetic = "Va",
                    hindiAudioSentence = "व से वट! छाया देने वाला विशाल बरगद का पेड़!",
                    funFactHindi = "बरगद हमारा राष्ट्रीय वृक्ष है और सैकड़ों साल जीता है।"
                ),
                AlphabetCard(
                    id = "hi_sh",
                    char = "श",
                    wordHindi = "शलजम",
                    wordEnglish = "Turnip",
                    emoji = "🥗",
                    phonetic = "Sha",
                    hindiAudioSentence = "श से शलजम! सेहतमंद और स्वादिष्ट शलजम!",
                    funFactHindi = "शलजम का सलाद खाने से पाचन तंत्र अच्छा रहता है।"
                ),
                AlphabetCard(
                    id = "hi_shh",
                    char = "ष",
                    wordHindi = "षट्कोण",
                    wordEnglish = "Hexagon",
                    emoji = "⬡",
                    phonetic = "Shha",
                    hindiAudioSentence = "ष से षट्कोण! छह कोनों वाला सुंदर गणितीय आकार!",
                    funFactHindi = "षट्कोण में छह समान भुजाएं और छह कोण होते हैं।"
                ),
                AlphabetCard(
                    id = "hi_s",
                    char = "स",
                    wordHindi = "सूरज",
                    wordEnglish = "Sun",
                    emoji = "☀️",
                    phonetic = "Sa",
                    hindiAudioSentence = "स से सूरज! सुबह-सुबह सुनहरी रोशनी लेकर आता सूरज!",
                    funFactHindi = "सूरज से हमें धूप, ऊर्जा और विटामिन डी मिलता है।"
                ),
                AlphabetCard(
                    id = "hi_h",
                    char = "ह",
                    wordHindi = "हाथी",
                    wordEnglish = "Elephant",
                    emoji = "🐘",
                    phonetic = "Ha",
                    hindiAudioSentence = "ह से हाथी! सूंड हिलाता बड़ा और समझदार हाथी राजा!",
                    funFactHindi = "हाथी की याददाश्त बहुत तेज होती है।"
                ),

                // ---- संयुक्त व्यंजन ----
                AlphabetCard(
                    id = "hi_ksh",
                    char = "क्ष",
                    wordHindi = "क्षत्रिय",
                    wordEnglish = "Warrior",
                    emoji = "⚔️",
                    phonetic = "Ksha",
                    hindiAudioSentence = "क्ष से क्षत्रिय! देश और धर्म की रक्षा करने वाले वीर क्षत्रिय!",
                    funFactHindi = "क और ष मिलकर 'क्ष' बनता है।"
                ),
                AlphabetCard(
                    id = "hi_tr",
                    char = "त्र",
                    wordHindi = "त्रिशूल",
                    wordEnglish = "Trident",
                    emoji = "🔱",
                    phonetic = "Tra",
                    hindiAudioSentence = "त्र से त्रिशूल! तीन धारदार नोकों वाला पावन त्रिशूल!",
                    funFactHindi = "त और र मिलकर 'त्र' बनता है।"
                ),
                AlphabetCard(
                    id = "hi_gy",
                    char = "ज्ञ",
                    wordHindi = "ज्ञानी",
                    wordEnglish = "Wise / Scholar",
                    emoji = "📚",
                    phonetic = "Gya",
                    hindiAudioSentence = "ज्ञ से ज्ञानी! मन लगाकर पढ़ने वाले बच्चे बनते हैं ज्ञानी!",
                    funFactHindi = "ज और ञ मिलकर 'ज्ञ' बनता है। संपूर्ण वर्णमाला पूर्ण हुई!"
                )
            )

            "english" -> listOf(
                AlphabetCard(
                    id = "en_a",
                    char = "A",
                    wordHindi = "ऐपल (सेब)",
                    wordEnglish = "Apple",
                    emoji = "🍎",
                    phonetic = "ए / ऐ",
                    hindiAudioSentence = "A for Apple! ऐपल माने मीठा लाल सेब! रोज एक सेब खाओ, डॉक्टर को दूर भगाओ!",
                    funFactHindi = "An apple a day keeps the doctor away!"
                ),
                AlphabetCard(
                    id = "en_b",
                    char = "B",
                    wordHindi = "बॉल (गेंद)",
                    wordEnglish = "Ball",
                    emoji = "⚽",
                    phonetic = "बी / ब",
                    hindiAudioSentence = "B for Ball! बॉल मतलब खेलने वाली गोल गेंद!",
                    funFactHindi = "गेंद से हम बैट-बॉल और फुटबॉल खेल सकते हैं।"
                ),
                AlphabetCard(
                    id = "en_c",
                    char = "C",
                    wordHindi = "कैट (बिल्ली)",
                    wordEnglish = "Cat",
                    emoji = "🐱",
                    phonetic = "सी / क",
                    hindiAudioSentence = "C for Cat! कैट मतलब म्याऊं-म्याऊं करती बिल्ली मौसी!",
                    funFactHindi = "बिल्ली को दूध पीना बहुत पसंद होता है।"
                ),
                AlphabetCard(
                    id = "en_d",
                    char = "D",
                    wordHindi = "डॉग (कुत्ता)",
                    wordEnglish = "Dog",
                    emoji = "🐶",
                    phonetic = "डी / ड",
                    hindiAudioSentence = "D for Dog! डॉग मतलब वफादार कुत्ता!",
                    funFactHindi = "डॉग इंसानों का सबसे सच्चा दोस्त होता है।"
                ),
                AlphabetCard(
                    id = "en_e",
                    char = "E",
                    wordHindi = "एलिफेंट (हाथी)",
                    wordEnglish = "Elephant",
                    emoji = "🐘",
                    phonetic = "ई / ए",
                    hindiAudioSentence = "E for Elephant! एलिफेंट मतलब लंबी सूंड वाला बड़ा हाथी!",
                    funFactHindi = "हाथी जमीन पर रहने वाला सबसे विशाल जानवर है।"
                ),
                AlphabetCard(
                    id = "en_f",
                    char = "F",
                    wordHindi = "फिश (मछली)",
                    wordEnglish = "Fish",
                    emoji = "🐟",
                    phonetic = "एफ / फ",
                    hindiAudioSentence = "F for Fish! मछली जल की रानी है, जीवन उसका पानी है!",
                    funFactHindi = "मछलियां पानी के अंदर सांस ले सकती हैं।"
                ),
                AlphabetCard(
                    id = "en_g",
                    char = "G",
                    wordHindi = "ग्रेप्स (अंगूर)",
                    wordEnglish = "Grapes",
                    emoji = "🍇",
                    phonetic = "जी / ग",
                    hindiAudioSentence = "G for Grapes! ग्रेप्स मतलब मीठे-मीठे रसीले अंगूर!",
                    funFactHindi = "अंगूर गुच्छों में लगते हैं और काले व हरे दोनों होते हैं।"
                ),
                AlphabetCard(
                    id = "en_h",
                    char = "H",
                    wordHindi = "हॉर्स (घोड़ा)",
                    wordEnglish = "Horse",
                    emoji = "🐴",
                    phonetic = "एच / ह",
                    hindiAudioSentence = "H for Horse! हॉर्स मतलब सरपट दौड़ने वाला घोड़ा!",
                    funFactHindi = "घोड़ा खड़े-खड़े भी सो सकता है।"
                ),
                AlphabetCard(
                    id = "en_i",
                    char = "I",
                    wordHindi = "आइसक्रीम",
                    wordEnglish = "Ice Cream",
                    emoji = "🍦",
                    phonetic = "आई",
                    hindiAudioSentence = "I for Ice Cream! ठंडी-मीठी स्वादिष्ट आइसक्रीम!",
                    funFactHindi = "गर्मियों में आइसक्रीम खाना सबको भाता है।"
                ),
                AlphabetCard(
                    id = "en_j",
                    char = "J",
                    wordHindi = "जग",
                    wordEnglish = "Jug",
                    emoji = "🥛",
                    phonetic = "जे / ज",
                    hindiAudioSentence = "J for Jug! पानी और जूस भरने वाला जग!",
                    funFactHindi = "जग से हम गिलास में पानी डालते हैं।"
                ),
                AlphabetCard(
                    id = "en_k",
                    char = "K",
                    wordHindi = "काइट (पतंग)",
                    wordEnglish = "Kite",
                    emoji = "🪁",
                    phonetic = "के / क",
                    hindiAudioSentence = "K for Kite! काइट मतलब आसमान में उड़ती पतंग!",
                    funFactHindi = "हवा के साथ पतंग बहुत ऊँची उड़ती है।"
                ),
                AlphabetCard(
                    id = "en_l",
                    char = "L",
                    wordHindi = "लायन (शेर)",
                    wordEnglish = "Lion",
                    emoji = "🦁",
                    phonetic = "एल / ल",
                    hindiAudioSentence = "L for Lion! जंगल का राजा बब्बर शेर!",
                    funFactHindi = "शेर की दहाड़ कई किलोमीटर दूर तक सुनाई देती है।"
                ),
                AlphabetCard(
                    id = "en_m",
                    char = "M",
                    wordHindi = "मंकी (बंदर)",
                    wordEnglish = "Monkey",
                    emoji = "🐵",
                    phonetic = "एम / म",
                    hindiAudioSentence = "M for Monkey! डाल-डाल कूदने वाला नटखट बंदर!",
                    funFactHindi = "बंदर को मीठा केला खाना बहुत पसंद है।"
                ),
                AlphabetCard(
                    id = "en_n",
                    char = "N",
                    wordHindi = "नेस्ट (घोंसला)",
                    wordEnglish = "Nest",
                    emoji = "🪺",
                    phonetic = "एन / न",
                    hindiAudioSentence = "N for Nest! चिड़िया का प्यारा तिनकों वाला घोंसला!",
                    funFactHindi = "चिड़िया घोंसले में अपने अंडे और बच्चे सुरक्षित रखती है।"
                ),
                AlphabetCard(
                    id = "en_o",
                    char = "O",
                    wordHindi = "ऑरेंज (संतरा)",
                    wordEnglish = "Orange",
                    emoji = "🍊",
                    phonetic = "ओ",
                    hindiAudioSentence = "O for Orange! खट्टा-मीठा रसीला संतरा!",
                    funFactHindi = "संतरे में विटामिन सी भरपूर मात्रा में होता है।"
                ),
                AlphabetCard(
                    id = "en_p",
                    char = "P",
                    wordHindi = "पैरट (तोता)",
                    wordEnglish = "Parrot",
                    emoji = "🦜",
                    phonetic = "पी / प",
                    hindiAudioSentence = "P for Parrot! हरी-हरी मिर्च खाने वाला हरा तोता!",
                    funFactHindi = "तोता इंसानों की तरह बोलना भी सीख सकता है।"
                ),
                AlphabetCard(
                    id = "en_q",
                    char = "Q",
                    wordHindi = "क्वीन (रानी)",
                    wordEnglish = "Queen",
                    emoji = "👸",
                    phonetic = "क्यू / क्व",
                    hindiAudioSentence = "Q for Queen! सुंदर ताज पहनने वाली दयालु रानी!",
                    funFactHindi = "रानी अपने राज्य के लोगों से बहुत प्यार करती है।"
                ),
                AlphabetCard(
                    id = "en_r",
                    char = "R",
                    wordHindi = "रेनबो (इंद्रधनुष)",
                    wordEnglish = "Rainbow",
                    emoji = "🌈",
                    phonetic = "आर / र",
                    hindiAudioSentence = "R for Rainbow! बारिश के बाद आसमान में चमकता सात रंगों का इंद्रधनुष!",
                    funFactHindi = "रेनबो में सात रंग होते हैं: बैंगनी, नीला, आसमानी, हरा, पीला, नारंगी, लाल।"
                ),
                AlphabetCard(
                    id = "en_s",
                    char = "S",
                    wordHindi = "सन (सूरज)",
                    wordEnglish = "Sun",
                    emoji = "☀️",
                    phonetic = "एस / स",
                    hindiAudioSentence = "S for Sun! दुनिया को रोशनी देने वाला चमकता सूरज!",
                    funFactHindi = "सूरज पूर्व दिशा से उगता है और पश्चिम में छिपता है।"
                ),
                AlphabetCard(
                    id = "en_t",
                    char = "T",
                    wordHindi = "ट्री (पेड़)",
                    wordEnglish = "Tree",
                    emoji = "🌳",
                    phonetic = "टी / ट",
                    hindiAudioSentence = "T for Tree! शीतल छाया और मीठे फल देने वाला पेड़!",
                    funFactHindi = "पेड़ हमें साफ हवा देते हैं, हमें पौधे लगाने चाहिए।"
                ),
                AlphabetCard(
                    id = "en_u",
                    char = "U",
                    wordHindi = "अंब्रेला (छतरी)",
                    wordEnglish = "Umbrella",
                    emoji = "☂️",
                    phonetic = "यू / अ",
                    hindiAudioSentence = "U for Umbrella! बारिश से बचाने वाली रंगीन छतरी!",
                    funFactHindi = "छतरी हमें भीगने से बचाती है।"
                ),
                AlphabetCard(
                    id = "en_v",
                    char = "V",
                    wordHindi = "वैन (गाड़ी)",
                    wordEnglish = "Van",
                    emoji = "🚐",
                    phonetic = "वी / व",
                    hindiAudioSentence = "V for Van! स्कूल और सैर पर ले जाने वाली वैन!",
                    funFactHindi = "वैन में चार पहिए होते हैं।"
                ),
                AlphabetCard(
                    id = "en_w",
                    char = "W",
                    wordHindi = "वॉच (घड़ी)",
                    wordEnglish = "Watch",
                    emoji = "⌚",
                    phonetic = "डब्ल्यू / व",
                    hindiAudioSentence = "W for Watch! टिक-टिक करके सही समय बताने वाली घड़ी!",
                    funFactHindi = "समय का सदुपयोग करना बहुत जरूरी है।"
                ),
                AlphabetCard(
                    id = "en_x",
                    char = "X",
                    wordHindi = "ज़ाइलोफ़ोन (वाद्य यंत्र)",
                    wordEnglish = "Xylophone",
                    emoji = "🎵",
                    phonetic = "एक्स",
                    hindiAudioSentence = "X for Xylophone! मधुर संगीत बजाने वाला रंग-बिरंगा बाजा!",
                    funFactHindi = "इस पर छड़ी से मारने पर मीठी धुन निकलती है।"
                ),
                AlphabetCard(
                    id = "en_y",
                    char = "Y",
                    wordHindi = "याक (पहाड़ी बैल)",
                    wordEnglish = "Yak",
                    emoji = "🐂",
                    phonetic = "वाई / य",
                    hindiAudioSentence = "Y for Yak! बर्फीले पहाड़ों पर रहने वाला लंबे बालों वाला याक!",
                    funFactHindi = "याक ठंड में भी आराम से रह सकता है।"
                ),
                AlphabetCard(
                    id = "en_z",
                    char = "Z",
                    wordHindi = "ज़ेबरा",
                    wordEnglish = "Zebra",
                    emoji = "🦓",
                    phonetic = "ज़ेड / ज़",
                    hindiAudioSentence = "Z for Zebra! काली और सफेद धारियों वाला सुंदर ज़ेबरा!",
                    funFactHindi = "हर ज़ेबरा की धारियां बिल्कुल अलग और अनोखी होती हैं।"
                )
            )

            else -> getMathCards1To100()
        }
    }

    private val hindiNumbers1To100 = listOf(
        "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ", "दस",
        "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस", "बीस",
        "इक्कीस", "बाईस", "तेईस", "चौबीस", "पच्चीस", "छब्बीस", "सत्ताईस", "अट्ठाईस", "उनतीस", "तीस",
        "इकतीस", "बत्तीस", "तैंतीस", "चौंतीस", "पैंतीस", "छत्तीस", "सैंतीस", "अड़तीस", "उनतालीस", "चालीस",
        "इकतालीस", "बयालीस", "तैंतालीस", "चवालीस", "पैंतालीस", "छियालीस", "सैंतालीस", "अड़तालीस", "उनचास", "पचास",
        "इक्यावन", "बावन", "तिरपन", "चौवन", "बचपन", "छप्पन", "सत्तावन", "अट्ठावन", "उनसठ", "साठ",
        "इकसठ", "बासठ", "तिरसठ", "चौंसठ", "पैंसठ", "छियासठ", "सड़सठ", "अड़सठ", "उनहत्तर", "सत्तर",
        "इकहत्तर", "बहत्तर", "तिहत्तर", "चौहत्तर", "पचहत्तर", "छिहत्तर", "सतहत्तर", "अठहत्तर", "उन्यासी", "अस्सी",
        "इक्यासी", "बयासी", "तिरासी", "चौरासी", "पचासी", "छियासी", "सत्तासी", "अट्ठासी", "नवासी", "नब्बे",
        "इक्यानवे", "बावनवे", "तिरानवे", "चौरानवे", "पंचानवे", "छियानवे", "सत्तानवे", "अट्ठानवे", "निन्यानवे", "सौ"
    )

    private val englishNumbers1To100 = listOf(
        "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty",
        "Twenty One", "Twenty Two", "Twenty Three", "Twenty Four", "Twenty Five", "Twenty Six", "Twenty Seven", "Twenty Eight", "Twenty Nine", "Thirty",
        "Thirty One", "Thirty Two", "Thirty Three", "Thirty Four", "Thirty Five", "Thirty Six", "Thirty Seven", "Thirty Eight", "Thirty Nine", "Forty",
        "Forty One", "Forty Two", "Forty Three", "Forty Four", "Forty Five", "Forty Six", "Forty Seven", "Forty Eight", "Forty Nine", "Fifty",
        "Fifty One", "Fifty Two", "Fifty Three", "Fifty Four", "Fifty Five", "Fifty Six", "Fifty Seven", "Fifty Eight", "Fifty Nine", "Sixty",
        "Sixty One", "Sixty Two", "Sixty Three", "Sixty Four", "Sixty Five", "Sixty Six", "Sixty Seven", "Sixty Eight", "Sixty Nine", "Seventy",
        "Seventy One", "Seventy Two", "Seventy Three", "Seventy Four", "Seventy Five", "Seventy Six", "Seventy Seven", "Seventy Eight", "Seventy Nine", "Eighty",
        "Eighty One", "Eighty Two", "Eighty Three", "Eighty Four", "Eighty Five", "Eighty Six", "Eighty Eight", "Eighty Eight", "Eighty Nine", "Ninety",
        "Ninety One", "Ninety Two", "Ninety Three", "Ninety Four", "Ninety Five", "Ninety Six", "Ninety Seven", "Ninety Eight", "Ninety Nine", "One Hundred"
    )

    private data class NumberExample(
        val hiExample: String,
        val enExample: String,
        val emoji: String,
        val audioSentence: String,
        val funFact: String
    )

    private val numberExamples1To100 = listOf(
        NumberExample("1 चमकता सूरज", "One Sun", "☀️", "१ - यह है संख्या एक! आसमान में चमकता हुआ एक सूरज!", "हमारे सौर मंडल में केवल एक ही सूर्य है।"),
        NumberExample("2 प्यारी आँखें", "Two Eyes", "👀", "२ - यह है संख्या दो! हमारी देखने वाली दो प्यारी आंखें!", "आँखों की मदद से हम पूरी दुनिया के रंग देखते हैं।"),
        NumberExample("3 पहियों का रिक्शा", "Three Wheels Rickshaw", "🛺", "३ - यह है संख्या तीन! ऑटो रिक्शा के होते हैं तीन पहिए!", "ट्रैफिक लाइट में भी तीन रंग होते हैं: लाल, पीला और हरा।"),
        NumberExample("4 पायों की मेज", "Four Legs Table", "🪑", "४ - यह है संख्या चार! मेज और कुर्सी के होते हैं चार पाए!", "गाय, घोड़ा और हाथी सभी के चार पैर होते हैं।"),
        NumberExample("5 उंगलियाँ", "Five Fingers", "🖐️", "५ - यह है संख्या पाँच! हमारे एक हाथ में होती हैं पाँच उंगलियाँ!", "पाँच उंगलियाँ मिलकर एक मजबूत मुट्ठी बनाती हैं।"),
        NumberExample("6 पैरों वाली चींटी", "Six Legs Ant", "🐜", "६ - यह है संख्या छह! नन्हीं चींटी के होते हैं छह पैर!", "सभी कीड़ों और तितलियों के छह पैर होते हैं।"),
        NumberExample("7 रंगों का इंद्रधनुष", "Seven Colors Rainbow", "🌈", "७ - यह है संख्या सात! इंद्रधनुष में होते हैं सात प्यारे रंग!", "सात सुर मिलकर संगीत की सरगम बनाते हैं: सा रे ग म प ध नि।"),
        NumberExample("8 भुजाओं वाला ऑक्टोपस", "Eight Arms Octopus", "🐙", "८ - यह है संख्या आठ! ऑक्टोपस की होती हैं आठ भुजाएं!", "ऑक्टोपस के आठ हाथ और तीन दिल होते हैं।"),
        NumberExample("9 अनमोल नवरत्न", "Nine Gems Navratna", "💎", "९ - यह है संख्या नौ! राजा के नौ अनमोल नवरत्न!", "नौ सबसे बड़ी एकल अंक संख्या है।"),
        NumberExample("10 दोनों हाथों की उंगलियाँ", "Ten Fingers", "🙌", "१० - यह है संख्या दस! दोनों हाथों की कुल दस उंगलियाँ!", "दस मिलकर एक दहाई बनती है।"),

        NumberExample("11 क्रिकेट के खिलाड़ी", "Eleven Cricket Players", "🏏", "११ - ग्यारह! क्रिकेट की एक टीम में होते हैं ११ खिलाड़ी!", "क्रिकेट और फुटबॉल दोनों में ११-११ खिलाड़ी खेलते हैं।"),
        NumberExample("12 केले (एक दर्जन)", "Twelve Bananas - One Dozen", "🍌", "१२ - बारह! एक दर्जन में होते हैं १२ मीठे केले!", "एक साल में १२ महीने होते हैं और एक दर्जन में १२ वस्तुएं।"),
        NumberExample("13 गरम रोटियां", "Thirteen Fresh Breads", "🥖", "१३ - तेरह! टोकरी में रखीं १३ गरम-गरम रोटियां!", "१३ को बेकर्स दर्जन भी कहा जाता है।"),
        NumberExample("14वीं का पूरा चाँद", "Fourteen - Full Moon", "🌕", "१४ - चौदह! आकाश में चमकता चौदहवीं का सुंदर गोल चाँद!", "चौदह दिनों में चंद्रमा धीरे-धीरे पूरा गोल हो जाता है।"),
        NumberExample("15 अगस्त तिरंगा", "Fifteen - August Independence", "🇮🇳", "१५ - पंद्रह! १५ अगस्त स्वतंत्रता दिवस पर फहराते हैं तिरंगा!", "१५ अगस्त १९४७ को हमारा प्यारा भारत देश आजाद हुआ था।"),
        NumberExample("16 रंग-बिरंगे फूल", "Sixteen Colorful Flowers", "🌸", "१६ - सोलह! बगीचे में खिले १६ महकते रंग-बिरंगे फूल!", "सोलह एक पूर्ण वर्ग संख्या है (४ x ४ = १६)।"),
        NumberExample("17 नटखट तोते", "Seventeen Green Parrots", "🦜", "१७ - सत्रह! पेड़ की डाल पर बैठे १७ नटखट तोते!", "तोता इंसानों की मीठी बोली की नकल कर सकता है।"),
        NumberExample("18 साल वोट की उम्र", "Eighteen - Voting Age", "🗳️", "१८ - अठारह! १८ वर्ष की उम्र में मिलता है मतदान का अधिकार!", "१८ साल की उम्र में नागरिक देश का जिम्मेदार वोटर बनता है।"),
        NumberExample("19 खिलौना कारें", "Nineteen Toy Cars", "🚗", "१९ - उन्नीस! खेलने के लिए सजीं १९ सुंदर रंगीन कारें!", "उन्नीस बीस से ठीक एक कम होता है।"),
        NumberExample("20 हाथ-पैर की उंगलियाँ", "Twenty Fingers & Toes", "🦶", "२० - बीस! हाथ और पैर दोनों मिलाकर कुल २० उंगलियाँ!", "हमारे हाथों और पैरों की उंगलियों की कुल संख्या २० होती है।"),

        NumberExample("21 शगुन के सिक्के", "Twenty-One Lucky Coins", "🪙", "२१ - इक्कीस! शगुन के लिफाफे में रखे २१ चमकदार सिक्के!", "२१ को भारत में बहुत शुभ और मंगलकारी माना जाता है।"),
        NumberExample("22 तैरती बत्तखें", "Twenty-Two Swimming Ducks", "🦆", "२२ - बाईस! नीले तालाब में तैरतीं २२ प्यारी बत्तखें!", "बत्तख के पंख वाटरप्रूफ होते हैं, वे पानी में नहीं भीगते।"),
        NumberExample("23 चमकीले सितारे", "Twenty-Three Shining Stars", "⭐", "२३ - तेईस! रात के आकाश में टिमटिमाते २३ चमकीले सितारे!", "मनुष्य के शरीर में २३ जोड़े गुणसूत्र होते हैं।"),
        NumberExample("24 घंटे और चक्र की तीलियां", "Twenty-Four Hours & Spokes", "⏰", "२४ - चौबीस! एक दिन-रात में २४ घंटे और अशोक चक्र में २४ तीलियां!", "समय का पहिया हमेशा २४ घंटे आगे बढ़ता रहता है।"),
        NumberExample("25 सिल्वर जुबली", "Twenty-Five Silver Jubilee", "🥈", "२५ - पच्चीस! २५ साल पूरे होने पर मनाते हैं सिल्वर जुबली!", "२५ मिलकर १०० का एक-चौथाई हिस्सा बनाते हैं।"),
        NumberExample("26 जनवरी गणतंत्र दिवस", "Twenty-Six - Republic Day", "🎖️", "२६ - छब्बीस! २६ जनवरी को हम मनाते हैं गणतंत्र दिवस!", "अंग्रेजी वर्णमाला में कुल २६ अक्षर होते हैं।"),
        NumberExample("27 आकाशीय नक्षत्र", "Twenty-Seven Constellations", "✨", "२७ - सत्ताईस! आकाश में तारों के २७ विशेष नक्षत्र!", "भारतीय खगोल विज्ञान में २७ नक्षत्र माने गए हैं।"),
        NumberExample("28 दिन फरवरी के", "Twenty-Eight Days of Feb", "🗓️", "२८ - अट्ठाइस! सामान्य वर्ष में फरवरी महीने में होते हैं २८ दिन!", "फरवरी साल का सबसे छोटा महीना होता है।"),
        NumberExample("29 दिन लीप ईयर फरवरी", "Twenty-Nine Leap Year Days", "📅", "२९ - उनतीस! लीप वर्ष में फरवरी के होते हैं २९ दिन!", "हर चार साल में एक बार लीप ईयर आता है।"),
        NumberExample("30 दिन एक महीने में", "Thirty Days in a Month", "📆", "३० - तीस! ज्यादातर महीनों में होते हैं कुल ३० दिन!", "अप्रैल, जून, सितंबर और नवंबर में ३०-३० दिन होते हैं।"),

        NumberExample("31 दिन बड़े महीनों में", "Thirty-One Days in Long Months", "📅", "३१ - इकत्तीस! जनवरी और दिसंबर जैसे बड़े महीनों में होते हैं ३१ दिन!", "साल के सात महीनों में ३१ दिन होते हैं।"),
        NumberExample("32 मजबूत दाँत", "Thirty-Two Strong Teeth", "🦷", "३२ - बत्तीस! मुस्कुराते चेहरे में कुल ३२ मजबूत दाँत!", "एक वयस्क इंसान के मुंह में कुल ३२ स्थायी दांत होते हैं।"),
        NumberExample("33 मीठी टॉफियाँ", "Thirty-Three Sweet Candies", "🍬", "३३ - तैंतीस! जन्मदिन की पार्टी में बंटीं ३३ मीठी टॉफियाँ!", "३३ को ३ से भाग देने पर पूरा-पूरा ११ आता है।"),
        NumberExample("34 रसीले सेब", "Thirty-Four Juicy Apples", "🍎", "३४ - चौंतीस! फलों की टोकरी में रखे ३४ लाल-लाल सेब!", "रोजाना एक सेब खाने से सेहत बहुत अच्छी रहती है।"),
        NumberExample("35 रंगीन गुब्बारे", "Thirty-Five Colorful Balloons", "🎈", "३५ - पैंतीस! मेले में हवा में लहराते ३५ रंग-बिरंगे गुब्बारे!", "५ x ७ मिलकर पैंतीस बनते हैं।"),
        NumberExample("36 रंगीन पेंसिलें", "Thirty-Six Colored Pencils", "✏️", "३६ - छत्तीस! चित्रकारी के बॉक्स में सजीं ३६ रंगीन पेंसिलें!", "३६ छह का वर्ग है (६ x ६ = ३६)।"),
        NumberExample("37 उड़ती तितलियाँ", "Thirty-Seven Butterflies", "🦋", "३७ - सैंतीस! बगीचे में फूलों पर मंडरातीं ३७ सुंदर तितलियाँ!", "३७ एक अभाज्य संख्या (प्राइम नंबर) है।"),
        NumberExample("38 मीठे आम", "Thirty-Eight Sweet Mangoes", "🥭", "३८ - अड़तीस! पेड़ से तोड़े गए ३८ रसीले मीठे आम!", "आम को फलों का राजा कहा जाता है।"),
        NumberExample("39 रसीली स्ट्रॉबेरी", "Thirty-Nine Strawberries", "🍓", "३९ - उनतालीस! प्लेट में सजीं ३९ ताजी और मीठी स्ट्रॉबेरी!", "स्ट्रॉबेरी के बीज फल के बाहर होते हैं।"),
        NumberExample("40 पृष्ठों की किताब", "Forty Pages Book", "📖", "४० - चालीस! परियों की कहानी वाली ४० पन्नों की रंगीन किताब!", "४० चार दहाइयों (१० x ४ = ४०) के बराबर होता है।"),

        NumberExample("41 बजती घंटियाँ", "Forty-One Temple Bells", "🔔", "४१ - इकतालीस! मंदिर में टुनटुन बजतीं ४१ पवित्र घंटियाँ!", "घंटी की आवाज से मन प्रसन्न होता है।"),
        NumberExample("42 नन्हें कछुए", "Forty-Two Little Turtles", "🐢", "४२ - बयालीस! रेत पर धीरे-धीरे चलते ४२ नन्हें कछुए!", "कछुआ १०० से भी ज्यादा सालों तक जी सकता है।"),
        NumberExample("43 स्वादिष्ट डोनट्स", "Forty-Three Sweet Donuts", "🍩", "४३ - तैंतालीस! बेकरी में ताजे बने ४३ गोल-गोल मीठे डोनट्स!", "४३ एक अभाज्य संख्या है।"),
        NumberExample("44 वाटर कलर शेड्स", "Forty-Four Watercolor Shades", "🎨", "४४ - चवालीस! पेंटिंग किट में रखे ४४ जादुई वाटर कलर!", "४४ चार से पूरी तरह विभाजित होता है।"),
        NumberExample("45 मिनट का स्कूल पीरियड", "Forty-Five Minutes School Period", "⏱️", "४५ - पैंतालीस! स्कूल की कक्षा का ४५ मिनट का मजेदार पीरियड!", "४५ मिनट एक घंटे का तीन-चौथाई समय होता है।"),
        NumberExample("46 अंतरिक्ष रॉकेट", "Forty-Six Space Rockets", "🚀", "४६ - छियालीस! अंतरिक्ष की ओर उड़ते ४६ तेज रफ्तार रॉकेट!", "मानव शरीर की कोशिकाओं में ४६ क्रोमोसोम होते हैं।"),
        NumberExample("47 मीठे संतरे", "Forty-Seven Sweet Oranges", "🍊", "४७ - सैंतालीस! टोकरी में भरे ४७ रसदार और मीठे संतरे!", "संतरे में भरपूर विटामिन सी होता है।"),
        NumberExample("48 जंगल के शेर", "Forty-Eight Mighty Lions", "🦁", "४८ - अड़तालीस! हरे-भरे जंगल में दहाड़ते ४८ बलशाली शेर!", "शेर की दहाड़ बहुत दूर तक गूंजती है।"),
        NumberExample("49 प्यारे पांडा", "Forty-Nine Cute Pandas", "🐼", "४९ - उनचास! बांस खाते हुए ४९ प्यारे और गोल-मटोल पांडा!", "४९ सात का वर्ग है (७ x ७ = ४९)।"),
        NumberExample("50 रनों का अर्धशतक", "Fifty - Half Century", "🏏", "५० - पचास! बल्लेबाज ने पूरे किए शानदार ५० रन अर्धशतक!", "५० पूरे १०० का आधा (५०%) होता है।"),

        NumberExample("51 शगुन के उपहार", "Fifty-One Auspicious Gifts", "🎁", "५१ - इक्यावन! शादी और त्योहार पर दिए जाने वाले ५१ सुंदर उपहार!", "५१ को भारतीय परंपरा में शुभ अंक माना जाता है।"),
        NumberExample("52 ताश के पत्ते और सप्ताह", "Fifty-Two Cards & Weeks", "🃏", "५२ - बावन! साल में होते हैं ५२ सप्ताह और ताश में ५२ पत्ते!", "एक वर्ष में कुल ५२ सप्ताह होते हैं।"),
        NumberExample("53 बच्चों की साइकिलें", "Fifty-Three Kids Bicycles", "🚲", "५३ - तिरपन! पार्क में दौड़तीं ५३ रंगीन और तेज साइकिलें!", "साइकिल चलाना सेहत के लिए बहुत अच्छा व्यायाम है।"),
        NumberExample("54 मीठे तरबूज", "Fifty-Four Sweet Watermelons", "🍉", "५४ - चौवन! खेत में पके ५४ रसीले और ठंडे-मीठे तरबूज!", "तरबूज में लगभग ९२ प्रतिशत पानी होता है।"),
        NumberExample("55 स्वादिष्ट कपकेक्स", "Fifty-Five Sweet Cupcakes", "🧁", "५५ - पचपन! क्रीम और चेरी से सजे ५५ स्वादिष्ट कपकेक्स!", "५५ में दोनों अंक ५ और ५ एक समान हैं।"),
        NumberExample("56 प्रकार के छप्पन भोग", "Fifty-Six Royal Dishes", "🍛", "५६ - छप्पन! मंदिर में भगवान को अर्पित किए गए ५६ प्रकार के भोग!", "भारतीय संस्कृति में 'छप्पन भोग' बहुत प्रसिद्ध है।"),
        NumberExample("57 सुरीले गिटार", "Fifty-Seven Musical Guitars", "🎸", "५७ - सत्तावन! संगीत की कक्षा में बजते ५७ सुरीले गिटार!", "संगीत सुनने से मन शांत और खुश रहता है।"),
        NumberExample("58 उड़ते हवाई जहाज", "Fifty-Eight Flying Airplanes", "✈️", "५८ - अट्ठावन! बादलों के ऊपर उड़ते ५८ सफेद हवाई जहाज!", "हवाई जहाज दूर देशों की यात्रा बहुत आसान बनाते हैं।"),
        NumberExample("59 खिले हुए सूरजमुखी", "Fifty-Nine Golden Sunflowers", "🌻", "५९ - उनसठ! धूप में मुस्कुराते ५९ खिले हुए पीले सूरजमुखी!", "सूरजमुखी का फूल हमेशा सूरज की ओर मुंह करता है।"),
        NumberExample("60 सेकंड और मिनट", "Sixty Seconds & Minutes", "⏱️", "६० - साठ! एक मिनट में ६० सेकंड और एक घंटे में ६० मिनट!", "घड़ी की सुई ६० सेकंड पूरे करते ही एक मिनट बढ़ाती है।"),

        NumberExample("61 रसीले अंगूर", "Sixty-One Sweet Grapes", "🍇", "६१ - इकसठ! बेल पर लटके ६१ रसीले और मीठे अंगूर!", "अंगूर गुच्छों में लगते हैं और बहुत स्वादिष्ट होते हैं।"),
        NumberExample("62 मुलायम टेडी बियर", "Sixty-Two Teddy Bears", "🧸", "६२ - बासठ! खिलौने की दुकान में बैठे ६२ मुलायम टेडी बियर!", "टेडी बियर बच्चों का सबसे प्यारा दोस्त होता है।"),
        NumberExample("63 चंचल डॉल्फ़िन", "Sixty-Three Ocean Dolphins", "🐬", "६३ - तिरसठ! समंदर की लहरों पर कूदतीं ६३ चंचल डॉल्फ़िन!", "डॉल्फ़िन बहुत समझदार और मित्रवत जीव है।"),
        NumberExample("64 शतरंज बोर्ड के खाने", "Sixty-Four Chess Squares", "♟️", "६४ - चौंसठ! शतरंज के बोर्ड पर होते हैं कुल ६४ काले और सफेद खाने!", "शतरंज का खेल भारत में ही शुरू हुआ था।"),
        NumberExample("65 आइसक्रीम कोन", "Sixty-Five Ice Cream Cones", "🍦", "६५ - पैंसठ! ठंडी-मीठी वैनिला और चॉकलेट की ६५ आइसक्रीम!", "गर्मियों में ठंडी आइसक्रीम सबको बहुत भाती है।"),
        NumberExample("66 ट्रेन के डिब्बे", "Sixty-Six Train Wagons", "🚂", "६६ - छियासठ! पटरी पर छुक-छुक दौड़ती लंबी ट्रेन के ६६ डिब्बे!", "ट्रेन बहुत सारा सामान और यात्रियों को ले जाती है।"),
        NumberExample("67 खट्टे-मीठे अनानास", "Sixty-Seven Juicy Pineapples", "🍍", "६७ - सड़सठ! बाग में पके ६७ खट्टे-मीठे रसीले अनानास!", "अनानास पाचन तंत्र के लिए बहुत फायदेमंद होता है।"),
        NumberExample("68 नटखट पिल्ले", "Sixty-Eight Playful Puppies", "🐶", "६८ - अड़सठ! आंगन में गेंद से खेलते ६८ नटखट पिल्ले!", "कुत्ता बहुत वफादार और सच्चा साथी होता है।"),
        NumberExample("69 रंगीन छाते", "Sixty-Nine Rain Umbrellas", "☂️", "६९ - उनहत्तर! बारिश में खुले ६९ सुंदर और रंग-बिरंगे छाते!", "छाता हमें बारिश में भीगने से बचाता है।"),
        NumberExample("70 सुनहरी ट्रॉफियां", "Seventy Golden Trophies", "🏆", "७० - सत्तर! खेलकूद प्रतियोगिता में जीती गईं ७० सुनहरी ट्रॉफियां!", "७० सात दहाइयों (१० x ७ = ७०) के बराबर होता है।"),

        NumberExample("71 रसीली लाल चेरी", "Seventy-One Red Cherries", "🍒", "७१ - इकहत्तर! पेड़ पर लगीं ७१ चमकदार और मीठी लाल चेरी!", "चेरी का रंग बहुत सुंदर और चटक लाल होता है।"),
        NumberExample("72 दिल की धड़कनें", "Seventy-Two Heartbeats/Min", "❤️", "७२ - बहत्तर! १ मिनट में स्वस्थ दिल धड़कता है लगभग ७२ बार!", "हमारा दिल बिना रुके हमेशा धड़कता रहता है।"),
        NumberExample("73 मजबूत ट्रैक्टर", "Seventy-Three Farm Tractors", "🚜", "७३ - तिहत्तर! लहलहाते खेतों में जुताई करते ७३ मजबूत ट्रैक्टर!", "ट्रैक्टर किसानों का सबसे मददगार वाहन है।"),
        NumberExample("74 उड़ते हेलीकॉप्टर", "Seventy-Four Flying Helicopters", "🚁", "७४ - चौहत्तर! आसमान में पंख घुमाते ७४ तेज हेलीकॉप्टर!", "हेलीकॉप्टर किसी भी जगह सीधे उतर सकता है।"),
        NumberExample("75 अमृत महोत्सव", "Seventy-Five - Platinum Jubilee", "🥇", "७५ - पचहत्तर! ७५ साल पूरे होने पर मनाया जाता है हीरक जयंती अमृत महोत्सव!", "७५ पूरे १०० का तीन-चौथाई (३/४) हिस्सा होता है।"),
        NumberExample("76 गरमा-गरम भुट्टे", "Seventy-Six Sweet Corns", "🌽", "७६ - छिहत्तर! बारिश के मौसम में भुने गए ७६ गरमा-गरम भुट्टे!", "भुट्टा खाने से शरीर को ऊर्जा मिलती है।"),
        NumberExample("77 लाल-लाल गाजरें", "Seventy-Seven Red Carrots", "🥕", "७७ - सतहत्तर! खरगोश को बहुत पसंद आने वाली ७७ लाल गाजरें!", "गाजर खाने से आंखों की दृष्टि तेज होती है।"),
        NumberExample("78 प्यारी बिल्लियाँ", "Seventy-Eight Playful Cats", "🐱", "७८ - अठहत्तर! दूध पीती और म्याऊं करतीं ७८ प्यारी बिल्लियाँ!", "बिल्ली अंधेरे में भी बहुत साफ देख सकती है।"),
        NumberExample("79 चालाक लोमड़ियां", "Seventy-Nine Clever Foxes", "🦊", "७९ - उन्यासी! जंगल में छुपकर रहने वाली ७९ होशियार लोमड़ियां!", "लोमड़ी बहुत चतुर और फुर्तीली होती है।"),
        NumberExample("80 हवा में उड़ते बैलून", "Eighty Sky Balloons", "🎈", "८० - अस्सी! नीले गगन में दूर उड़ते ८० रंग-बिरंगे हॉट एयर बैलून!", "८० आठ दहाइयों (१० x ८ = ८०) के बराबर होता है।"),

        NumberExample("81 फुर्तीले खरगोश", "Eighty-One Jumping Rabbits", "🐰", "८१ - इक्यासी! हरी घास पर उछलते-कूदते ८१ सफेद खरगोश!", "८१ नौ का वर्ग है (९ x ९ = ८१)।"),
        NumberExample("82 कूदते मेंढक", "Eighty-Two Hopping Frogs", "🐸", "८२ - बयासी! बारिश में पानी में छलांग लगाते ८२ नन्हें मेंढक!", "मेंढक पानी और जमीन दोनों जगह रह सकते हैं।"),
        NumberExample("83 विशालकाय हाथी", "Eighty-Three Mighty Elephants", "🐘", "८३ - तिरासी! जंगल में सूंड उठाए चलते ८३ विशालकाय हाथी!", "हाथी जमीन पर रहने वाला सबसे बड़ा जानवर है।"),
        NumberExample("84 लंबे जिराफ", "Eighty-Four Tall Giraffes", "🦒", "८४ - चौरासी! ऊंची डालियों से पत्ते खाते ८४ लंबे जिराफ!", "जिराफ दुनिया का सबसे लंबा जानवर होता है।"),
        NumberExample("85 नटखट बंदर", "Eighty-Five Climbing Monkeys", "🐵", "८५ - पचासी! पेड़ों की डालियों पर झूलते ८५ नटखट बंदर!", "बंदर केले बड़े चाव से छीलकर खाते हैं।"),
        NumberExample("86 सुंदर ज़ेबरा", "Eighty-Six Striped Zebras", "🦓", "८६ - छियासी! काली और सफेद धारियों वाले ८६ सुंदर ज़ेबरा!", "हर ज़ेबरा के शरीर की धारियां अनोखी होती हैं।"),
        NumberExample("87 नारियल के पेड़", "Eighty-Seven Coconut Trees", "🥥", "८७ - सत्तासी! समुद्र किनारे लहराते ८७ ऊँचे नारियल के पेड़!", "नारियल का पानी बहुत पौष्टिक होता है।"),
        NumberExample("88 पियानो की कुंजियां", "Eighty-Eight Piano Keys", "🎹", "८८ - अट्ठासी! पियानो में सुरीली धुन निकालने वाली कुल ८८ कुंजियां!", "एक पूरे पियानो में कुल ८८ चाबियां होती हैं।"),
        NumberExample("89 ताजे अमरूद", "Eighty-Nine Fresh Guavas", "🍈", "८९ - नवासी! पेड़ से तोड़े गए ८९ ताजे और मीठे अमरूद!", "अमरूद में बहुत सारा विटामिन सी होता है।"),
        NumberExample("90 अंश का समकोण", "Ninety - Right Angle 90°", "📐", "९० - नब्बे! गणित में सीधा कोण होता है ठीक ९० अंश का समकोण!", "९० डिग्री के कोण को समकोण कहते हैं।"),

        NumberExample("91 रसीले आड़ू", "Ninety-One Juicy Peaches", "🍑", "९१ - इक्यानवे! बगीचे में पके ९१ मुलायम और मीठे आड़ू!", "आड़ू एक खुशबूदार और स्वादिष्ट पहाड़ी फल है।"),
        NumberExample("92 हरे एवोकाडो", "Ninety-Two Green Avocados", "🥑", "९२ - बावनवे! सेहत के लिए फायदेमंद ९२ ताजे हरे एवोकाडो!", "एवोकाडो दिल के लिए बहुत सेहतमंद होता है।"),
        NumberExample("93 क्रिस्पी पॉपकॉर्न", "Ninety-Three Popcorn Bags", "🍿", "९३ - तिरानवे! गरमा-गरम फूटे ९३ पैकेट कुरकुरे पॉपकॉर्न!", "मक्के के दाने गर्म होने पर पॉपकॉर्न बनते हैं।"),
        NumberExample("94 स्वादिष्ट कुकीज", "Ninety-Four Crispy Cookies", "🍪", "९४ - चौरानवे! ओवन में बेक की गईं ९४ स्वादिष्ट चॉकलेट कुकीज!", "दूध और कुकीज बच्चों को बहुत भाते हैं।"),
        NumberExample("95 मीठी चॉकलेट बार्स", "Ninety-Five Chocolate Bars", "🍫", "९५ - पंचानवे! बच्चों की मनपसंद ९५ मीठी चॉकलेट बार्स!", "चॉकलेट कोको के पौधों से बनाई जाती है।"),
        NumberExample("96 जन्मदिन के केक", "Ninety-Six Birthday Cakes", "🎂", "९६ - छियानवे! मोमबत्ती लगे हुए ९६ सुंदर और मीठे केक!", "जन्मदिन पर केक काटने से खुशियां बढ़ती हैं।"),
        NumberExample("97 मीठी लॉलीपॉप", "Ninety-Seven Sweet Lollipops", "🍭", "९७ - सत्तानवे! रंग-बिरंगी गोल और मीठी ९७ रसदार लॉलीपॉप!", "९७ दो अंकों की सबसे बड़ी अभाज्य संख्या है।"),
        NumberExample("98 शहद की शीशियाँ", "Ninety-Eight Honey Jars", "🍯", "९८ - अट्ठानवे! मधुमक्खियों के छत्ते से निकला ९८ शीशी शुद्ध शहद!", "शहद कभी खराब नहीं होता, हमेशा ताजा रहता है।"),
        NumberExample("99 सोने के सिक्के", "Ninety-Nine Golden Coins", "🪙", "९९ - निन्यानवे! खजाने के बक्से में रखे ९९ चमचमाते सोने के सिक्के!", "९९ दो अंकों की सबसे बड़ी संख्या है।"),
        NumberExample("100 पूरा शतक", "One Hundred - Full Century!", "💯", "१०० - सौ! बधाई हो! पूरे १०० रन बनाकर लग गया शानदार शतक!", "सौ तीन अंकों की सबसे पहली और सबसे छोटी संख्या है।")
    )

    fun getMathCards1To100(): List<AlphabetCard> {
        return (1..100).map { num ->
            val idx = num - 1
            val hiName = hindiNumbers1To100.getOrElse(idx) { "$num" }
            val enName = englishNumbers1To100.getOrElse(idx) { "$num" }
            val example = numberExamples1To100.getOrNull(idx)

            val exampleHi = example?.hiExample ?: "$num"
            val exampleEn = example?.enExample ?: "$enName $num"
            val emoji = example?.emoji ?: "🔢"
            val sentence = example?.audioSentence ?: "संख्या $num! $num मतलब $hiName!"
            val fact = example?.funFact ?: "संख्या $num बहुत महत्वपूर्ण है। चलो आगे सीखते हैं!"

            AlphabetCard(
                id = "ma_$num",
                char = "$num",
                wordHindi = "$hiName ($exampleHi)",
                wordEnglish = exampleEn,
                emoji = emoji,
                phonetic = hiName,
                hindiAudioSentence = sentence,
                funFactHindi = fact
            )
        }
    }

    // 3. Tracing Letters (Level 2) - Includes ALL alphabets for every subject
    fun getTracingLetters(subjectId: String): List<TracingLetter> {
        val cards = getAlphabetCards(subjectId)
        return cards.map { card ->
            val (hint, dots) = getTracingLetterDetails(card.char, subjectId)
            TracingLetter(
                char = card.char,
                subjectId = subjectId,
                titleHindi = when (subjectId.lowercase()) {
                    "hindi" -> "अक्षर '${card.char}' लिखो"
                    "english" -> "Letter '${card.char}' लिखो"
                    else -> "संख्या '${card.char}' लिखो"
                },
                hintHindi = hint,
                guideDots = dots,
                audioPromptHindi = when (subjectId.lowercase()) {
                    "hindi" -> "${card.char} से ${card.wordHindi}! चलो स्लेट पर '${card.char}' लिखना सीखें!"
                    "english" -> "${card.char} for ${card.wordEnglish}! Let's write '${card.char}' on the slate!"
                    else -> "संख्या ${card.char}! चलो स्लेट पर '${card.char}' लिखना सीखें!"
                }
            )
        }
    }

    private fun getTracingLetterDetails(char: String, subjectId: String): Pair<String, List<Pair<Float, Float>>> {
        return when (char) {
            // ---- स्वर (Vowels) ----
            "अ" -> Pair("ऊपर से गोल घुमाओ, बीच से लाइन, खड़ी लाइन और ऊपर छत बनाओ", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.32f), Pair(0.45f, 0.45f), Pair(0.58f, 0.55f), Pair(0.35f, 0.65f), Pair(0.45f, 0.45f), Pair(0.70f, 0.45f), Pair(0.70f, 0.20f), Pair(0.70f, 0.70f), Pair(0.30f, 0.20f), Pair(0.75f, 0.20f)))
            "आ" -> Pair("'अ' बनाकर पास में एक और सीधी खड़ी लाइन खींचो", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.32f), Pair(0.45f, 0.45f), Pair(0.60f, 0.45f), Pair(0.60f, 0.20f), Pair(0.60f, 0.70f), Pair(0.75f, 0.20f), Pair(0.75f, 0.70f), Pair(0.25f, 0.20f), Pair(0.80f, 0.20f)))
            "इ" -> Pair("छोटी खड़ी लाइन, 'S' जैसा मोड़ो और नीचे सुंदर पूंछ निकालो", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.30f), Pair(0.38f, 0.35f), Pair(0.62f, 0.50f), Pair(0.40f, 0.65f), Pair(0.32f, 0.75f)))
            "ई" -> Pair("'इ' बनाकर ऊपर की तरफ घुमावदार मात्रा लगाओ", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.30f), Pair(0.38f, 0.35f), Pair(0.62f, 0.50f), Pair(0.40f, 0.65f), Pair(0.32f, 0.75f), Pair(0.50f, 0.20f), Pair(0.65f, 0.12f)))
            "उ" -> Pair("ऊपर आधा गोल और नीचे आधा गोल बनाकर छत लगाओ", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.25f), Pair(0.45f, 0.45f), Pair(0.60f, 0.60f), Pair(0.35f, 0.68f), Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)))
            "ऊ" -> Pair("'उ' बनाकर बीच से पीछे की तरफ मुड़ी हुई पूंछ निकालो", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.25f), Pair(0.45f, 0.45f), Pair(0.60f, 0.60f), Pair(0.35f, 0.68f), Pair(0.45f, 0.45f), Pair(0.65f, 0.52f)))
            "ऋ" -> Pair("खड़ी लाइन, बाईं ओर दो शाखाएं और दाईं ओर घुमाव बनाओ", listOf(Pair(0.45f, 0.20f), Pair(0.45f, 0.70f), Pair(0.45f, 0.45f), Pair(0.28f, 0.35f), Pair(0.45f, 0.45f), Pair(0.28f, 0.60f), Pair(0.45f, 0.45f), Pair(0.65f, 0.45f), Pair(0.60f, 0.68f)))
            "ए" -> Pair("खड़ी लाइन अंदर मोड़ो और पास में तिरछी लाइन खींचो", listOf(Pair(0.40f, 0.20f), Pair(0.40f, 0.45f), Pair(0.55f, 0.50f), Pair(0.60f, 0.20f), Pair(0.40f, 0.70f)))
            "ऐ" -> Pair("'ए' बनाकर ऊपर एक तिरछी मात्रा लगाओ", listOf(Pair(0.40f, 0.20f), Pair(0.40f, 0.45f), Pair(0.55f, 0.50f), Pair(0.60f, 0.20f), Pair(0.40f, 0.70f), Pair(0.40f, 0.20f), Pair(0.30f, 0.10f)))
            "ओ" -> Pair("'आ' बनाकर ऊपर एक तिरछी मात्रा लगाओ", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.32f), Pair(0.45f, 0.45f), Pair(0.60f, 0.45f), Pair(0.60f, 0.20f), Pair(0.60f, 0.70f), Pair(0.75f, 0.20f), Pair(0.75f, 0.70f), Pair(0.75f, 0.20f), Pair(0.65f, 0.10f)))
            "औ" -> Pair("'आ' बनाकर ऊपर दो सुंदर तिरछी मात्राएं लगाओ", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.32f), Pair(0.45f, 0.45f), Pair(0.60f, 0.45f), Pair(0.60f, 0.20f), Pair(0.60f, 0.70f), Pair(0.75f, 0.20f), Pair(0.75f, 0.70f), Pair(0.75f, 0.20f), Pair(0.60f, 0.10f)))
            "अं" -> Pair("'अ' बनाकर ऊपर एक चमकती बिंदी लगाओ", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.32f), Pair(0.45f, 0.45f), Pair(0.70f, 0.45f), Pair(0.70f, 0.20f), Pair(0.70f, 0.70f), Pair(0.70f, 0.12f)))
            "अः" -> Pair("'अ' बनाकर आगे दो विसर्ग बिंदियां लगाओ", listOf(Pair(0.35f, 0.25f), Pair(0.55f, 0.32f), Pair(0.45f, 0.45f), Pair(0.70f, 0.45f), Pair(0.70f, 0.20f), Pair(0.70f, 0.70f), Pair(0.82f, 0.35f), Pair(0.82f, 0.55f)))

            // ---- व्यंजन (Consonants) ----
            "क" -> Pair("सीधी लाइन, बाईं ओर गोल पेट और दाईं ओर आधी पूंछ", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.75f), Pair(0.50f, 0.40f), Pair(0.32f, 0.40f), Pair(0.32f, 0.55f), Pair(0.50f, 0.55f), Pair(0.50f, 0.45f), Pair(0.68f, 0.45f), Pair(0.68f, 0.65f), Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)))
            "ख" -> Pair("गोल घुमाकर नीचे लाओ, फिर गोल पेट और खड़ी लाइन बनाओ", listOf(Pair(0.35f, 0.25f), Pair(0.45f, 0.25f), Pair(0.35f, 0.48f), Pair(0.45f, 0.65f), Pair(0.65f, 0.65f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.28f, 0.20f), Pair(0.75f, 0.20f)))
            "ग" -> Pair("छोटी लाइन बनाकर अंदर मोड़ो और पास में लंबी लाइन खींचो", listOf(Pair(0.40f, 0.20f), Pair(0.40f, 0.55f), Pair(0.30f, 0.55f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.28f, 0.20f), Pair(0.75f, 0.20f)))
            "घ" -> Pair("दो बार आधा गोल घुमाओ और खड़ी लाइन से जोड़ दो", listOf(Pair(0.35f, 0.25f), Pair(0.50f, 0.25f), Pair(0.35f, 0.42f), Pair(0.50f, 0.58f), Pair(0.65f, 0.58f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.28f, 0.20f), Pair(0.75f, 0.20f)))
            "ङ" -> Pair("छोटी लाइन, 'S' जैसा मोड़ो और पास में बिंदी लगाओ", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.32f), Pair(0.38f, 0.40f), Pair(0.60f, 0.52f), Pair(0.40f, 0.68f), Pair(0.68f, 0.52f)))
            "च" -> Pair("छोटी आड़ी लाइन, नीचे गोल घुमाओ और खड़ी लाइन से मिलाओ", listOf(Pair(0.32f, 0.42f), Pair(0.48f, 0.42f), Pair(0.38f, 0.55f), Pair(0.50f, 0.62f), Pair(0.65f, 0.55f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "छ" -> Pair("'घ' जैसा दो बार गोल बनाकर ऊपर छोटी गांठ बांधो", listOf(Pair(0.35f, 0.25f), Pair(0.50f, 0.25f), Pair(0.35f, 0.45f), Pair(0.55f, 0.60f), Pair(0.50f, 0.40f), Pair(0.50f, 0.20f)))
            "ज" -> Pair("खड़ी लाइन, बीच से आड़ी लाइन और नीचे गोल घुमाव", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.45f), Pair(0.50f, 0.45f), Pair(0.45f, 0.62f), Pair(0.32f, 0.55f)))
            "झ" -> Pair("'इ' बनाकर बीच से लाइन निकालकर खड़ी लाइन से जोड़ो", listOf(Pair(0.38f, 0.20f), Pair(0.38f, 0.32f), Pair(0.28f, 0.40f), Pair(0.48f, 0.50f), Pair(0.32f, 0.65f), Pair(0.38f, 0.48f), Pair(0.65f, 0.48f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "ञ" -> Pair("उल्टा 'C' घुमाओ, बीच से लाइन और खड़ी लाइन खींचो", listOf(Pair(0.45f, 0.35f), Pair(0.30f, 0.48f), Pair(0.45f, 0.60f), Pair(0.45f, 0.48f), Pair(0.65f, 0.48f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "ट" -> Pair("छोटी खड़ी लाइन और नीचे बड़ा आधा गोल कटोरा बनाओ", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.35f), Pair(0.32f, 0.50f), Pair(0.50f, 0.72f), Pair(0.68f, 0.55f)))
            "ठ" -> Pair("छोटी लाइन और नीचे पूरा गोल घेरा बनाओ", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.35f), Pair(0.32f, 0.55f), Pair(0.50f, 0.75f), Pair(0.68f, 0.55f), Pair(0.50f, 0.35f)))
            "ड" -> Pair("छोटी लाइन और सुंदर 'S' जैसा आकार मोड़ो", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.35f), Pair(0.35f, 0.45f), Pair(0.62f, 0.55f), Pair(0.40f, 0.72f)))
            "ढ" -> Pair("'ट' बनाकर अंदर छोटा गोल लूप बंद करो", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.35f), Pair(0.32f, 0.52f), Pair(0.52f, 0.72f), Pair(0.62f, 0.55f), Pair(0.52f, 0.52f)))
            "ण" -> Pair("'U' जैसा लूप बनाओ और पास में खड़ी लाइन खींचो", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.55f), Pair(0.48f, 0.60f), Pair(0.48f, 0.20f), Pair(0.68f, 0.20f), Pair(0.68f, 0.75f)))
            "त" -> Pair("खड़ी लाइन, बीच से लाइन निकालकर नीचे मोड़ो", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.42f), Pair(0.45f, 0.42f), Pair(0.38f, 0.68f)))
            "थ" -> Pair("ऊपर छोटी गांठ, नीचे गोल घुमाव और खड़ी लाइन", listOf(Pair(0.35f, 0.35f), Pair(0.42f, 0.25f), Pair(0.35f, 0.48f), Pair(0.50f, 0.65f), Pair(0.68f, 0.65f), Pair(0.68f, 0.20f), Pair(0.68f, 0.75f)))
            "द" -> Pair("'ट' बनाकर नीचे तिरछी पूंछ निकालो", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.35f), Pair(0.35f, 0.48f), Pair(0.55f, 0.65f), Pair(0.42f, 0.78f)))
            "ध" -> Pair("ऊपर छोटी गांठ, दो घुमाव और खड़ी लाइन खींचो", listOf(Pair(0.32f, 0.30f), Pair(0.45f, 0.25f), Pair(0.35f, 0.45f), Pair(0.50f, 0.60f), Pair(0.68f, 0.60f), Pair(0.68f, 0.20f), Pair(0.68f, 0.75f)))
            "न" -> Pair("खड़ी लाइन, बीच से आड़ी लाइन और छोटा लूप बनाओ", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.45f), Pair(0.40f, 0.45f), Pair(0.32f, 0.52f), Pair(0.40f, 0.52f)))
            "प" -> Pair("'U' जैसा आकार बनाकर खड़ी लाइन से जोड़ो", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.50f), Pair(0.50f, 0.58f), Pair(0.65f, 0.58f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "फ" -> Pair("'प' बनाकर दाईं तरफ नीचे पूंछ लटकाओ", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.50f), Pair(0.50f, 0.58f), Pair(0.50f, 0.20f), Pair(0.50f, 0.75f), Pair(0.50f, 0.45f), Pair(0.68f, 0.52f), Pair(0.68f, 0.70f)))
            "ब" -> Pair("खड़ी लाइन, बीच में गोल पेट और अंदर तिरछी लाइन खींचो", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.38f), Pair(0.40f, 0.38f), Pair(0.40f, 0.60f), Pair(0.65f, 0.60f), Pair(0.42f, 0.40f), Pair(0.62f, 0.58f)))
            "भ" -> Pair("ऊपर गांठ, खड़ी लाइन, अंदर मोड़कर बड़ी खड़ी लाइन से जोड़ो", listOf(Pair(0.35f, 0.30f), Pair(0.35f, 0.55f), Pair(0.45f, 0.55f), Pair(0.65f, 0.55f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "म" -> Pair("खड़ी लाइन, अंदर मोड़कर आड़ी लाइन और पास में खड़ी लाइन", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.55f), Pair(0.45f, 0.55f), Pair(0.65f, 0.55f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "य" -> Pair("आधा घुमाव, नीचे मोड़ और खड़ी लाइन से मिलाओ", listOf(Pair(0.35f, 0.28f), Pair(0.45f, 0.38f), Pair(0.38f, 0.55f), Pair(0.55f, 0.62f), Pair(0.65f, 0.62f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "र" -> Pair("आधा गोल घुमाकर नीचे तिरछी पूंछ निकालो", listOf(Pair(0.40f, 0.25f), Pair(0.55f, 0.25f), Pair(0.58f, 0.42f), Pair(0.42f, 0.48f), Pair(0.60f, 0.75f)))
            "ल" -> Pair("खड़ी लाइन, बीच से दो बार उल्टा घुमाव बनाओ", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.50f), Pair(0.50f, 0.42f), Pair(0.35f, 0.55f), Pair(0.32f, 0.70f)))
            "व" -> Pair("खड़ी लाइन और बीच में गोल पेट बनाओ", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.38f), Pair(0.40f, 0.38f), Pair(0.40f, 0.58f), Pair(0.65f, 0.58f)))
            "श" -> Pair("ऊपर गोल गांठ, '2' जैसा मोड़, और पास में खड़ी लाइन", listOf(Pair(0.35f, 0.25f), Pair(0.45f, 0.38f), Pair(0.32f, 0.55f), Pair(0.45f, 0.70f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "ष" -> Pair("'प' बनाकर अंदर से तिरछी लाइन खींचो", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.50f), Pair(0.50f, 0.58f), Pair(0.65f, 0.58f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.38f, 0.35f), Pair(0.62f, 0.55f)))
            "स" -> Pair("'र' बनाकर बीच से आड़ी लाइन और खड़ी लाइन से मिलाओ", listOf(Pair(0.35f, 0.25f), Pair(0.48f, 0.38f), Pair(0.35f, 0.70f), Pair(0.45f, 0.48f), Pair(0.65f, 0.48f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f)))
            "ह" -> Pair("छोटी लाइन, 'S' जैसा मोड़ो और नीचे आधा चाँद जोड़ो", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.32f), Pair(0.38f, 0.42f), Pair(0.55f, 0.50f), Pair(0.38f, 0.60f), Pair(0.58f, 0.72f)))
            "क्ष" -> Pair("खड़ी लाइन, बीच से ऊपर गांठ, 'S' मोड़ और नीचे पूंछ", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.45f), Pair(0.40f, 0.30f), Pair(0.35f, 0.50f), Pair(0.50f, 0.70f)))
            "त्र" -> Pair("खड़ी लाइन और बाईं ओर तीर के समान दो तिरछी लाइनें", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.45f), Pair(0.35f, 0.32f), Pair(0.65f, 0.45f), Pair(0.35f, 0.65f)))
            "ज्ञ" -> Pair("खड़ी लाइन, बीच से लाइन, 'ज' घुमाकर नीचे पूंछ निकालो", listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.45f), Pair(0.45f, 0.45f), Pair(0.38f, 0.60f), Pair(0.50f, 0.75f)))

            // ---- ENGLISH ALPHABETS (A to Z) ----
            "A" -> Pair("बाईं तिरछी लाइन, दाईं तिरछी लाइन और बीच में आड़ी लाइन", listOf(Pair(0.50f, 0.20f), Pair(0.32f, 0.75f), Pair(0.50f, 0.20f), Pair(0.68f, 0.75f), Pair(0.38f, 0.55f), Pair(0.62f, 0.55f)))
            "B" -> Pair("सीधी खड़ी लाइन और दाईं ओर दो छोटे गोल पेट बनाओ", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.62f, 0.32f), Pair(0.35f, 0.48f), Pair(0.65f, 0.62f), Pair(0.35f, 0.75f)))
            "C" -> Pair("दाईं तरफ से आधा गोल घुमाकर बाईं ओर लाओ", listOf(Pair(0.65f, 0.25f), Pair(0.45f, 0.22f), Pair(0.32f, 0.48f), Pair(0.45f, 0.75f), Pair(0.68f, 0.70f)))
            "D" -> Pair("सीधी खड़ी लाइन और ऊपर से नीचे तक बड़ा गोल पेट", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.68f, 0.48f), Pair(0.35f, 0.75f)))
            "E" -> Pair("खड़ी लाइन, फिर ऊपर, बीच और नीचे तीन आड़ी लाइनें", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.68f, 0.20f), Pair(0.35f, 0.48f), Pair(0.60f, 0.48f), Pair(0.35f, 0.75f), Pair(0.68f, 0.75f)))
            "F" -> Pair("खड़ी लाइन और ऊपर तथा बीच में दो आड़ी लाइनें खींचो", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.68f, 0.20f), Pair(0.35f, 0.48f), Pair(0.60f, 0.48f)))
            "G" -> Pair("'C' बनाकर अंदर मोड़ो और छोटी खड़ी लाइन खींचो", listOf(Pair(0.65f, 0.25f), Pair(0.35f, 0.48f), Pair(0.55f, 0.75f), Pair(0.68f, 0.60f), Pair(0.52f, 0.60f)))
            "H" -> Pair("दो सीधी खड़ी लाइनें और बीच में एक जोड़ने वाली लाइन", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.65f, 0.20f), Pair(0.65f, 0.75f), Pair(0.35f, 0.48f), Pair(0.65f, 0.48f)))
            "I" -> Pair("सीधी खड़ी लाइन, ऊपर और नीचे छोटी आड़ी लाइन", listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.75f), Pair(0.38f, 0.20f), Pair(0.62f, 0.20f), Pair(0.38f, 0.75f), Pair(0.62f, 0.75f)))
            "J" -> Pair("खड़ी लाइन खींचकर नीचे से बाईं ओर गोल मोड़ो", listOf(Pair(0.58f, 0.20f), Pair(0.58f, 0.65f), Pair(0.48f, 0.75f), Pair(0.35f, 0.65f)))
            "K" -> Pair("खड़ी लाइन और बीच से दो तिरछी लाइनें खोलो", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.65f, 0.22f), Pair(0.35f, 0.48f), Pair(0.65f, 0.75f)))
            "L" -> Pair("सीधी खड़ी लाइन और नीचे दाईं ओर आड़ी लाइन खींचो", listOf(Pair(0.38f, 0.20f), Pair(0.38f, 0.75f), Pair(0.38f, 0.75f), Pair(0.68f, 0.75f)))
            "M" -> Pair("दो खड़ी लाइनें और बीच में दो तिरछी लाइनें मिलाओ", listOf(Pair(0.30f, 0.75f), Pair(0.30f, 0.20f), Pair(0.50f, 0.55f), Pair(0.70f, 0.20f), Pair(0.70f, 0.75f)))
            "N" -> Pair("दो खड़ी लाइनें और ऊपर से नीचे तिरछी लाइन जोड़ो", listOf(Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.20f)))
            "O" -> Pair("पूरा गोल घेरा अंडाकार घुमाकर बंद करो", listOf(Pair(0.50f, 0.20f), Pair(0.32f, 0.48f), Pair(0.50f, 0.75f), Pair(0.68f, 0.48f), Pair(0.50f, 0.20f)))
            "P" -> Pair("खड़ी लाइन और ऊपर एक छोटा गोल पेट बनाओ", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.65f, 0.35f), Pair(0.35f, 0.50f)))
            "Q" -> Pair("'O' बनाकर नीचे दाईं ओर छोटी तिरछी पूंछ निकालो", listOf(Pair(0.50f, 0.20f), Pair(0.32f, 0.48f), Pair(0.50f, 0.75f), Pair(0.68f, 0.48f), Pair(0.50f, 0.20f), Pair(0.55f, 0.65f), Pair(0.72f, 0.80f)))
            "R" -> Pair("'P' बनाकर नीचे दाईं ओर तिरछी टांग खींचो", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.75f), Pair(0.35f, 0.20f), Pair(0.65f, 0.35f), Pair(0.35f, 0.50f), Pair(0.65f, 0.75f)))
            "S" -> Pair("ऊपर से आधा गोल बाईं ओर, फिर नीचे दाईं ओर मोड़ो", listOf(Pair(0.65f, 0.28f), Pair(0.48f, 0.20f), Pair(0.35f, 0.35f), Pair(0.62f, 0.58f), Pair(0.50f, 0.75f), Pair(0.35f, 0.68f)))
            "T" -> Pair("ऊपर बड़ी आड़ी छत और बीच में सीधी खड़ी लाइन खींचो", listOf(Pair(0.30f, 0.20f), Pair(0.70f, 0.20f), Pair(0.50f, 0.20f), Pair(0.50f, 0.75f)))
            "U" -> Pair("ऊपर से नीचे कटोरे जैसा गोल घुमाकर ऊपर ले जाओ", listOf(Pair(0.35f, 0.20f), Pair(0.35f, 0.60f), Pair(0.50f, 0.75f), Pair(0.65f, 0.60f), Pair(0.65f, 0.20f)))
            "V" -> Pair("बाईं तिरछी लाइन और दाईं तिरछी लाइन नीचे मिलाओ", listOf(Pair(0.32f, 0.20f), Pair(0.50f, 0.75f), Pair(0.68f, 0.20f)))
            "W" -> Pair("दो 'V' को मिलाकर एक साथ सुंदर आकृति बनाओ", listOf(Pair(0.28f, 0.20f), Pair(0.38f, 0.75f), Pair(0.50f, 0.40f), Pair(0.62f, 0.75f), Pair(0.72f, 0.20f)))
            "X" -> Pair("दो तिरछी लाइनें एक-दूसरे को काटती हुई खींचो", listOf(Pair(0.35f, 0.20f), Pair(0.65f, 0.75f), Pair(0.65f, 0.20f), Pair(0.35f, 0.75f)))
            "Y" -> Pair("ऊपर छोटा 'V' और नीचे सीधी खड़ी लाइन जोड़ो", listOf(Pair(0.35f, 0.20f), Pair(0.50f, 0.48f), Pair(0.65f, 0.20f), Pair(0.50f, 0.48f), Pair(0.50f, 0.75f)))
            "Z" -> Pair("ऊपर आड़ी लाइन, तिरछी लाइन और नीचे आड़ी लाइन खींचो", listOf(Pair(0.35f, 0.20f), Pair(0.65f, 0.20f), Pair(0.35f, 0.75f), Pair(0.65f, 0.75f)))

            // ---- MATH NUMBERS (1 to 10) ----
            "1" -> Pair("ऊपर से नीचे तक एक सीधी खड़ी लाइन खींचो", listOf(Pair(0.42f, 0.28f), Pair(0.50f, 0.20f), Pair(0.50f, 0.75f), Pair(0.38f, 0.75f), Pair(0.62f, 0.75f)))
            "2" -> Pair("ऊपर गोल घुमाओ, तिरछा नीचे लाओ और नीचे सीधी लाइन खींचो", listOf(Pair(0.38f, 0.28f), Pair(0.52f, 0.20f), Pair(0.65f, 0.30f), Pair(0.40f, 0.70f), Pair(0.68f, 0.70f)))
            "3" -> Pair("पहला आधा घेरा, फिर दूसरा आधा घेरा बनाओ", listOf(Pair(0.38f, 0.25f), Pair(0.58f, 0.22f), Pair(0.48f, 0.45f), Pair(0.62f, 0.55f), Pair(0.40f, 0.75f)))
            "4" -> Pair("तिरछी लाइन, आड़ी लाइन और फिर सीधी खड़ी लाइन", listOf(Pair(0.58f, 0.20f), Pair(0.35f, 0.55f), Pair(0.70f, 0.55f), Pair(0.58f, 0.20f), Pair(0.58f, 0.75f)))
            "5" -> Pair("ऊपर आड़ी लाइन, छोटी खड़ी लाइन और नीचे गोल पेट", listOf(Pair(0.65f, 0.22f), Pair(0.40f, 0.22f), Pair(0.40f, 0.45f), Pair(0.65f, 0.55f), Pair(0.40f, 0.75f)))
            "6" -> Pair("ऊपर से घुमावदार नीचे लाओ और नीचे पूरा गोल घेरा बंद करो", listOf(Pair(0.62f, 0.25f), Pair(0.38f, 0.48f), Pair(0.50f, 0.75f), Pair(0.65f, 0.60f), Pair(0.38f, 0.60f)))
            "7" -> Pair("ऊपर आड़ी लाइन और नीचे दाईं ओर तिरछी लाइन खींचो", listOf(Pair(0.35f, 0.22f), Pair(0.68f, 0.22f), Pair(0.45f, 0.75f)))
            "8" -> Pair("ऊपर छोटा गोल घेरा और नीचे दूसरा गोल घेरा बनाओ", listOf(Pair(0.50f, 0.20f), Pair(0.38f, 0.32f), Pair(0.62f, 0.60f), Pair(0.50f, 0.75f), Pair(0.38f, 0.60f), Pair(0.62f, 0.32f), Pair(0.50f, 0.20f)))
            "9" -> Pair("ऊपर पूरा गोल घेरा बनाओ और नीचे सीधी लाइन खींचो", listOf(Pair(0.50f, 0.48f), Pair(0.35f, 0.35f), Pair(0.50f, 0.20f), Pair(0.65f, 0.35f), Pair(0.65f, 0.75f)))
            "10" -> Pair("पहले संख्या '1' बनाओ और पास में पूरा गोल '0' बनाओ", listOf(Pair(0.40f, 0.20f), Pair(0.40f, 0.75f), Pair(0.65f, 0.20f), Pair(0.55f, 0.48f), Pair(0.65f, 0.75f), Pair(0.75f, 0.48f), Pair(0.65f, 0.20f)))

            else -> {
                val hint = "पूरे अक्षर की आउटलाइन पर अपनी उंगली से सुंदर आकृति बनाओ"
                val dots = listOf(
                    Pair(0.50f, 0.20f),
                    Pair(0.30f, 0.50f),
                    Pair(0.50f, 0.75f),
                    Pair(0.70f, 0.50f),
                    Pair(0.50f, 0.20f)
                )
                Pair(hint, dots)
            }
        }
    }


    // 4. Word & Blend Combination (Level 3)
    fun getWordBlends(subjectId: String): List<WordBlend> {
        val dynamicBlends = ContentRepository.getDynamicWordBlends(subjectId)
        if (dynamicBlends.isNotEmpty()) {
            return dynamicBlends
        }
        return when (subjectId.lowercase()) {
            "hindi" -> listOf(
                WordBlend(
                    id = "hi_blend_1",
                    subjectId = "hindi",
                    char1 = "क",
                    char2 = "ल",
                    resultWord = "कल",
                    meaningHindi = "कल (Kal) - आने वाला या बीता हुआ दिन",
                    emoji = "📅",
                    audioExplanationHindi = "क... और ल... मिलकर बना... कल! कल हम सब पार्क घूमने जाएंगे!"
                ),
                WordBlend(
                    id = "hi_blend_2",
                    subjectId = "hindi",
                    char1 = "ज",
                    char2 = "ल",
                    resultWord = "जल",
                    meaningHindi = "जल (Jal) - पीने का शुद्ध पानी",
                    emoji = "💧",
                    audioExplanationHindi = "ज... और ल... मिलकर बना... जल! जल ही जीवन है!"
                ),
                WordBlend(
                    id = "hi_blend_3",
                    subjectId = "hindi",
                    char1 = "घ",
                    char2 = "र",
                    resultWord = "घर",
                    meaningHindi = "घर (Ghar) - जहाँ हम सब रहते हैं",
                    emoji = "🏠",
                    audioExplanationHindi = "घ... और र... मिलकर बना... घर! हमारा प्यारा घर!"
                ),
                WordBlend(
                    id = "hi_blend_4",
                    subjectId = "hindi",
                    char1 = "ब",
                    char2 = "स",
                    resultWord = "बस",
                    meaningHindi = "बस (Bus) - स्कूल जाने वाली बस",
                    emoji = "🚌",
                    audioExplanationHindi = "ब... और स... मिलकर बना... बस! पीली स्कूल बस!"
                ),
                WordBlend(
                    id = "hi_blend_5",
                    subjectId = "hindi",
                    char1 = "फ",
                    char2 = "ल",
                    resultWord = "फल",
                    meaningHindi = "फल (Fal) - मीठे और ताजे फल",
                    emoji = "🍎",
                    audioExplanationHindi = "फ... और ल... मिलकर बना... फल! ताजे फल खाओ!"
                ),
                WordBlend(
                    id = "hi_blend_6",
                    subjectId = "hindi",
                    char1 = "न",
                    char2 = "ल",
                    resultWord = "नल",
                    meaningHindi = "नल (Nal) - पानी देने वाला नल",
                    emoji = "🚰",
                    audioExplanationHindi = "न... और ल... मिलकर बना... नल! नल से ताजा पानी लो!"
                ),
                WordBlend(
                    id = "hi_blend_7",
                    subjectId = "hindi",
                    char1 = "र",
                    char2 = "थ",
                    resultWord = "रथ",
                    meaningHindi = "रथ (Rath) - राजा का सुंदर रथ",
                    emoji = "🎠",
                    audioExplanationHindi = "र... और थ... मिलकर बना... रथ! रथ पर सवार राजा!"
                ),
                WordBlend(
                    id = "hi_blend_8",
                    subjectId = "hindi",
                    char1 = "द",
                    char2 = "स",
                    resultWord = "दस",
                    meaningHindi = "दस (Das) - संख्या 10",
                    emoji = "🔟",
                    audioExplanationHindi = "द... और स... मिलकर बना... दस! 10 उंगलियाँ!"
                ),
                WordBlend(
                    id = "hi_blend_9",
                    subjectId = "hindi",
                    char1 = "ह",
                    char2 = "ल",
                    resultWord = "हल",
                    meaningHindi = "हल (Hal) - खेत जोतने वाला हल या समस्या का समाधान",
                    emoji = "🚜",
                    audioExplanationHindi = "ह... और ल... मिलकर बना... हल! किसान का हल!"
                ),
                WordBlend(
                    id = "hi_blend_10",
                    subjectId = "hindi",
                    char1 = "ध",
                    char2 = "न",
                    resultWord = "धन",
                    meaningHindi = "धन (Dhan) - संपत्ति और विद्या धन",
                    emoji = "💰",
                    audioExplanationHindi = "ध... और न... मिलकर बना... धन! विद्या सबसे बड़ा धन है!"
                ),
                WordBlend(
                    id = "hi_blend_11",
                    subjectId = "hindi",
                    char1 = "ज",
                    char2 = "ग",
                    resultWord = "जग",
                    meaningHindi = "जग (Jag) - पानी का जग या पूरी दुनिया",
                    emoji = "🥛",
                    audioExplanationHindi = "ज... और ग... मिलकर बना... जग! जग में पानी भरो!"
                ),
                WordBlend(
                    id = "hi_blend_12",
                    subjectId = "hindi",
                    char1 = "स",
                    char2 = "च",
                    resultWord = "सच",
                    meaningHindi = "सच (Sach) - हमेशा सच बोलो",
                    emoji = "✨",
                    audioExplanationHindi = "स... और च... मिलकर बना... सच! सदा सत्य बोलो!"
                )
            )

            "english" -> listOf(
                WordBlend(
                    id = "en_blend_1",
                    subjectId = "english",
                    char1 = "A",
                    char2 = "T",
                    resultWord = "AT",
                    meaningHindi = "AT - किसी जगह पर होना (Look AT the toy)",
                    emoji = "📍",
                    audioExplanationHindi = "A plus T makes... AT! Look AT Priya Didi!"
                ),
                WordBlend(
                    id = "en_blend_2",
                    subjectId = "english",
                    char1 = "I",
                    char2 = "N",
                    resultWord = "IN",
                    meaningHindi = "IN - अंदर होना (Ball is IN the box)",
                    emoji = "📥",
                    audioExplanationHindi = "I plus N makes... IN! The fish is IN water!"
                ),
                WordBlend(
                    id = "en_blend_3",
                    subjectId = "english",
                    char1 = "O",
                    char2 = "N",
                    resultWord = "ON",
                    meaningHindi = "ON - ऊपर होना (Book is ON table)",
                    emoji = "🔛",
                    audioExplanationHindi = "O plus N makes... ON! Cap is ON my head!"
                ),
                WordBlend(
                    id = "en_blend_4",
                    subjectId = "english",
                    char1 = "G",
                    char2 = "O",
                    resultWord = "GO",
                    meaningHindi = "GO - जाना या चलना (Ready, Steady, GO!)",
                    emoji = "🏃",
                    audioExplanationHindi = "G plus O makes... GO! Let's GO to school!"
                ),
                WordBlend(
                    id = "en_blend_5",
                    subjectId = "english",
                    char1 = "N",
                    char2 = "O",
                    resultWord = "NO",
                    meaningHindi = "NO - नहीं कहना (NO means nahii)",
                    emoji = "🙅",
                    audioExplanationHindi = "N plus O makes... NO! Say NO to junk food!"
                ),
                WordBlend(
                    id = "en_blend_6",
                    subjectId = "english",
                    char1 = "U",
                    char2 = "P",
                    resultWord = "UP",
                    meaningHindi = "UP - ऊपर देखना या उठना (Look UP in sky)",
                    emoji = "⬆️",
                    audioExplanationHindi = "U plus P makes... UP! Sun is UP in the sky!"
                ),
                WordBlend(
                    id = "en_blend_7",
                    subjectId = "english",
                    char1 = "M",
                    char2 = "E",
                    resultWord = "ME",
                    meaningHindi = "ME - मुझे / मैं (Listen to ME)",
                    emoji = "🙋",
                    audioExplanationHindi = "M plus E makes... ME! Give the toy to ME!"
                ),
                WordBlend(
                    id = "en_blend_8",
                    subjectId = "english",
                    char1 = "H",
                    char2 = "E",
                    resultWord = "HE",
                    meaningHindi = "HE - वह लड़का (HE is my friend)",
                    emoji = "👦",
                    audioExplanationHindi = "H plus E makes... HE! HE is studying!"
                )
            )

            else -> listOf( // Math
                WordBlend(
                    id = "ma_blend_1",
                    subjectId = "math",
                    char1 = "1",
                    char2 = "1",
                    operatorSymbol = "+",
                    resultWord = "2",
                    meaningHindi = "1 + 1 = 2 (एक और एक मिलकर बने दो)",
                    emoji = "✌️",
                    audioExplanationHindi = "एक सेब... और एक और सेब... मिलकर बने दो सेब! 1 + 1 = 2!"
                ),
                WordBlend(
                    id = "ma_blend_2",
                    subjectId = "math",
                    char1 = "2",
                    char2 = "1",
                    operatorSymbol = "+",
                    resultWord = "3",
                    meaningHindi = "2 + 1 = 3 (दो और एक मिलकर बने तीन)",
                    emoji = "🎈",
                    audioExplanationHindi = "दो गुब्बारे... और एक गुब्बारा... कुल हुए तीन गुब्बारे! 2 + 1 = 3!"
                ),
                WordBlend(
                    id = "ma_blend_3",
                    subjectId = "math",
                    char1 = "2",
                    char2 = "2",
                    operatorSymbol = "+",
                    resultWord = "4",
                    meaningHindi = "2 + 2 = 4 (दो और दो मिलकर बने चार)",
                    emoji = "🍀",
                    audioExplanationHindi = "दो और दो जोड़ने पर बनते हैं चार! 2 + 2 = 4!"
                ),
                WordBlend(
                    id = "ma_blend_4",
                    subjectId = "math",
                    char1 = "3",
                    char2 = "2",
                    operatorSymbol = "+",
                    resultWord = "5",
                    meaningHindi = "3 + 2 = 5 (तीन और दो मिलकर बने पाँच)",
                    emoji = "⭐",
                    audioExplanationHindi = "तीन सितारे और दो सितारे... कुल पाँच चमकते सितारे! 3 + 2 = 5!"
                ),
                WordBlend(
                    id = "ma_blend_5",
                    subjectId = "math",
                    char1 = "4",
                    char2 = "1",
                    operatorSymbol = "+",
                    resultWord = "5",
                    meaningHindi = "4 + 1 = 5 (चार और एक मिलकर बने पाँच)",
                    emoji = "🖐️",
                    audioExplanationHindi = "चार गेंद और एक गेंद... कुल हुईं पाँच गेंदें! 4 + 1 = 5!"
                ),
                WordBlend(
                    id = "ma_blend_6",
                    subjectId = "math",
                    char1 = "5",
                    char2 = "5",
                    operatorSymbol = "+",
                    resultWord = "10",
                    meaningHindi = "5 + 5 = 10 (पाँच और पाँच मिलकर बने दस)",
                    emoji = "🙌",
                    audioExplanationHindi = "पाँच उंगलियाँ और पाँच उंगलियाँ... मिलकर बनीं दस उंगलियाँ! 5 + 5 = 10!"
                )
            )
        }
    }

    // 5. Interactive Quizzes (Level 4)
    fun getQuizzes(subjectId: String): List<QuizQuestion> {
        val dynamicQuizzes = ContentRepository.getDynamicQuizzes(subjectId)
        if (dynamicQuizzes.isNotEmpty()) {
            return dynamicQuizzes
        }
        return when (subjectId.lowercase()) {
            "hindi" -> listOf(
                QuizQuestion(
                    id = 101,
                    subjectId = "hindi",
                    questionHindi = "इनमें से 'क' से कबूतर कौन सा है?",
                    promptEmoji = "🕊️",
                    options = listOf("ग", "क", "म", "न"),
                    correctIndex = 1,
                    praiseHindi = "शाबाश! बिल्कुल सही! 'क' से कबूतर!",
                    explanationHindi = "क अक्षर से कबूतर बनता है।"
                ),
                QuizQuestion(
                    id = 102,
                    subjectId = "hindi",
                    questionHindi = "'आम' का पहला अक्षर क्या है?",
                    promptEmoji = "🥭",
                    options = listOf("अ", "ई", "आ", "उ"),
                    correctIndex = 2,
                    praiseHindi = "अरे वाह! बहुत खूब! 'आ' से आम!",
                    explanationHindi = "आम शब्द 'आ' से शुरू होता है।"
                ),
                QuizQuestion(
                    id = 103,
                    subjectId = "hindi",
                    questionHindi = "क + ल मिलकर कौन सा शब्द बनता है?",
                    promptEmoji = "🧩",
                    options = listOf("कम", "कल", "कट", "कस"),
                    correctIndex = 1,
                    praiseHindi = "शानदार! क + ल = कल!",
                    explanationHindi = "क और ल जोड़ने पर 'कल' बनता है।"
                ),
                QuizQuestion(
                    id = 104,
                    subjectId = "hindi",
                    questionHindi = "'मछली' का पहला अक्षर कौन सा है?",
                    promptEmoji = "🐟",
                    options = listOf("प", "फ", "ब", "म"),
                    correctIndex = 3,
                    praiseHindi = "अरे वाह! म से मछली जल की रानी!",
                    explanationHindi = "मछली शब्द 'म' अक्षर से शुरू होता है।"
                ),
                QuizQuestion(
                    id = 105,
                    subjectId = "hindi",
                    questionHindi = "ज + ल मिलकर क्या बनता है?",
                    promptEmoji = "💧",
                    options = listOf("जल", "जग", "जन", "जब"),
                    correctIndex = 0,
                    praiseHindi = "बहुत खूब! ज + ल = जल (पानी)!",
                    explanationHindi = "ज और ल मिलकर जल बनता है।"
                ),
                QuizQuestion(
                    id = 106,
                    subjectId = "hindi",
                    questionHindi = "इनमें से 'त' से तरबूज कौन सा है?",
                    promptEmoji = "🍉",
                    options = listOf("द", "थ", "त", "ध"),
                    correctIndex = 2,
                    praiseHindi = "शाबाश! 'त' से मीठा तरबूज!",
                    explanationHindi = "त वर्ण से तरबूज बनता है।"
                ),
                QuizQuestion(
                    id = 107,
                    subjectId = "hindi",
                    questionHindi = "ब + स मिलकर कौन सा वाहन बनता है?",
                    promptEmoji = "🚌",
                    options = listOf("बल", "बस", "बक", "बम"),
                    correctIndex = 1,
                    praiseHindi = "कमाल कर दिया! ब + स = बस!",
                    explanationHindi = "ब और स मिलकर 'बस' बनता है।"
                ),
                QuizQuestion(
                    id = 108,
                    subjectId = "hindi",
                    questionHindi = "वर्णमाला का अंतिम संयुक्त व्यंजन कौन सा है?",
                    promptEmoji = "📚",
                    options = listOf("क्ष", "त्र", "ज्ञ", "ह"),
                    correctIndex = 2,
                    praiseHindi = "अद्भुत! 'ज्ञ' से ज्ञानी!",
                    explanationHindi = "ज्ञानी का 'ज्ञ' संयुक्त व्यंजन है।"
                )
            )

            "english" -> listOf(
                QuizQuestion(
                    id = 201,
                    subjectId = "english",
                    questionHindi = "Which letter is for 'Apple' 🍎?",
                    promptEmoji = "🍎",
                    options = listOf("B", "A", "C", "D"),
                    correctIndex = 1,
                    praiseHindi = "Superb! A is for Apple!",
                    explanationHindi = "Apple starts with letter A."
                ),
                QuizQuestion(
                    id = 202,
                    subjectId = "english",
                    questionHindi = "What does 'G + O' make?",
                    promptEmoji = "🏃",
                    options = listOf("GO", "NO", "TO", "SO"),
                    correctIndex = 0,
                    praiseHindi = "Awesome! G + O makes GO!",
                    explanationHindi = "G and O blend together as GO."
                ),
                QuizQuestion(
                    id = 203,
                    subjectId = "english",
                    questionHindi = "Which letter makes the sound of 'Cat' 🐱?",
                    promptEmoji = "🐱",
                    options = listOf("D", "F", "C", "P"),
                    correctIndex = 2,
                    praiseHindi = "Great Job! C for Cat!",
                    explanationHindi = "C sounds like /k/ in Cat."
                ),
                QuizQuestion(
                    id = 204,
                    subjectId = "english",
                    questionHindi = "Which letter is for 'Lion' 🦁?",
                    promptEmoji = "🦁",
                    options = listOf("L", "M", "N", "O"),
                    correctIndex = 0,
                    praiseHindi = "Brilliant! L is for Lion!",
                    explanationHindi = "Lion starts with letter L."
                )
            )

            else -> listOf( // Math
                QuizQuestion(
                    id = 301,
                    subjectId = "math",
                    questionHindi = "1 + 1 कितना होता है?",
                    promptEmoji = "🍎 + 🍎",
                    options = listOf("1", "3", "2", "4"),
                    correctIndex = 2,
                    praiseHindi = "बहुत बढ़िया! 1 + 1 = 2!",
                    explanationHindi = "एक और एक जोड़ने पर दो बनता है।"
                ),
                QuizQuestion(
                    id = 302,
                    subjectId = "math",
                    questionHindi = "इनमें से कौन सा अंक '3' है?",
                    promptEmoji = "🛺 (तीन पहिए)",
                    options = listOf("5", "3", "2", "1"),
                    correctIndex = 1,
                    praiseHindi = "शाबाश! यह अंक ३ (Three) है!",
                    explanationHindi = "रिक्शा के ३ पहिए होते हैं।"
                ),
                QuizQuestion(
                    id = 303,
                    subjectId = "math",
                    questionHindi = "2 + 2 कितना होता है?",
                    promptEmoji = "🎈🎈 + 🎈🎈",
                    options = listOf("4", "5", "3", "6"),
                    correctIndex = 0,
                    praiseHindi = "कमाल कर दिया! 2 + 2 = 4!",
                    explanationHindi = "दो और दो जोड़ने पर चार बनता है।"
                ),
                QuizQuestion(
                    id = 304,
                    subjectId = "math",
                    questionHindi = "5 + 5 कितना होता है?",
                    promptEmoji = "🖐️ + 🖐️",
                    options = listOf("8", "9", "10", "12"),
                    correctIndex = 2,
                    praiseHindi = "शाबाश! 5 + 5 = 10 (दस)!",
                    explanationHindi = "दोनों हाथों की दस उंगलियाँ होती हैं।"
                )
            )
        }
    }

    // 6. Badges Showcase (Dynamically unlocked based on actual student progress)
    fun getBadges(userProfile: UserProfile? = null): List<Badge> {
        val xp = userProfile?.totalXp ?: 0
        val stars = userProfile?.totalStars ?: 0
        val streak = userProfile?.currentStreakDays ?: 1
        val rank = userProfile?.currentRank ?: 8
        val cards = userProfile?.cardsLearnedCount ?: 0
        val tracing = userProfile?.lettersTracedCount ?: 0
        val words = userProfile?.wordsBuiltCount ?: 0
        val quiz = userProfile?.quizzesSolvedCount ?: 0
        val mathPct = userProfile?.mathProgressPercent ?: 0
        val hindiPct = userProfile?.hindiProgressPercent ?: 0

        return listOf(
            Badge("b_first_step", "पहला कदम", "पहला पाठ पूरा करने पर", "🌟", xp > 0 || cards > 0, if (xp > 0 || cards > 0) "सक्रिय" else ""),
            Badge("b_tracing_pro", "लेखन जादूगर", "उंगली से 3 अक्षर सही लिखने पर", "✍️", tracing >= 3 || stars >= 6, if (tracing >= 3 || stars >= 6) "सक्रिय" else ""),
            Badge("b_streak_master", "नियमित विद्यार्थी", "लगातार 3 दिन पढ़ाई करने पर", "🔥", streak >= 3, if (streak >= 3) "सक्रिय" else ""),
            Badge("b_word_builder", "शब्द शिल्पी", "लेवल 3 में 2 शब्द जोड़ने पर", "🧩", words >= 2 || xp >= 200, if (words >= 2 || xp >= 200) "सक्रिय" else ""),
            Badge("b_quiz_champ", "क्विज चैम्पियन", "सवालों के सही जवाब देने पर", "🧠", quiz >= 3 || xp >= 350, if (quiz >= 3 || xp >= 350) "सक्रिय" else ""),
            Badge("b_math_star", "गणित का सितारा", "संख्या जोड़ना सीखने पर", "🔢", mathPct > 0 || xp >= 450, if (mathPct > 0 || xp >= 450) "सक्रिय" else ""),
            Badge("b_varnamala_master", "वर्णमाला सम्राट", "पूरी हिंदी वर्णमाला सीखने पर", "👑", hindiPct >= 20 || xp >= 600, if (hindiPct >= 20 || xp >= 600) "सक्रिय" else ""),
            Badge("b_super_kid", "सुपर किड ऑलराउंडर", "लीडरबोर्ड टॉप 3 और 700+ XP पाने पर", "🏆", rank <= 3 && xp >= 700, if (rank <= 3 && xp >= 700) "सक्रिय" else "")
        )
    }

    // 7. Community Leaderboard (Real-time dynamic ranking with user's actual XP, avatar and name)
    fun getLeaderboard(currentUser: UserProfile? = null): List<LeaderboardUser> {
        val peers = listOf(
            LeaderboardUser(rank = 0, name = "अन्वी शर्मा", avatarEmoji = "👧", xp = 620, streakDays = 14),
            LeaderboardUser(rank = 0, name = "विहान पटेल", avatarEmoji = "🚀", xp = 520, streakDays = 9),
            LeaderboardUser(rank = 0, name = "सान्वी गुप्ता", avatarEmoji = "🐱", xp = 420, streakDays = 8),
            LeaderboardUser(rank = 0, name = "कबीर सिंह", avatarEmoji = "👦", xp = 310, streakDays = 4),
            LeaderboardUser(rank = 0, name = "रिया वर्मा", avatarEmoji = "🦄", xp = 220, streakDays = 3),
            LeaderboardUser(rank = 0, name = "अद्वैत जोशी", avatarEmoji = "🤖", xp = 120, streakDays = 2),
            LeaderboardUser(rank = 0, name = "परी मेहता", avatarEmoji = "🌟", xp = 50, streakDays = 1)
        )

        val kid = LeaderboardUser(
            rank = 0,
            name = "${currentUser?.name ?: "आरव"} (आप)",
            avatarEmoji = currentUser?.avatarEmoji ?: "🦁",
            xp = currentUser?.totalXp ?: 0,
            streakDays = currentUser?.currentStreakDays ?: 1,
            isCurrentUser = true
        )

        val all = (peers + kid).sortedWith(
            compareByDescending<LeaderboardUser> { it.xp }
                .thenByDescending { it.streakDays }
        )

        return all.mapIndexed { index, user ->
            user.copy(rank = index + 1)
        }
    }
}
