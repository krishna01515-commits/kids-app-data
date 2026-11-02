package com.kidslearning.chutki

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.data.UserPreferencesManager
import com.kidslearning.chutki.model.SubjectType
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.ChutkiKidsTheme
import com.kidslearning.chutki.ui.screens.DashboardScreen
import com.kidslearning.chutki.ui.screens.LeaderboardScreen
import com.kidslearning.chutki.ui.screens.Level1AlphabetCardsScreen
import com.kidslearning.chutki.ui.screens.Level2TracingScreen
import com.kidslearning.chutki.ui.screens.Level3WordBuilderScreen
import com.kidslearning.chutki.ui.screens.ProfileSettingsScreen
import com.kidslearning.chutki.ui.screens.QuizScreen
import com.kidslearning.chutki.ui.screens.SubjectLevelsScreen
import com.kidslearning.chutki.ui.screens.WelcomeDialogScreen

enum class AppNavDestination {
    WELCOME,
    DASHBOARD,
    SUBJECT_LEVELS,
    LEVEL_1_CARDS,
    LEVEL_2_TRACING,
    LEVEL_3_WORD_BUILDER,
    QUIZ,
    LEADERBOARD,
    PROFILE
}

enum class BottomTab(val labelHi: String, val labelEn: String, val icon: String, val destination: AppNavDestination) {
    HOME("होम", "Home", "🏠", AppNavDestination.DASHBOARD),
    COURSES("विषय", "Subjects", "📚", AppNavDestination.SUBJECT_LEVELS),
    QUIZ("खेल", "Games", "🎯", AppNavDestination.QUIZ),
    LEADERBOARD("रैंक", "Ranks", "🏆", AppNavDestination.LEADERBOARD),
    PROFILE("प्रोफाइल", "Profile", "👤", AppNavDestination.PROFILE)
}

