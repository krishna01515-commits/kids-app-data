package com.kidslearning.chutki.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.ui.components.ClayButton
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.TeacherAvatar3D
import com.kidslearning.chutki.ui.components.TeacherPose

@Composable
fun WelcomeDialogScreen(
    currentKidName: String,
    currentAvatar: String,
    ttsManager: HindiTtsManager?,
    onStartLearning: (name: String, avatar: String) -> Unit
) {
    var kidName by remember { mutableStateOf(currentKidName) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }
    val avatarList = listOf("🦁", "👧", "👦", "🚀", "🐱", "🤖", "🦄", "🐼")

    LaunchedEffect(Unit) {
        ttsManager?.welcomeKidsGreeting(kidName)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFAF0),
                        Color(0xFFFFECC8),
                        Color(0xFFFFD699)
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 32.dp,
            elevation = 14.dp,
            bgGradient = Brush.verticalGradient(
                colors = listOf(Color.White, Color(0xFFFFF7E6))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge with App Logo
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFF7A00))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.kidslearning.chutki.R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "चुटकी किड्स लर्निंग 3D",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Girl Tutor (New 3D Model with Glasses & Stylus)
                TeacherAvatar3D(
                    size = 175.dp,
                    pose = TeacherPose.WELCOME_WAVE,
                    isSpeaking = true,
                    onClick = {
                        ttsManager?.welcomeKidsGreeting(kidName)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Welcoming Speech Card
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 4.dp,
                    bgGradient = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
                    )
                ) {
                    Text(
                        text = "“नमस्ते प्यारे बच्चों! मैं हूँ आपकी टीचर प्रिया दीदी! चलो आज मिलकर खेल-खेल में मजेदार पढ़ाई करते हैं!”",
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kid's Name Field
                Text(
                    text = "आपका प्यारा नाम क्या है?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = kidName,
                    onValueChange = { kidName = it },
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = CandyOrange,
                        unfocusedIndicatorColor = Color(0xFFFFD54F)
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Selector
                Text(
                    text = "अपना पसंदीदा दोस्त चुनो:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF546E7A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    avatarList.take(6).forEach { avatar ->
                        val isSelected = avatar == selectedAvatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) CandyOrange else Color(0xFFFFF3E0)
                                )
                                .clickable { selectedAvatar = avatar },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatar, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 3D Start Button
                ClayButton(
                    onClick = {
                        val finalName = kidName.ifBlank { "नन्हे उस्ताद" }
                        onStartLearning(finalName, selectedAvatar)
                    },
                    modifier = Modifier.fillMaxWidth(0.85f),
                    primaryColor = Color(0xFF00E676),
                    shadowColor = Color(0xFF00B248),
                    topHighlightColor = Color(0xFF69F0AE)
                ) {
                    Text(
                        text = "पढ़ाई शुरू करो! 🚀",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

