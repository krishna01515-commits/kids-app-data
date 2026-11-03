package com.kidslearning.chutki.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class HindiTtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized: Boolean = false
    private val hindiLocale = Locale.forLanguageTag("hi-IN")
    private val englishLocale = Locale.US
    private var currentLanguage: String = "hi"
    private var pendingSpeech: String? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            applyLanguage(currentLanguage)
            tts?.setPitch(1.15f) // Cheerful, youthful teacher tone
            tts?.setSpeechRate(0.85f) // Slower, crystal clear for kids
            isInitialized = true

            pendingSpeech?.let {
                speak(it)
                pendingSpeech = null
            }
        } else {
            Log.e("HindiTtsManager", "Initialization of TextToSpeech failed with status: $status")
        }
    }

    fun setAppLanguage(lang: String) {
        currentLanguage = lang
        applyLanguage(lang)
    }

    private fun applyLanguage(lang: String) {
        val targetLocale = if (lang.equals("en", ignoreCase = true)) englishLocale else hindiLocale
        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("HindiTtsManager", "Target locale $targetLocale not fully supported, falling back to default")
            tts?.setLanguage(Locale.getDefault())
        }
    }

    fun speakLanguageChanged(lang: String) {
        setAppLanguage(lang)
        if (lang.equals("en", ignoreCase = true)) {
            speak("Hello! Language changed to English! Let's play and learn together!")
        } else {
            speak("नमस्ते! भाषा हिंदी में बदल दी गई है! चलो मिलकर सीखते हैं!")
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!isInitialized) {
            pendingSpeech = text
            return
        }
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "UTTERANCE_KIDS_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun welcomeKidsGreeting(kidName: String, isEnglish: Boolean = false) {
        if (isEnglish) {
            speak("Hello friends! I am your teacher Priya! $kidName, let's learn and play together!")
        } else {
            speak("नमस्ते प्यारे बच्चों! मैं हूँ आपकी टीचर प्रिया दीदी! $kidName, चलो आज खेल खेल में मजेदार पढ़ाई करते हैं!")
        }
    }

    fun speakWelcomeBackGreeting(kidName: String, isEnglish: Boolean = false) {
        if (isEnglish) {
            speak("Hello $kidName, welcome back! Let's learn something new today!")
        } else {
            speak("हेलो $kidName, वेलकम बैक! चलो आज कुछ नया सीखते हैं!")
        }
    }

    fun celebrateCorrect(isEnglish: Boolean = false) {
        if (isEnglish) {
            val phrases = listOf(
                "Great job! That's correct!",
                "Super smart! You did it!",
                "Awesome! Shiny stars for you!",
                "Well done little superstar! Let's play more!"
            )
            speak(phrases.random())
        } else {
            val phrases = listOf(
                "अरे वाह! शाबाश! बिल्कुल सही जवाब!",
                "बहुत खूब! आप तो बहुत होशियार हैं!",
                "कमाल कर दिया! आपको मिले चमकते हुए सितारे!",
                "शाबाश प्यारे बच्चे! अगला सवाल देखते हैं!"
            )
            speak(phrases.random())
        }
    }

    fun encourageRetry(isEnglish: Boolean = false) {
        if (isEnglish) {
            val phrases = listOf(
                "Good try! Try one more time!",
                "Look carefully and choose again! You can do it!",
                "Almost there! Let's try once more!"
            )
            speak(phrases.random())
        } else {
            val phrases = listOf(
                "कोई बात नहीं! एक बार फिर से कोशिश करो!",
                "ध्यान से देखो और फिर चुनो! आप कर सकते हो!",
                "ओह, यह थोड़ा अलग था! फिर से प्रयास करो!"
            )
            speak(phrases.random())
        }
    }

    fun celebrateTracingComplete(isEnglish: Boolean = false) {
        if (isEnglish) {
            speak("Wonderful! You wrote it so beautifully! You earned three golden stars!")
        } else {
            speak("शाबाश! आपने बहुत ही सुंदर लिखा है! पूरे तीन सितारे मिले!")
        }
    }

    fun celebrateChampionshipWin(kidName: String, subjectName: String, isEnglish: Boolean = false) {
        val message = if (isEnglish) {
            "Heartiest congratulations to our Super Kid $kidName! You have completed all 3 rounds, conquered the Grand Championship, and earned the Golden Master Trophy in $subjectName! You are a brilliant superstar!"
        } else {
            "अरे वाह! बहुत-बहुत बधाई हमारे नन्हे उस्ताद $kidName! आपने तीनों राउंड पार करके $subjectName का महा मुकाबला जीत लिया है और आपको मिलती है सुनहरी मास्टर ट्रॉफी! आप सचमुच जीनियस हैं!"
        }
        speak(message)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
