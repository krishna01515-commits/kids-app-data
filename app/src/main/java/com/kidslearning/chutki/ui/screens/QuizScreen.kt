package com.kidslearning.chutki.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kidslearning.chutki.model.GameCategory
import com.kidslearning.chutki.model.GameType
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose
import com.kidslearning.chutki.ui.screens.games.BalloonPopGame
import com.kidslearning.chutki.ui.screens.games.CardFlipMemoryGame
import com.kidslearning.chutki.ui.screens.games.CountCollectGame
import com.kidslearning.chutki.ui.screens.games.DigitalColoringGame
import com.kidslearning.chutki.ui.screens.games.FingerTracingGame
import com.kidslearning.chutki.ui.screens.games.JigsawPuzzleGame
import com.kidslearning.chutki.ui.screens.games.MissingLettersGame
import com.kidslearning.chutki.ui.screens.games.MusicalPianoGame
import com.kidslearning.chutki.ui.screens.games.NumberTrainGame
import com.kidslearning.chutki.ui.screens.games.OddOneOutGame
import com.kidslearning.chutki.ui.screens.games.ShadowMatchingGame
import com.kidslearning.chutki.ui.screens.games.ShapeColorSorterGame
import com.kidslearning.chutki.ui.screens.games.SizeComparisonGame
import com.kidslearning.chutki.ui.screens.games.WordMatchGame
import com.kidslearning.chutki.data.UserPreferencesManager

