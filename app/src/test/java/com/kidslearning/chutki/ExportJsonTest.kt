package com.kidslearning.chutki

import com.google.gson.GsonBuilder
import com.kidslearning.chutki.data.ContentRepository
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.data.MasterAppContent
import com.kidslearning.chutki.data.WordBlendsDataProvider
import org.junit.Test
import java.io.File

class ExportJsonTest {

    @Test
    fun exportCompleteGameContentJson() {
        // 1. Read existing 20 game items
        val assetsFile = File("src/main/assets/game_content.json")
        val existingItems = if (assetsFile.exists()) {
            ContentRepository.parseJson(assetsFile.readText())
        } else emptyList()

        // 2. Collect all alphabet cards (Hindi, English, Math 1-100)
        val alphabetCardsMap = mapOf(
            "hindi" to LearningContentRepository.getAlphabetCards("hindi"),
            "english" to LearningContentRepository.getAlphabetCards("english"),
            "math" to LearningContentRepository.getMathCards1To100()
        )

        // 3. Collect all word blends (200 words each for Hindi, English, Math)
        val wordBlendsMap = mapOf(
            "hindi" to WordBlendsDataProvider.getHindiWordBlends200(),
            "english" to WordBlendsDataProvider.getEnglishWordBlends200(),
            "math" to WordBlendsDataProvider.getMathWordBlends200()
        )
        require(wordBlendsMap["hindi"]?.size == 200) { "Hindi blends must be 200, got ${wordBlendsMap["hindi"]?.size}" }
        require(wordBlendsMap["english"]?.size == 200) { "English blends must be 200, got ${wordBlendsMap["english"]?.size}" }
        require(wordBlendsMap["math"]?.size == 200) { "Math blends must be 200, got ${wordBlendsMap["math"]?.size}" }

        // 4. Collect all quizzes
        val quizzesMap = mapOf(
            "hindi" to LearningContentRepository.getQuizzes("hindi"),
            "english" to LearningContentRepository.getQuizzes("english"),
            "math" to LearningContentRepository.getQuizzes("math")
        )

        // 5. Build master structure
        val master = MasterAppContent(
            game_items = existingItems,
            alphabet_cards = alphabetCardsMap,
            word_blends = wordBlendsMap,
            quiz_questions = quizzesMap
        )

        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        val jsonOutput = gson.toJson(master)

        // Write to assets/game_content.json
        assetsFile.writeText(jsonOutput, Charsets.UTF_8)

        // Write to root game_content.json (for GitHub repository)
        val rootFile = File("../game_content.json")
        if (rootFile.exists() || rootFile.parentFile?.exists() == true) {
            rootFile.writeText(jsonOutput, Charsets.UTF_8)
        }

        println("Successfully generated Master game_content.json with:")
        println(" - Game items: ${existingItems.size}")
        println(" - Hindi cards: ${alphabetCardsMap["hindi"]?.size}")
        println(" - English cards: ${alphabetCardsMap["english"]?.size}")
        println(" - Math cards: ${alphabetCardsMap["math"]?.size}")
        println(" - Hindi blends: ${wordBlendsMap["hindi"]?.size}")
        println(" - Quizzes: ${quizzesMap["hindi"]?.size}")
    }
}

