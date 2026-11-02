package com.kidslearning.chutki.data

object AppLocalization {
    fun t(isEnglish: Boolean, hindi: String, english: String): String {
        return if (isEnglish) english else hindi
    }
}