@Composable
fun QuizScreen(
    subject: SubjectType,
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    userPrefs: UserPreferencesManager? = null,
    onBack: () -> Unit,
    onAddReward: (xp: Int, stars: Int) -> Unit = { _, _ -> },
    onQuizAnswered: (questionId: Int) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(GameCategory.LITERACY) }
    var activeGame by remember { mutableStateOf<GameType?>(null) }

    LaunchedEffect(isEnglish) {
        if (activeGame == null) {
            if (isEnglish) {
                ttsManager?.speak("Welcome to the Games Hub! Choose any fun game to play!")
            } else {
                ttsManager?.speak("खेल संसार में आपका स्वागत है! अपनी पसंद की श्रेणी और खेल चुनो!")
            }
        }
    }

    // If a game is actively running, show that game screen
    if (activeGame != null) {
        val game = activeGame!!
        val rewardHandler: (Int, Int) -> Unit = { xp, stars ->
            onAddReward(xp, stars)
            onQuizAnswered(game.ordinal + 1)
        }

        when (game) {
            // Category 1: भाषा और अक्षर ज्ञान
            GameType.FINGER_TRACING -> {
                FingerTracingGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.BALLOON_POP -> {
                BalloonPopGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.WORD_MATCH -> {
                WordMatchGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.MISSING_LETTERS -> {
                MissingLettersGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }

            // Category 2: गणित और तार्किक सोच
            GameType.COUNT_COLLECT -> {
                CountCollectGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.SHAPE_COLOR_SORTER -> {
                ShapeColorSorterGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.NUMBER_TRAIN -> {
                NumberTrainGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.SIZE_COMPARISON -> {
                SizeComparisonGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }

            // Category 3: मेमोरी और फोकस
            GameType.CARD_MEMORY -> {
                CardFlipMemoryGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.ODD_ONE_OUT -> {
                OddOneOutGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.SHADOW_MATCHING -> {
                ShadowMatchingGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }

            // Category 4: रचनात्मकता और मोटर स्किल्स
            GameType.DIGITAL_COLORING -> {
                DigitalColoringGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.JIGSAW_PUZZLE -> {
                JigsawPuzzleGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
            GameType.MUSICAL_PIANO -> {
                MusicalPianoGame(
                    isDarkMode = isDarkMode,
                    isEnglish = isEnglish,
                    ttsManager = ttsManager,
                    userPrefs = userPrefs,
                    onBack = { activeGame = null },
                    onReward = rewardHandler
                )
            }
        }
        return
    }

    // MAIN GAMES HUB SCREEN (Category Selector & Game Cards)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // 1. Top Header
        item {
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
                        .background(if (isDarkMode) Color(0xFF262A56) else Color(0xFFFFECB3))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⬅️", fontSize = 18.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEnglish) "🎯 Kids Games Zone" else "🎯 नन्हे उस्तादों का खेल संसार",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF263238)
                    )
                    Text(
                        text = if (isEnglish) "Play, learn and win shiny stars!" else "खेल-खेल में सीखो और सितारे जीतो!",
                        fontSize = 12.sp,
                        color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CandyOrange)
                        .clickable {
                            if (isEnglish) {
                                ttsManager?.speak("Choose any game you like and have fun learning!")
                            } else {
                                ttsManager?.speak("अपनी पसंद का कोई भी खेल चुनो और मजे करो!")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Priya Didi Guidance Banner
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                elevation = 6.dp,
                bgGradient = Brush.horizontalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeacherAvatar3D(
                        size = 85.dp,
                        pose = TeacherPose.POINTING_AT_CARD,
                        isSpeaking = false
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isEnglish) "Teacher Priya says:" else "प्रिया दीदी कहती हैं:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CandyOrange
                        )
                        Text(
                            text = if (isEnglish) "“Playing games makes your mind sharp and creative! Pick a game and enjoy!”" else "“खेलने से दिमाग तेज होता है! कोई भी गेम चुनो और मजे से खेलो!”",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF3E2723)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Category Horizontal Pills Selector (4 Categories)
        item {
            Text(
                text = if (isEnglish) "📁 Select Game Category:" else "📁 खेल की श्रेणी चुनें (Select Category):",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDarkMode) Color.White else Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GameCategory.entries) { cat ->
                    val isSelected = cat == selectedCategory
                    val catColor = Color(cat.themeColorHex)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) catColor
                                else if (isDarkMode) Color(0xFF1E2142) else Color(0xFFF0F0F0)
                            )
                            .clickable {
                                selectedCategory = cat
                                SoundEffectHelper.playPopSound()
                                if (isEnglish) {
                                    ttsManager?.speak("${cat.titleEnglish} games!")
                                } else {
                                    ttsManager?.speak("${cat.titleHindi} के खेल!")
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = cat.iconEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEnglish) cat.titleEnglish else cat.titleHindi,
                                color = if (isSelected) Color.White else (if (isDarkMode) Color.White else Color(0xFF455A64)),
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Game Cards for Selected Category
        val categoryGames = GameType.entries.filter { it.category == selectedCategory }

        items(categoryGames) { game ->
            val catColor = Color(selectedCategory.themeColorHex)
            val (savedLvl, savedScr) = userPrefs?.getGameProgress(game.name) ?: Pair(1, 0)
            val isLastPlayed = userPrefs?.getLastPlayedGame() == game.name

            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        SoundEffectHelper.playPopSound()
                        if (isEnglish) {
                            ttsManager?.speak("Let's play: ${game.titleEnglish}!")
                        } else {
                            ttsManager?.speak("चलो खेलते हैं: ${game.titleHindi}!")
                        }
                        userPrefs?.setLastPlayedGame(game.name)
                        activeGame = game
                    },
                cornerRadius = 24.dp,
                elevation = 6.dp,
                bgGradient = Brush.verticalGradient(
                    if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                ),
                borderHighlightColor = catColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Icon Box
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(catColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = game.iconEmoji, fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEnglish) game.titleEnglish else game.titleHindi,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF263238)
                        )
                        Text(
                            text = if (isEnglish) game.subtitleEnglish else game.subtitleHindi,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${game.xpReward} XP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandyOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⭐".repeat(game.starReward),
                                fontSize = 12.sp
                            )
                            if (savedLvl > 1 || savedScr > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEnglish) "Lvl $savedLvl • Score $savedScr" else "लेवल $savedLvl • स्कोर $savedScr",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CorrectGreen
                                )
                            }
                            if (isLastPlayed) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "• Last Played" else "• पिछली बार खेला",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CandyOrange
                                )
                            }
                        }
                    }

                    // Play Arrow Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (savedLvl > 1 || savedScr > 0) CorrectGreen else catColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "▶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
