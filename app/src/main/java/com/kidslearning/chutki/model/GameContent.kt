package com.kidslearning.chutki.model

import com.google.gson.annotations.SerializedName

/**
 * Model representing a bilingual learning/game item loaded from JSON or remote network sync.
 */
data class GameContent(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name_en")
    val name_en: String = "",

    @SerializedName("name_hi")
    val name_hi: String = "",

    @SerializedName("audio_text_en")
    val audio_text_en: String = "",

    @SerializedName("audio_text_hi")
    val audio_text_hi: String = "",

    @SerializedName("level")
    val level: Int = 1
) {
    // Convenient camelCase property aliases
    val nameEn: String get() = name_en
    val nameHi: String get() = name_hi
    val audioTextEn: String get() = audio_text_en
    val audioTextHi: String get() = audio_text_hi
}

