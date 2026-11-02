package com.kidslearning.chutki.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.audio.SoundEffectHelper
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CorrectGreen
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.LevelMilestoneCelebration
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PuzzlePiece(
    val id: Int,
    val pieceSymbol: String,
    val labelHindi: String,
    val labelEnglish: String
)

data class JigsawTarget(
    val titleHindi: String,
    val titleEnglish: String,
    val completedEmoji: String,
    val pieces: List<PuzzlePiece>
)

@Composable
fun JigsawPuzzleGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val puzzleTargets = remember {
        listOf(
            JigsawTarget(
                titleHindi = "शेर (Lion)",
                titleEnglish = "Lion Puzzle",
                completedEmoji = "🦁",
                pieces = listOf(
                    PuzzlePiece(0, "👂", "कान", "Ears"),
                    PuzzlePiece(1, "👀", "आँखें", "Eyes"),
                    PuzzlePiece(2, "👃", "नाक", "Nose"),
                    PuzzlePiece(3, "🦁", "दाढ़ी", "Mane")
                )
            ),
            JigsawTarget(
                titleHindi = "रॉकेट (Rocket)",
                titleEnglish = "Rocket Puzzle",
                completedEmoji = "🚀",
                pieces = listOf(
                    PuzzlePiece(0, "🔺", "टिप", "Nose Cone"),
                    PuzzlePiece(1, "🛸", "खिड़की", "Cabin"),
                    PuzzlePiece(2, "🚀", "बॉडी", "Body"),
                    PuzzlePiece(3, "🔥", "आग", "Flame")
                )
            ),
            JigsawTarget(
                titleHindi = "कार (Car)",
                titleEnglish = "Car Puzzle",
                completedEmoji = "🚗",
                pieces = listOf(
                    PuzzlePiece(0, "🚘", "छत", "Roof"),
                    PuzzlePiece(1, "🛞", "पहिया", "Wheel 1"),
                    PuzzlePiece(2, "🚗", "बॉडी", "Body"),
                    PuzzlePiece(3, "🛞", "पहिया", "Wheel 2")
                )
            ),
            JigsawTarget(
                titleHindi = "हाथी (Elephant)",
                titleEnglish = "Elephant Puzzle",
                completedEmoji = "🐘",
                pieces = listOf(
                    PuzzlePiece(0, "👂", "कान", "Big Ears"),
                    PuzzlePiece(1, "🐘", "सूँड", "Trunk"),
                    PuzzlePiece(2, "👀", "आँख", "Eyes"),
                    PuzzlePiece(3, "🦶", "पैर", "Legs")
                )
            ),
            JigsawTarget(
                titleHindi = "रेलगाड़ी (Train)",
                titleEnglish = "Train Puzzle",
                completedEmoji = "🚂",
                pieces = listOf(
                    PuzzlePiece(0, "💨", "धुआं", "Smoke"),
                    PuzzlePiece(1, "🚂", "इंजन", "Engine"),
                    PuzzlePiece(2, "🚃", "डिब्बा", "Bogey"),
                    PuzzlePiece(3, "🛞", "पहिए", "Wheels")
                )
            ),
            JigsawTarget(
                titleHindi = "तितली (Butterfly)",
                titleEnglish = "Butterfly Puzzle",
                completedEmoji = "🦋",
                pieces = listOf(
                    PuzzlePiece(0, "🪽", "पंख 1", "Left Wing"),
                    PuzzlePiece(1, "✨", "एंटीना", "Antenna"),
                    PuzzlePiece(2, "🦋", "धड़", "Body"),
                    PuzzlePiece(3, "🪽", "पंख 2", "Right Wing")
                )
            ),
            JigsawTarget(
                titleHindi = "सेब (Apple)",
                titleEnglish = "Apple Puzzle",
                completedEmoji = "🍎",
                pieces = listOf(
                    PuzzlePiece(0, "🍃", "पत्ती", "Leaf"),
                    PuzzlePiece(1, "🪵", "डंडी", "Stem"),
                    PuzzlePiece(2, "🍎", "सेब", "Apple"),
                    PuzzlePiece(3, "✨", "चमक", "Shine")
                )
            ),
            JigsawTarget(
                titleHindi = "हवाई जहाज़ (Airplane)",
                titleEnglish = "Airplane Puzzle",
                completedEmoji = "✈️",
                pieces = listOf(
                    PuzzlePiece(0, "🛫", "कॉकपिट", "Cockpit"),
                    PuzzlePiece(1, "🪽", "दाहिना पंख", "Right Wing"),
                    PuzzlePiece(2, "✈️", "बॉडी", "Fuselage"),
                    PuzzlePiece(3, "🪽", "बायां पंख", "Left Wing")
                )
            )
        )
    }

    var puzzleIdx by remember { mutableIntStateOf(0) }
    var currentLevel by remember { mutableIntStateOf(1) }
    var streakCombo by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }

    val current = puzzleTargets[puzzleIdx % puzzleTargets.size]
    val placedPieceIds = remember(puzzleIdx) { mutableStateListOf<Int>() }
    var showConfetti by remember { mutableStateOf(false) }
    val isComplete = placedPieceIds.size == 4
    val scope = rememberCoroutineScope()

    LaunchedEffect(puzzleIdx) {
        placedPieceIds.clear()
        showConfetti = false
        isProcessing = false
        ttsManager?.stop()
        if (isEnglish) {
            ttsManager?.speak("Assemble the pieces to complete the ${current.titleEnglish}!")
        } else {
            ttsManager?.speak("टुकड़े जोड़कर ${current.titleHindi} पूरा करो!")
        }
    }

    fun proceedToNextPuzzle() {
        puzzleIdx = (puzzleIdx + 1) % puzzleTargets.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFE8F5E9)))
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

                Text(
                    text = if (isEnglish) "🧩 Jigsaw Puzzle • Level $currentLevel" else "🧩 चित्र पज़ल • लेवल $currentLevel",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF263238)
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CandyOrange)
                        .clickable {
                            ttsManager?.stop()
                            if (isEnglish) {
                                ttsManager?.speak("Tap the pieces below to assemble the puzzle!")
                            } else {
                                ttsManager?.speak("टुकड़ों को छूकर पज़ल पूरा करो!")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 2x2 Target Board
            ClayCard(
                modifier = Modifier.size(240.dp),
                cornerRadius = 28.dp,
                elevation = 8.dp,
                bgGradient = Brush.verticalGradient(
                    if (isComplete) listOf(Color(0xFFE8F8F5), Color(0xFFC8E6C9))
                    else if (isDarkMode) listOf(Color(0xFF262A56), Color(0xFF1E2142))
                    else listOf(Color.White, Color(0xFFFFF9E6))
                ),
                borderHighlightColor = if (isComplete) CorrectGreen else CandyOrange
            ) {
                if (isComplete) {
                    // Full picture assembled
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = current.completedEmoji, fontSize = 90.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEnglish) "🎉 Puzzle Solved! 🎉" else "🎉 पज़ल पूरा हुआ! 🎉",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = CorrectGreen
                            )
                        }
                    }
                } else {
                    // 2x2 grid slots
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PuzzleSlot(piece = current.pieces[0], isPlaced = placedPieceIds.contains(0), isDarkMode = isDarkMode)
                            PuzzleSlot(piece = current.pieces[1], isPlaced = placedPieceIds.contains(1), isDarkMode = isDarkMode)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PuzzleSlot(piece = current.pieces[2], isPlaced = placedPieceIds.contains(2), isDarkMode = isDarkMode)
                            PuzzleSlot(piece = current.pieces[3], isPlaced = placedPieceIds.contains(3), isDarkMode = isDarkMode)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isComplete) {
                    if (isEnglish) "Well Done! Puzzle Complete!" else "शाबाश! आपने पज़ल जोड़ लिया!"
                } else {
                    if (isEnglish) "Tap pieces below to place them above:" else "नीचे से टुकड़े टैप करके ऊपर लगाओ:"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isComplete) CorrectGreen else (if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF455A64))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Available Pieces to tap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                current.pieces.forEach { piece ->
                    val isAlreadyPlaced = placedPieceIds.contains(piece.id)
                    ClayCard(
                        modifier = Modifier
                            .size(68.dp)
                            .clickable(enabled = !isAlreadyPlaced && !isProcessing) {
                                if (isProcessing || isAlreadyPlaced) return@clickable
                                placedPieceIds.add(piece.id)
                                SoundEffectHelper.playPopSound()

                                if (placedPieceIds.size == 4) {
                                    isProcessing = true
                                    showConfetti = true
                                    streakCombo++
                                    SoundEffectHelper.playFanfare()
                                    ttsManager?.celebrateCorrect()
                                    onReward(30 + streakCombo * 5, 3)

                                    scope.launch {
                                        delay(1200)
                                        showMilestoneDialog = true
                                    }
                                }
                            },
                        cornerRadius = 18.dp,
                        elevation = if (isAlreadyPlaced) 1.dp else 5.dp,
                        bgGradient = Brush.verticalGradient(
                            if (isAlreadyPlaced) listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5))
                            else listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isAlreadyPlaced) "✓" else piece.pieceSymbol,
                                fontSize = if (isAlreadyPlaced) 24.sp else 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyPlaced) Color.White else Color(0xFF3E2723)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Next button
            ClayButton(
                onClick = {
                    proceedToNextPuzzle()
                    SoundEffectHelper.playPopSound()
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                primaryColor = CandyOrange,
                shadowColor = Color(0xFFD84315),
                topHighlightColor = Color(0xFFFFB74D)
            ) {
                Text(
                    text = if (isEnglish) "Next Puzzle ▶" else "अगला पज़ल ▶",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        if (showConfetti) {
            StarConfettiOverlay(onAnimationComplete = { showConfetti = false })
        }

        // Level Up Milestone Dialog
        LevelMilestoneCelebration(
            isShowing = showMilestoneDialog,
            level = currentLevel,
            streak = streakCombo,
            isEnglish = isEnglish,
            onContinue = {
                showMilestoneDialog = false
                currentLevel++
                proceedToNextPuzzle()
            }
        )
    }
}

@Composable
private fun PuzzleSlot(piece: PuzzlePiece, isPlaced: Boolean, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isPlaced) Color(0xFFE8F5E9)
                else (if (isDarkMode) Color(0xFF1E2142) else Color(0xFFF5F5F5))
            )
            .border(
                2.dp,
                if (isPlaced) CorrectGreen else Color(0xFFB0BEC5),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaced) {
            Text(text = piece.pieceSymbol, fontSize = 38.sp)
        } else {
            Text(text = "🧩", fontSize = 28.sp, color = Color.Gray)
        }
    }
}
