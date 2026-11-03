package com.kidslearning.chutki.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ShapeTarget(
    val id: String,
    val shapeKey: String,
    val emoji: String,
    val nameHindi: String,
    val nameEnglish: String,
    val boxEmoji: String
)

data class ShapeSorterRound(
    val currentShape: ShapeTarget,
    val boxOptions: List<ShapeTarget>
)

@Composable
fun ShapeColorSorterGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val masterShapes = remember {
        listOf(
            ShapeTarget("c_red", "circle", "🔴", "लाल गोला (Circle)", "Red Circle", "⚪"),
            ShapeTarget("c_blue", "circle", "🔵", "नीला गोला (Circle)", "Blue Circle", "⚪"),
            ShapeTarget("c_yellow", "circle", "🟡", "पीला गोला (Circle)", "Yellow Circle", "⚪"),
            ShapeTarget("c_green", "circle", "🟢", "हरा गोला (Circle)", "Green Circle", "⚪"),

            ShapeTarget("s_red", "square", "🟥", "लाल चौकोर (Square)", "Red Square", "⬜"),
            ShapeTarget("s_blue", "square", "🟦", "नीला चौकोर (Square)", "Blue Square", "⬜"),
            ShapeTarget("s_green", "square", "🟩", "हरा चौकोर (Square)", "Green Square", "⬜"),
            ShapeTarget("s_yellow", "square", "🟨", "पीला चौकोर (Square)", "Yellow Square", "⬜"),

            ShapeTarget("t_red", "triangle", "🔺", "लाल त्रिकोण (Triangle)", "Red Triangle", "🔺"),
            ShapeTarget("t_orange", "triangle", "🔻", "नारंगी त्रिकोण (Triangle)", "Orange Triangle", "🔺"),

            ShapeTarget("st_gold", "star", "⭐", "सुनहरा सितारा (Star)", "Golden Star", "⭐"),
            ShapeTarget("st_sparkle", "star", "✨", "चमकता सितारा (Sparkle)", "Sparkling Star", "⭐"),

            ShapeTarget("h_pink", "heart", "💖", "गुलाबी दिल (Heart)", "Pink Heart", "🤍"),
            ShapeTarget("h_red", "heart", "❤️", "लाल दिल (Heart)", "Red Heart", "🤍"),

            ShapeTarget("d_blue", "diamond", "🔷", "नीला डायमंड (Diamond)", "Blue Diamond", "💠"),
            ShapeTarget("d_orange", "diamond", "🔶", "नारंगी डायमंड (Diamond)", "Orange Diamond", "💠")
        )
    }

    val (savedLevel, savedScore) = remember {
        userPrefs?.getGameProgress("SHAPE_COLOR_SORTER") ?: Pair(1, 0)
    }
    var gameLevel by remember { mutableIntStateOf(savedLevel.coerceAtLeast(1)) }
    var roundScore by remember { mutableIntStateOf(savedScore.coerceAtLeast(0)) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(savedScore.coerceAtLeast(0) + 1) }

    // Persist game level and score automatically
    LaunchedEffect(gameLevel, roundScore) {
        userPrefs?.saveGameProgress("SHAPE_COLOR_SORTER", gameLevel, roundScore, roundCount)
    }

    fun generateRound(): ShapeSorterRound {
        val target = masterShapes.random()
        val distinctShapeKeys = masterShapes.map { it.shapeKey }.distinct()
        val otherKeys = distinctShapeKeys.filter { it != target.shapeKey }.shuffled()

        val optionsCount = if (gameLevel < 3) 2 else 3
        val chosenKeys = (listOf(target.shapeKey) + otherKeys.take(optionsCount)).shuffled()

        val options = chosenKeys.map { key ->
            masterShapes.first { it.shapeKey == key }
        }

        return ShapeSorterRound(currentShape = target, boxOptions = options)
    }

    var currentRound by remember { mutableStateOf(generateRound()) }

    var selectedBox by remember { mutableStateOf<String?>(null) }
    var isSolved by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showMilestone by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadNextRound() {
        selectedBox = null
        isSolved = false
        showConfetti = false
        isProcessing = false

        currentRound = generateRound()
        roundCount++

        ttsManager?.stop()
        val shapeName = if (isEnglish) currentRound.currentShape.nameEnglish else currentRound.currentShape.nameHindi
        val prompt = if (isEnglish) {
            "This is a $shapeName! Put it into the matching shape box!"
        } else {
            "यह ${currentRound.currentShape.nameHindi} है! इसे सही आकार वाले डिब्बे में डालो!"
        }
        ttsManager?.speak(prompt)
    }

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        val shapeName = if (isEnglish) currentRound.currentShape.nameEnglish else currentRound.currentShape.nameHindi
        val prompt = if (isEnglish) {
            "This is a $shapeName! Put it into the matching shape box!"
        } else {
            "यह ${currentRound.currentShape.nameHindi} है! इसे सही आकार वाले डिब्बे में डालो!"
        }
        ttsManager?.speak(prompt)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFF8E1), Color(0xFFE8F5E9)))
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF262A56) else Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⬅️", fontSize = 18.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "🔷 Shape & Color Sorter" else "🔷 आकार व रंग छंटाई",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isEnglish) "Level $gameLevel • Round $roundCount" else "लेवल $gameLevel • राउंड $roundCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyOrange
                        )
                        if (currentStreak > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔥 x$currentStreak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF5722)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CandyOrange)
                        .clickable {
                            ttsManager?.stop()
                            val prompt = if (isEnglish) "Spot the shape and choose the right box!" else "आकार पहचानो और सही डिब्बा चुनो!"
                            ttsManager?.speak(prompt)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Active Shape Display Box
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                cornerRadius = 28.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                ),
                borderHighlightColor = if (isSolved) CorrectGreen else CandyOrange
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = currentRound.currentShape.emoji, fontSize = 78.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSolved) {
                            "⭐ " + (if (isEnglish) "Matched: ${currentRound.currentShape.nameEnglish}!" else "सही डिब्बे में गया: ${currentRound.currentShape.nameHindi}!") + " ⭐"
                        } else {
                            if (isEnglish) currentRound.currentShape.nameEnglish else currentRound.currentShape.nameHindi
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSolved) CorrectGreen else CandyOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isEnglish) "Tap the matching shape box below:" else "सही आकार वाले डिब्बे पर टैप करो:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Sorting Boxes Row (Debounced & Dynamic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentRound.boxOptions.forEach { box ->
                    val isChosen = selectedBox == box.shapeKey
                    val isCorrect = box.shapeKey == currentRound.currentShape.shapeKey

                    ClayCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(125.dp)
                            .clickable(enabled = !isProcessing) {
                                if (!isProcessing) {
                                    selectedBox = box.shapeKey
                                    if (isCorrect) {
                                        isProcessing = true
                                        isSolved = true
                                        showConfetti = true
                                        roundScore++
                                        currentStreak++

                                        SoundEffectHelper.playCorrectChime()
                                        SoundEffectHelper.playComboSound(currentStreak)
                                        ttsManager?.stop()

                                        if (isEnglish) {
                                            ttsManager?.speak("Well done! That's the correct ${box.shapeKey} box!")
                                        } else {
                                            ttsManager?.speak("शाबाश! बिल्कुल सही डिब्बा चुना!")
                                        }
                                        onReward(35, 2)

                                        scope.launch {
                                            delay(1400)
                                            if (roundScore % 5 == 0) {
                                                gameLevel++
                                                SoundEffectHelper.playFanfare()
                                                showMilestone = true
                                            } else {
                                                loadNextRound()
                                            }
                                        }
                                    } else {
                                        currentStreak = 0
                                        SoundEffectHelper.playSoftTryAgain()
                                        ttsManager?.stop()
                                        if (isEnglish) {
                                            ttsManager?.speak("Look closely! That box has a different shape!")
                                        } else {
                                            ttsManager?.speak("यह अलग आकार का डिब्बा है! ध्यान से मिलाओ!")
                                        }
                                    }
                                }
                            },
                        cornerRadius = 22.dp,
                        elevation = 6.dp,
                        bgGradient = Brush.verticalGradient(
                            if (isChosen && isSolved) listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                            else if (isDarkMode) listOf(Color(0xFF1E2142), Color(0xFF151833))
                            else listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9))
                        ),
                        borderHighlightColor = if (isChosen && isSolved) CorrectGreen else Color(0xFF7E57C2)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📦", fontSize = 32.sp)
                            Text(text = box.boxEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isEnglish) box.shapeKey.replaceFirstChar { it.uppercase() } else box.nameHindi.split(" ").first(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen && isSolved) Color.White else (if (isDarkMode) Color.White else Color(0xFF3E2723))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Next button
            ClayButton(
                onClick = {
                    if (!isProcessing) {
                        SoundEffectHelper.playPopSound()
                        loadNextRound()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                primaryColor = CandyOrange,
                shadowColor = Color(0xFFD84315),
                topHighlightColor = Color(0xFFFFB74D)
            ) {
                Text(
                    text = if (isEnglish) "Next Shape ▶" else "अगला आकार ▶",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        if (showConfetti) {
            StarConfettiOverlay(onAnimationComplete = { showConfetti = false })
        }

        LevelMilestoneCelebration(
            isShowing = showMilestone,
            level = gameLevel,
            streak = currentStreak,
            isEnglish = isEnglish,
            onContinue = {
                showMilestone = false
                loadNextRound()
            }
        )
    }
}