@Composable
fun ChutkiKidsApp(
    userPrefs: UserPreferencesManager,
    ttsManager: HindiTtsManager?
) {
    val userProfile by userPrefs.userProfileState.collectAsState()

    var currentScreen by remember {
        mutableStateOf(AppNavDestination.DASHBOARD)
    }

    var selectedSubject by remember {
        mutableStateOf(
            SubjectType.entries.find { it.id == userProfile.currentSubjectId } ?: SubjectType.HINDI
        )
    }

    // 1. Speak greeting every time the app opens/starts
    LaunchedEffect(Unit) {
        ttsManager?.setAppLanguage(userProfile.appLanguage)
        delay(400)
        ttsManager?.speakWelcomeBackGreeting(userProfile.name, userProfile.isEnglish)
    }

    // 2. Phone Hardware Back Button: Steps back one screen instead of closing app
    BackHandler(enabled = currentScreen != AppNavDestination.DASHBOARD) {
        when (currentScreen) {
            AppNavDestination.LEVEL_1_CARDS,
            AppNavDestination.LEVEL_2_TRACING,
            AppNavDestination.LEVEL_3_WORD_BUILDER -> {
                currentScreen = AppNavDestination.SUBJECT_LEVELS
            }
            AppNavDestination.SUBJECT_LEVELS,
            AppNavDestination.QUIZ,
            AppNavDestination.LEADERBOARD,
            AppNavDestination.PROFILE,
            AppNavDestination.WELCOME -> {
                currentScreen = AppNavDestination.DASHBOARD
            }
            AppNavDestination.DASHBOARD -> {
                // Not enabled on dashboard, allows standard exit
            }
        }
    }

    ChutkiKidsTheme(darkTheme = userProfile.isDarkMode) {
        Scaffold(
            bottomBar = {
                // Bottom bar is shown on primary browsing screens
                if (currentScreen in listOf(
                        AppNavDestination.DASHBOARD,
                        AppNavDestination.SUBJECT_LEVELS,
                        AppNavDestination.QUIZ,
                        AppNavDestination.LEADERBOARD,
                        AppNavDestination.PROFILE
                    )
                ) {
                    Kids3DBottomNavigation(
                        currentScreen = currentScreen,
                        isDarkMode = userProfile.isDarkMode,
                        isEnglish = userProfile.isEnglish,
                        onTabSelected = { tab ->
                            currentScreen = tab.destination
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                    when (screen) {
                        AppNavDestination.WELCOME -> {
                            WelcomeDialogScreen(
                                currentKidName = userProfile.name,
                                currentAvatar = userProfile.avatarEmoji,
                                ttsManager = ttsManager,
                                onStartLearning = { name, avatar ->
                                    userPrefs.updateProfile(name, avatar, userProfile.age)
                                    currentScreen = AppNavDestination.DASHBOARD
                                }
                            )
                        }

                        AppNavDestination.DASHBOARD -> {
                            DashboardScreen(
                                userProfile = userProfile,
                                onSelectSubject = { subject ->
                                    selectedSubject = subject
                                    userPrefs.setCurrentSubject(subject.id)
                                    currentScreen = AppNavDestination.SUBJECT_LEVELS
                                },
                                onResumeLesson = {
                                    val activeUnlocked = when (selectedSubject) {
                                        SubjectType.HINDI -> userProfile.hindiUnlockedLevel
                                        SubjectType.ENGLISH -> userProfile.englishUnlockedLevel
                                        SubjectType.MATH -> userProfile.mathUnlockedLevel
                                    }
                                    currentScreen = when (activeUnlocked) {
                                        1 -> AppNavDestination.LEVEL_1_CARDS
                                        2 -> AppNavDestination.LEVEL_2_TRACING
                                        3 -> AppNavDestination.LEVEL_3_WORD_BUILDER
                                        4 -> AppNavDestination.QUIZ
                                        else -> AppNavDestination.LEVEL_1_CARDS
                                    }
                                },
                                onToggleDarkMode = {
                                    userPrefs.setDarkMode(!userProfile.isDarkMode)
                                },
                                onToggleLanguage = { lang ->
                                    userPrefs.setAppLanguage(lang)
                                    ttsManager?.speakLanguageChanged(lang)
                                }
                            )
                        }

                        AppNavDestination.SUBJECT_LEVELS -> {
                            SubjectLevelsScreen(
                                subject = selectedSubject,
                                userProfile = userProfile,
                                userPrefs = userPrefs,
                                ttsManager = ttsManager,
                                onBackToDashboard = {
                                    currentScreen = AppNavDestination.DASHBOARD
                                },
                                onOpenLevel = { levelNum ->
                                    currentScreen = when (levelNum) {
                                        1 -> AppNavDestination.LEVEL_1_CARDS
                                        2 -> AppNavDestination.LEVEL_2_TRACING
                                        3 -> AppNavDestination.LEVEL_3_WORD_BUILDER
                                        4 -> AppNavDestination.QUIZ
                                        else -> AppNavDestination.LEVEL_1_CARDS
                                    }
                                }
                            )
                        }

                        AppNavDestination.LEVEL_1_CARDS -> {
                            Level1AlphabetCardsScreen(
                                subject = selectedSubject,
                                isDarkMode = userProfile.isDarkMode,
                                ttsManager = ttsManager,
                                onBack = {
                                    currentScreen = AppNavDestination.SUBJECT_LEVELS
                                },
                                onNavigateToLevel2 = {
                                    userPrefs.unlockLevel(selectedSubject.id, 2)
                                    currentScreen = AppNavDestination.LEVEL_2_TRACING
                                },
                                onCardLearned = { cardId ->
                                    userPrefs.recordCardLearned(selectedSubject.id, cardId)
                                }
                            )
                        }

                        AppNavDestination.LEVEL_2_TRACING -> {
                            Level2TracingScreen(
                                subject = selectedSubject,
                                isDarkMode = userProfile.isDarkMode,
                                ttsManager = ttsManager,
                                onBack = {
                                    currentScreen = AppNavDestination.SUBJECT_LEVELS
                                },
                                onNavigateToLevel3 = {
                                    userPrefs.unlockLevel(selectedSubject.id, 3)
                                    currentScreen = AppNavDestination.LEVEL_3_WORD_BUILDER
                                },
                                onLetterTraced = { char ->
                                    userPrefs.recordLetterTraced(selectedSubject.id, char)
                                }
                            )
                        }

                        AppNavDestination.LEVEL_3_WORD_BUILDER -> {
                            Level3WordBuilderScreen(
                                subject = selectedSubject,
                                isDarkMode = userProfile.isDarkMode,
                                ttsManager = ttsManager,
                                onBack = {
                                    currentScreen = AppNavDestination.SUBJECT_LEVELS
                                },
                                onNavigateToQuiz = {
                                    userPrefs.unlockLevel(selectedSubject.id, 4)
                                    currentScreen = AppNavDestination.QUIZ
                                },
                                onWordBuilt = { wordId ->
                                    userPrefs.recordWordBuilt(selectedSubject.id, wordId)
                                }
                            )
                        }

                        AppNavDestination.QUIZ -> {
                            QuizScreen(
                                subject = selectedSubject,
                                isDarkMode = userProfile.isDarkMode,
                                isEnglish = userProfile.isEnglish,
                                ttsManager = ttsManager,
                                onBack = {
                                    currentScreen = AppNavDestination.DASHBOARD
                                },
                                onAddReward = { xp, stars ->
                                    userPrefs.addXpAndStars(xp, stars)
                                },
                                onQuizAnswered = { questionId ->
                                    userPrefs.recordQuizAnswered(selectedSubject.id, questionId)
                                }
                            )
                        }

                        AppNavDestination.LEADERBOARD -> {
                            LeaderboardScreen(
                                userProfile = userProfile,
                                isDarkMode = userProfile.isDarkMode
                            )
                        }

                        AppNavDestination.PROFILE -> {
                            ProfileSettingsScreen(
                                userProfile = userProfile,
                                isDarkMode = userProfile.isDarkMode,
                                ttsManager = ttsManager,
                                onToggleDarkMode = { userPrefs.setDarkMode(it) },
                                onToggleLanguage = { lang ->
                                    userPrefs.setAppLanguage(lang)
                                    ttsManager?.speakLanguageChanged(lang)
                                },
                                onUpdateProfile = { name, avatar, age ->
                                    userPrefs.updateProfile(name, avatar, age)
                                },
                                onLoginParent = { contact ->
                                    userPrefs.setParentLogin(contact, true)
                                },
                                onLogoutParent = {
                                    userPrefs.logoutParent()
                                },
                                onResetProgress = {
                                    userPrefs.resetProgress()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Kids3DBottomNavigation(
    currentScreen: AppNavDestination,
    isDarkMode: Boolean,
    isEnglish: Boolean = false,
    onTabSelected: (BottomTab) -> Unit
) {
    val navShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(16.dp, navShape)
            .clip(navShape)
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF1E2142), Color(0xFF13142B)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF7E6)))
            )
            .border(
                width = 2.dp,
                color = if (isDarkMode) Color(0xFF333866) else Color(0xFFFFE0B2),
                shape = navShape
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = when (tab) {
                    BottomTab.HOME -> currentScreen == AppNavDestination.DASHBOARD
                    BottomTab.COURSES -> currentScreen == AppNavDestination.SUBJECT_LEVELS
                    BottomTab.QUIZ -> currentScreen == AppNavDestination.QUIZ
                    BottomTab.LEADERBOARD -> currentScreen == AppNavDestination.LEADERBOARD
                    BottomTab.PROFILE -> currentScreen == AppNavDestination.PROFILE
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 44.dp else 36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.radialGradient(listOf(CandyYellow, CandyOrange))
                                } else {
                                    Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.icon,
                            fontSize = if (isSelected) 22.sp else 18.sp
                        )
                    }

                    Text(
                        text = if (isEnglish) tab.labelEn else tab.labelHi,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) CandyOrange else (if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C))
                    )
                }
            }
        }
    }
}

