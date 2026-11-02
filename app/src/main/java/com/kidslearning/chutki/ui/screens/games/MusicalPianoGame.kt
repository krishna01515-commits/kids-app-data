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
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.StarConfettiOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PianoKey(
    val noteHindi: String,
    val noteEnglish: String,
    val freq: Double,
    val color: Color
)

@Composable
fun MusicalPianoGame(
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    ttsManager: HindiTtsManager?,
    onBack: () -> Unit,
    onReward: (xp: Int, stars: Int) -> Unit
) {
    val pianoKeys = remember {
        listOf(
            PianoKey("सा", "C", 261.63, Color(0xFFFF1744)),
            PianoKey("रे", "D", 293.66, Color(0xFFFF9100)),
            PianoKey("ग", "E", 329.63, Color(0xFFFFD600)),
            PianoKey("म", "F", 349.23, Color(0xFF00E676)),
            PianoKey("प", "G", 392.00, Color(0xFF00B0FF)),
            PianoKey("ध", "A", 440.00, Color(0xFF651FFF)),
            PianoKey("नि", "B", 493.88, Color(0xFFE040FB)),
            PianoKey("सां", "C5", 523.25, Color(0xFFFF4081))
        )
    }

    var notesPlayedCount by remember { mutableIntStateOf(0) }
    var activeNoteIndex by remember { mutableStateOf<Int?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        ttsManager?.stop()
        if (isEnglish) {
            ttsManager?.speak("Play the magical piano! Tap the colorful keys and hear animal sounds!")
        } else {
            ttsManager?.speak("जादुई पियानो बजाओ! रंग-बिरंगी कीज़ छूकर सुर सुनो और जानवरों की आवाज़ें सुनो!")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFF3E5F5)))
            )
            .padding(14.dp)
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
                    text = if (isEnglish) "🎹 Musical Piano" else "🎹 जादुई पियानो (Musical Piano)",
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
                                ttsManager?.speak("Play piano notes or listen to rhymes!")
                            } else {
                                ttsManager?.speak("पियानो कीज बजाओ या कविताएं सुनो!")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔊", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Animal Sounds Bar
            Text(
                text = if (isEnglish) "🐾 Animal Voices:" else "🐾 जानवरों की प्यारी आवाजें सुनो:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Cow
                AnimalTile(
                    emoji = "🐮",
                    name = if (isEnglish) "Cow" else "गाय",
                    onClick = {
                        SoundEffectHelper.playAnimalSound("cow")
                        ttsManager?.stop()
                        if (isEnglish) {
                            ttsManager?.speak("The cow says: Moo-Moo!")
                        } else {
                            ttsManager?.speak("गाय कहती है: अम्मा! Moo-Moo!")
                        }
                    }
                )

                // Lion
                AnimalTile(
                    emoji = "🦁",
                    name = if (isEnglish) "Lion" else "शेर",
                    onClick = {
                        SoundEffectHelper.playAnimalSound("lion")
                        ttsManager?.stop()
                        if (isEnglish) {
                            ttsManager?.speak("The lion roars: Roar-Roar!")
                        } else {
                            ttsManager?.speak("शेर दहाड़ता है: रोआर! Roar!")
                        }
                    }
                )

                // Bird
                AnimalTile(
                    emoji = "🐦",
                    name = if (isEnglish) "Bird" else "चिड़िया",
                    onClick = {
                        SoundEffectHelper.playAnimalSound("bird")
                        ttsManager?.stop()
                        if (isEnglish) {
                            ttsManager?.speak("The bird chirps: Tweet-Tweet!")
                        } else {
                            ttsManager?.speak("चिड़िया चहकती है: चीं-चीं! Tweet-Tweet!")
                        }
                    }
                )

                // Cat
                AnimalTile(
                    emoji = "🐱",
                    name = if (isEnglish) "Cat" else "बिल्ली",
                    onClick = {
                        SoundEffectHelper.playAnimalSound("cat")
                        ttsManager?.stop()
                        if (isEnglish) {
                            ttsManager?.speak("The cat says: Meow-Meow!")
                        } else {
                            ttsManager?.speak("बिल्ली कहती है: म्याऊं-म्याऊं! Meow-Meow!")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Nursery Rhymes Autoplay Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClayButton(
                    onClick = {
                        scope.launch {
                            ttsManager?.stop()
                            if (isEnglish) {
                                ttsManager?.speak("Twinkle Twinkle Little Star!")
                            } else {
                                ttsManager?.speak("ट्विंकल ट्विंकल लिटिल स्टार!")
                            }
                            delay(1200)
                            // Play Twinkle tune: C C G G A A G
                            val twinkleNotes = listOf(261.63, 261.63, 392.0, 392.0, 440.0, 440.0, 392.0)
                            twinkleNotes.forEach { f ->
                                SoundEffectHelper.playPianoNote(f, 320)
                                delay(360)
                            }
                            showConfetti = true
                            onReward(35, 2)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = Color(0xFF00B0FF),
                    shadowColor = Color(0xFF0081CB),
                    topHighlightColor = Color(0xFF80D8FF)
                ) {
                    Text(
                        text = if (isEnglish) "⭐ Twinkle Star" else "⭐ ट्विंकल स्टार",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ClayButton(
                    onClick = {
                        scope.launch {
                            ttsManager?.stop()
                            if (isEnglish) {
                                ttsManager?.speak("Chanda Mama Door Ke!")
                            } else {
                                ttsManager?.speak("चंदा मामा दूर के! पुए पकाएं बूर के!")
                            }
                            delay(1400)
                            val chandaNotes = listOf(329.63, 349.23, 392.0, 349.23, 329.63, 293.66, 261.63)
                            chandaNotes.forEach { f ->
                                SoundEffectHelper.playPianoNote(f, 320)
                                delay(360)
                            }
                            showConfetti = true
                            onReward(35, 2)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    primaryColor = Color(0xFFE040FB),
                    shadowColor = Color(0xFFAA00FF),
                    topHighlightColor = Color(0xFFEA80FC)
                ) {
                    Text(
                        text = if (isEnglish) "🌙 Chanda Mama" else "🌙 चंदा मामा",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. 8 Piano Keys (Keyboard)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF212121))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pianoKeys.forEachIndexed { idx, key ->
                    val isPressed = activeNoteIndex == idx
                    val keyScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1.0f,
                        animationSpec = spring(),
                        label = "key_press"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .scale(keyScale)
                            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(key.color.copy(alpha = 0.85f), key.color)
                                )
                            )
                            .border(
                                1.5.dp,
                                if (isPressed) Color.White else Color.Black.copy(alpha = 0.2f),
                                RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            )
                            .clickable {
                                activeNoteIndex = idx
                                SoundEffectHelper.playPianoNote(key.freq, 300)
                                notesPlayedCount++

                                if (notesPlayedCount == 8) {
                                    showConfetti = true
                                    SoundEffectHelper.playFanfare()
                                    ttsManager?.stop()
                                    if (isEnglish) {
                                        ttsManager?.speak("Bravo little musician! You played all the notes!")
                                    } else {
                                        ttsManager?.speak("शाबाश नन्हे संगीतकार! आपने पूरे सुर बजा लिए!")
                                    }
                                    onReward(35, 2)
                                }

                                scope.launch {
                                    delay(200)
                                    activeNoteIndex = null
                                }
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = if (isEnglish) key.noteEnglish else key.noteHindi,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isEnglish) key.noteHindi else key.noteEnglish,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        if (showConfetti) {
            StarConfettiOverlay(onAnimationComplete = { showConfetti = false })
        }
    }
}

@Composable
private fun AnimalTile(emoji: String, name: String, onClick: () -> Unit) {
    ClayCard(
        modifier = Modifier
            .size(68.dp)
            .clickable { onClick() },
        cornerRadius = 18.dp,
        elevation = 4.dp,
        bgGradient = Brush.verticalGradient(
            listOf(Color.White, Color(0xFFFFF8E1))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
        }
    }
}
