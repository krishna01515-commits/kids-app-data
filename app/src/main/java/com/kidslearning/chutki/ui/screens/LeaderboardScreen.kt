package com.kidslearning.chutki.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslearning.chutki.data.LearningContentRepository
import com.kidslearning.chutki.model.UserProfile
import com.kidslearning.chutki.theme.CandyOrange
import com.kidslearning.chutki.theme.CandyYellow
import com.kidslearning.chutki.theme.GoldenStar
import com.kidslearning.chutki.theme.StreakFire
import com.kidslearning.chutki.ui.components.ClayCard
import com.kidslearning.chutki.ui.components.KidAvatarView

@Composable
fun LeaderboardScreen(
    userProfile: UserProfile,
    isDarkMode: Boolean
) {
    val leaderboardUsers = remember(userProfile.totalXp, userProfile.name, userProfile.avatarEmoji, userProfile.currentStreakDays) {
        LearningContentRepository.getLeaderboard(userProfile)
    }
    val badges = remember(userProfile.totalXp, userProfile.currentRank, userProfile.currentStreakDays, userProfile.cardsLearnedCount, userProfile.lettersTracedCount) {
        LearningContentRepository.getBadges(userProfile)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF13142B), Color(0xFF1E2142)))
                else Brush.verticalGradient(listOf(Color(0xFFFFFDF5), Color(0xFFFFF3E0)))
            )
            .padding(18.dp)
    ) {
        // 1. Header & User Rank Highlight Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (userProfile.isEnglish) "🏆 Little Champions Leaderboard" else "🏆 नन्हे सितारों का लीडरबोर्ड",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF263238)
                )
                Text(
                    text = if (userProfile.isEnglish) "Play with friends and rise together!" else "दोस्तों के साथ खेलो और आगे बढ़ो!",
                    fontSize = 13.sp,
                    color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Current Student Real-Time Standing Card
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 6.dp,
                    bgGradient = Brush.horizontalGradient(
                        listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
                    ),
                    borderHighlightColor = CandyOrange
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KidAvatarView(avatarKey = userProfile.avatarEmoji, size = 48.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${userProfile.name} ${if (userProfile.isEnglish) "(You)" else "(आप)"}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF3E2723)
                                )
                                Text(
                                    text = if (userProfile.isEnglish) "🔥 ${userProfile.currentStreakDays} day streak • ${userProfile.unlockedBadgesCount}/8 badges" else "🔥 ${userProfile.currentStreakDays} दिन स्ट्रिक • ${userProfile.unlockedBadgesCount}/8 बैज",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (userProfile.isEnglish) "Rank #${userProfile.currentRank}" else "रैंक #${userProfile.currentRank}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color(0xFFBF360C)
                            )
                            Text(
                                text = "${userProfile.totalXp} XP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Top 3 Podium
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (leaderboardUsers.size > 1) {
                    val user2 = leaderboardUsers[1]
                    PodiumColumn(
                        rank = 2,
                        name = user2.name,
                        avatar = user2.avatarEmoji,
                        xp = user2.xp,
                        medal = "🥈",
                        pedestalHeight = 110.dp,
                        pedestalColor = Color(0xFFCFD8DC),
                        isDarkMode = isDarkMode
                    )
                }

                // 1st Place (Gold)
                if (leaderboardUsers.isNotEmpty()) {
                    val user1 = leaderboardUsers[0]
                    PodiumColumn(
                        rank = 1,
                        name = user1.name,
                        avatar = user1.avatarEmoji,
                        xp = user1.xp,
                        medal = "🥇",
                        pedestalHeight = 140.dp,
                        pedestalColor = Color(0xFFFFD54F),
                        isDarkMode = isDarkMode
                    )
                }

                // 3rd Place (Bronze)
                if (leaderboardUsers.size > 2) {
                    val user3 = leaderboardUsers[2]
                    PodiumColumn(
                        rank = 3,
                        name = user3.name,
                        avatar = user3.avatarEmoji,
                        xp = user3.xp,
                        medal = "🥉",
                        pedestalHeight = 90.dp,
                        pedestalColor = Color(0xFFFFAB91),
                        isDarkMode = isDarkMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. Current User Rank Banner
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 6.dp,
                bgGradient = Brush.horizontalGradient(
                    listOf(Color(0xFFFFECB3), Color(0xFFFFD54F))
                ),
                borderHighlightColor = CandyOrange
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#4",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color(0xFFBF360C)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        KidAvatarView(avatarKey = userProfile.avatarEmoji, size = 42.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${userProfile.name} (आप)",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF3E2723)
                            )
                            Text(
                                text = "तीसरे स्थान से केवल 130 XP दूर!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD84315)
                            )
                        }
                    }

                    Text(
                        text = "${userProfile.totalXp} XP",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. Full Leaderboard List
        item {
            Text(
                text = if (userProfile.isEnglish) "All Bright Learners:" else "सभी होशियार साथी:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isDarkMode) Color.White else Color(0xFF37474F)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(leaderboardUsers) { user ->
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                cornerRadius = 18.dp,
                elevation = 3.dp,
                bgGradient = if (user.isCurrentUser) {
                    Brush.horizontalGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)))
                } else {
                    if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF1E2142), Color(0xFF262A56)))
                    else Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9E6)))
                },
                borderHighlightColor = if (user.isCurrentUser) CandyOrange else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${user.rank}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C),
                            modifier = Modifier.width(32.dp)
                        )
                        KidAvatarView(avatarKey = user.avatarEmoji, size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF263238)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (userProfile.isEnglish) "🔥 ${user.streakDays} days" else "🔥 ${user.streakDays} दिन", fontSize = 11.sp, color = StreakFire)
                            }
                        }
                    }

                    Text(
                        text = "${user.xp} XP",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = CandyOrange
                    )
                }
            }
        }

        // 5. Unlocked Badges Showcase
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (userProfile.isEnglish) "🎖️ Badges & Achievements:" else "🎖️ आपके बैज और उपलब्धियां:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isDarkMode) Color.White else Color(0xFF37474F)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(badges) { badge ->
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 18.dp,
                elevation = 3.dp,
                bgGradient = if (badge.isUnlocked) {
                    if (isDarkMode) Brush.verticalGradient(listOf(Color(0xFF262A56), Color(0xFF1E2142)))
                    else Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F8E9)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)))
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.titleHindi,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (badge.isUnlocked) {
                                if (isDarkMode) Color.White else Color(0xFF263238)
                            } else {
                                Color.Gray
                            }
                        )
                        Text(
                            text = badge.descriptionHindi,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFFA5B0E0) else Color(0xFF78909C)
                        )
                    }

                    if (badge.isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "अनलॉक ✨", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PodiumColumn(
    rank: Int,
    name: String,
    avatar: String,
    xp: Int,
    medal: String,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    pedestalColor: Color,
    isDarkMode: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Text(text = medal, fontSize = 24.sp)
        Text(text = avatar, fontSize = 34.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = name.split(" ").first(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color(0xFF37474F)
        )
        Text(
            text = "$xp XP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = CandyOrange
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal Box
        Box(
            modifier = Modifier
                .width(88.dp)
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(pedestalColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

