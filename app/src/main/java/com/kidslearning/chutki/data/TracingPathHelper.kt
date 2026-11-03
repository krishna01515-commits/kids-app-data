package com.kidslearning.chutki.data

/**
 * TracingPathHelper:
 * Provides pedagogical stroke paths and checkpoints for Hindi, English, and Math characters.
 * Each stroke represents a continuous pen/chalk movement.
 * Guide dots sit precisely at the waypoints along each stroke path so that they are
 * ALWAYS directly in the middle of the tracing line!
 */
object TracingPathHelper {

    data class LetterTracingData(
        val hintHindi: String,
        val strokePaths: List<List<Pair<Float, Float>>>,
        val guideDots: List<Pair<Float, Float>>
    )

    fun getLetterTracingData(char: String, subjectId: String = ""): LetterTracingData {
        val trimmed = char.trim()

        // 1. Check if it's a multi-digit number (e.g., 10 to 100)
        if (trimmed.all { it.isDigit() } && trimmed.length > 1) {
            return getMultiDigitNumberData(trimmed)
        }

        // 2. Check if single digit (0-9)
        if (trimmed.length == 1 && trimmed[0].isDigit()) {
            return getSingleDigitData(trimmed[0])
        }

        // 3. Check English capital or small
        if (trimmed.length == 1 && trimmed[0] in 'A'..'Z') {
            return getEnglishCapitalData(trimmed)
        }
        if (trimmed.length == 1 && trimmed[0] in 'a'..'z') {
            return getEnglishSmallData(trimmed)
        }

        // 4. Check Hindi Vowels & Consonants
        val hindiData = getHindiData(trimmed)
        if (hindiData != null) return hindiData

        // 5. Fallback for any other glyph: clean symmetric oval
        val defaultStrokes = listOf(
            listOf(
                Pair(0.50f, 0.20f),
                Pair(0.32f, 0.35f),
                Pair(0.32f, 0.65f),
                Pair(0.50f, 0.78f),
                Pair(0.68f, 0.65f),
                Pair(0.68f, 0.35f),
                Pair(0.50f, 0.20f)
            )
        )
        return LetterTracingData(
            hintHindi = "डॉट्स को मिलाते हुए सुंदर आकृति बनाएं",
            strokePaths = defaultStrokes,
            guideDots = defaultStrokes.flatten()
        )
    }

    // =========================================================================
    // Multi-Digit Number Generation (10 to 100)
    // =========================================================================
    private fun getMultiDigitNumberData(numberStr: String): LetterTracingData {
        val count = numberStr.length
        val combinedStrokes = mutableListOf<List<Pair<Float, Float>>>()

        when (count) {
            2 -> {
                // Two digits side-by-side
                val digitWidth = 0.40f
                val gap = 0.08f
                val leftOffsets = listOf(gap, gap + digitWidth + 0.04f)

                numberStr.forEachIndexed { idx, digitChar ->
                    val singleData = getSingleDigitData(digitChar)
                    val offsetX = leftOffsets[idx]
                    val scaledStrokes = singleData.strokePaths.map { stroke ->
                        stroke.map { (x, y) ->
                            Pair(offsetX + (x - 0.25f) * (digitWidth / 0.50f), y)
                        }
                    }
                    combinedStrokes.addAll(scaledStrokes)
                }
            }
            3 -> {
                // Three digits (e.g. 100)
                val digitWidth = 0.28f
                val gap = 0.04f
                val leftOffsets = listOf(gap, gap + digitWidth + 0.04f, gap + (digitWidth + 0.04f) * 2)

                numberStr.forEachIndexed { idx, digitChar ->
                    val singleData = getSingleDigitData(digitChar)
                    val offsetX = leftOffsets[idx]
                    val scaledStrokes = singleData.strokePaths.map { stroke ->
                        stroke.map { (x, y) ->
                            Pair(offsetX + (x - 0.25f) * (digitWidth / 0.50f), y)
                        }
                    }
                    combinedStrokes.addAll(scaledStrokes)
                }
            }
            else -> {
                val singleData = getSingleDigitData(numberStr.first())
                combinedStrokes.addAll(singleData.strokePaths)
            }
        }

        val allDots = combinedStrokes.flatten()
        return LetterTracingData(
            hintHindi = "संख्या '$numberStr' के डॉट्स को क्रम से जोड़ें",
            strokePaths = combinedStrokes,
            guideDots = allDots
        )
    }

    // =========================================================================
    // Single Digits (0 to 9)
    // =========================================================================
    private fun getSingleDigitData(digit: Char): LetterTracingData {
        val strokes: List<List<Pair<Float, Float>>>
        val hint: String

        when (digit) {
            '0' -> {
                hint = "ऊपर से गोल घुमाकर सुंदर शून्य '0' पूरा करें"
                strokes = listOf(
                    listOf(
                        Pair(0.50f, 0.20f),
                        Pair(0.34f, 0.35f),
                        Pair(0.34f, 0.65f),
                        Pair(0.50f, 0.78f),
                        Pair(0.66f, 0.65f),
                        Pair(0.66f, 0.35f),
                        Pair(0.50f, 0.20f)
                    )
                )
            }
            '1' -> {
                hint = "ऊपर से नीचे तक एक सीधी खड़ी रेखा खींचें"
                strokes = listOf(
                    listOf(Pair(0.40f, 0.30f), Pair(0.50f, 0.20f), Pair(0.50f, 0.78f)),
                    listOf(Pair(0.34f, 0.78f), Pair(0.66f, 0.78f))
                )
            }
            '2' -> {
                hint = "ऊपर गोल घुमाएं, तिरछा नीचे लाएं और सीधी लाइन खींचें"
                strokes = listOf(
                    listOf(
                        Pair(0.36f, 0.30f),
                        Pair(0.50f, 0.20f),
                        Pair(0.64f, 0.30f),
                        Pair(0.40f, 0.78f),
                        Pair(0.68f, 0.78f)
                    )
                )
            }
            '3' -> {
                hint = "पहला आधा घेरा बनाएं, फिर दूसरा आधा घेरा जोड़ें"
                strokes = listOf(
                    listOf(
                        Pair(0.36f, 0.26f),
                        Pair(0.52f, 0.20f),
                        Pair(0.64f, 0.32f),
                        Pair(0.48f, 0.46f),
                        Pair(0.66f, 0.58f),
                        Pair(0.52f, 0.78f),
                        Pair(0.36f, 0.74f)
                    )
                )
            }
            '4' -> {
                hint = "तिरछी लाइन, आड़ी लाइन और फिर सीधी खड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.58f, 0.20f), Pair(0.34f, 0.56f), Pair(0.70f, 0.56f)),
                    listOf(Pair(0.58f, 0.20f), Pair(0.58f, 0.78f))
                )
            }
            '5' -> {
                hint = "ऊपर आड़ी लाइन, छोटी खड़ी लाइन और नीचे गोल पेट बनाएं"
                strokes = listOf(
                    listOf(
                        Pair(0.66f, 0.22f),
                        Pair(0.40f, 0.22f),
                        Pair(0.40f, 0.46f),
                        Pair(0.64f, 0.54f),
                        Pair(0.64f, 0.68f),
                        Pair(0.40f, 0.78f)
                    )
                )
            }
            '6' -> {
                hint = "ऊपर से घुमाते हुए नीचे लाएं और पूरा गोल घेरा बंद करें"
                strokes = listOf(
                    listOf(
                        Pair(0.62f, 0.24f),
                        Pair(0.40f, 0.48f),
                        Pair(0.40f, 0.70f),
                        Pair(0.52f, 0.78f),
                        Pair(0.64f, 0.70f),
                        Pair(0.62f, 0.54f),
                        Pair(0.40f, 0.54f)
                    )
                )
            }
            '7' -> {
                hint = "ऊपर आड़ी लाइन और नीचे दाईं ओर तिरछी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.35f, 0.22f), Pair(0.68f, 0.22f), Pair(0.46f, 0.78f))
                )
            }
            '8' -> {
                hint = "ऊपर छोटा गोल और नीचे दूसरा गोल घेरा बनाकर मिलाओ"
                strokes = listOf(
                    listOf(
                        Pair(0.50f, 0.20f),
                        Pair(0.36f, 0.32f),
                        Pair(0.64f, 0.64f),
                        Pair(0.50f, 0.78f),
                        Pair(0.36f, 0.64f),
                        Pair(0.64f, 0.32f),
                        Pair(0.50f, 0.20f)
                    )
                )
            }
            '9' -> {
                hint = "ऊपर पूरा गोल घेरा बनाएं और नीचे सीधी लाइन खींचें"
                strokes = listOf(
                    listOf(
                        Pair(0.50f, 0.48f),
                        Pair(0.36f, 0.36f),
                        Pair(0.50f, 0.22f),
                        Pair(0.64f, 0.36f),
                        Pair(0.50f, 0.48f)
                    ),
                    listOf(Pair(0.64f, 0.36f), Pair(0.64f, 0.78f))
                )
            }
            else -> {
                hint = "संख्या के डॉट्स को मिलाओ"
                strokes = listOf(
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.78f))
                )
            }
        }

        return LetterTracingData(
            hintHindi = hint,
            strokePaths = strokes,
            guideDots = strokes.flatten()
        )
    }

    // =========================================================================
    // English Capital Letters (A to Z)
    // =========================================================================
    private fun getEnglishCapitalData(char: String): LetterTracingData {
        val strokes: List<List<Pair<Float, Float>>>
        val hint: String

        when (char) {
            "A" -> {
                hint = "बाईं तिरछी रेखा, दाईं तिरछी रेखा और बीच में जोड़ने वाली रेखा"
                strokes = listOf(
                    listOf(Pair(0.50f, 0.18f), Pair(0.28f, 0.80f)),
                    listOf(Pair(0.50f, 0.18f), Pair(0.72f, 0.80f)),
                    listOf(Pair(0.36f, 0.54f), Pair(0.64f, 0.54f))
                )
            }
            "B" -> {
                hint = "सीधी खड़ी लाइन और दाईं ओर दो गोल पेट बनाएं"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.18f), Pair(0.32f, 0.80f)),
                    listOf(
                        Pair(0.32f, 0.18f),
                        Pair(0.58f, 0.18f),
                        Pair(0.66f, 0.33f),
                        Pair(0.58f, 0.48f),
                        Pair(0.32f, 0.48f)
                    ),
                    listOf(
                        Pair(0.32f, 0.48f),
                        Pair(0.62f, 0.48f),
                        Pair(0.70f, 0.64f),
                        Pair(0.62f, 0.80f),
                        Pair(0.32f, 0.80f)
                    )
                )
            }
            "C" -> {
                hint = "ऊपर से आधा गोल घुमाते हुए नीचे लाएं"
                strokes = listOf(
                    listOf(
                        Pair(0.70f, 0.25f),
                        Pair(0.50f, 0.18f),
                        Pair(0.32f, 0.35f),
                        Pair(0.30f, 0.50f),
                        Pair(0.32f, 0.65f),
                        Pair(0.50f, 0.80f),
                        Pair(0.70f, 0.75f)
                    )
                )
            }
            "D" -> {
                hint = "सीधी खड़ी लाइन और ऊपर से नीचे तक बड़ा गोल पेट"
                strokes = listOf(
                    listOf(Pair(0.34f, 0.18f), Pair(0.34f, 0.80f)),
                    listOf(
                        Pair(0.34f, 0.18f),
                        Pair(0.55f, 0.18f),
                        Pair(0.72f, 0.48f),
                        Pair(0.55f, 0.80f),
                        Pair(0.34f, 0.80f)
                    )
                )
            }
            "E" -> {
                hint = "खड़ी लाइन, फिर ऊपर, बीच और नीचे तीन आड़ी लाइनें"
                strokes = listOf(
                    listOf(Pair(0.34f, 0.18f), Pair(0.34f, 0.80f)),
                    listOf(Pair(0.34f, 0.18f), Pair(0.70f, 0.18f)),
                    listOf(Pair(0.34f, 0.48f), Pair(0.62f, 0.48f)),
                    listOf(Pair(0.34f, 0.80f), Pair(0.70f, 0.80f))
                )
            }
            "F" -> {
                hint = "खड़ी लाइन और ऊपर तथा बीच में दो आड़ी लाइनें खींचें"
                strokes = listOf(
                    listOf(Pair(0.34f, 0.18f), Pair(0.34f, 0.80f)),
                    listOf(Pair(0.34f, 0.18f), Pair(0.70f, 0.18f)),
                    listOf(Pair(0.34f, 0.48f), Pair(0.62f, 0.48f))
                )
            }
            "G" -> {
                hint = "'C' बनाकर अंदर मोड़ें और छोटी आड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(
                        Pair(0.70f, 0.25f),
                        Pair(0.50f, 0.18f),
                        Pair(0.32f, 0.35f),
                        Pair(0.30f, 0.50f),
                        Pair(0.32f, 0.65f),
                        Pair(0.50f, 0.80f),
                        Pair(0.70f, 0.65f),
                        Pair(0.70f, 0.52f),
                        Pair(0.52f, 0.52f)
                    )
                )
            }
            "H" -> {
                hint = "दो सीधी खड़ी लाइनें और बीच में जोड़ने वाली लाइन"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.18f), Pair(0.32f, 0.80f)),
                    listOf(Pair(0.68f, 0.18f), Pair(0.68f, 0.80f)),
                    listOf(Pair(0.32f, 0.48f), Pair(0.68f, 0.48f))
                )
            }
            "I" -> {
                hint = "सीधी खड़ी लाइन, ऊपर और नीचे छोटी आड़ी लाइन"
                strokes = listOf(
                    listOf(Pair(0.50f, 0.18f), Pair(0.50f, 0.80f)),
                    listOf(Pair(0.36f, 0.18f), Pair(0.64f, 0.18f)),
                    listOf(Pair(0.36f, 0.80f), Pair(0.64f, 0.80f))
                )
            }
            "J" -> {
                hint = "खड़ी लाइन खींचकर नीचे से बाईं ओर गोल मोड़ें"
                strokes = listOf(
                    listOf(
                        Pair(0.60f, 0.18f),
                        Pair(0.60f, 0.64f),
                        Pair(0.52f, 0.80f),
                        Pair(0.36f, 0.70f)
                    ),
                    listOf(Pair(0.44f, 0.18f), Pair(0.76f, 0.18f))
                )
            }
            "K" -> {
                hint = "खड़ी लाइन और बीच से दो तिरछी शाखाएं बनाएं"
                strokes = listOf(
                    listOf(Pair(0.34f, 0.18f), Pair(0.34f, 0.80f)),
                    listOf(Pair(0.68f, 0.20f), Pair(0.34f, 0.48f)),
                    listOf(Pair(0.34f, 0.48f), Pair(0.68f, 0.80f))
                )
            }
            "L" -> {
                hint = "सीधी खड़ी लाइन और नीचे दाईं ओर आड़ी लाइन"
                strokes = listOf(
                    listOf(Pair(0.36f, 0.18f), Pair(0.36f, 0.80f), Pair(0.70f, 0.80f))
                )
            }
            "M" -> {
                hint = "दो खड़ी लाइनें और बीच में नीचे तक तिरछी लाइनें जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.26f, 0.80f), Pair(0.26f, 0.18f)),
                    listOf(Pair(0.26f, 0.18f), Pair(0.50f, 0.55f)),
                    listOf(Pair(0.50f, 0.55f), Pair(0.74f, 0.18f)),
                    listOf(Pair(0.74f, 0.18f), Pair(0.74f, 0.80f))
                )
            }
            "N" -> {
                hint = "दो खड़ी लाइनें और ऊपर से नीचे तिरछी लाइन जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.80f), Pair(0.32f, 0.18f)),
                    listOf(Pair(0.32f, 0.18f), Pair(0.68f, 0.80f)),
                    listOf(Pair(0.68f, 0.80f), Pair(0.68f, 0.18f))
                )
            }
            "O" -> {
                hint = "पूरा गोल घेरा अंडाकार घुमाकर बंद करें"
                strokes = listOf(
                    listOf(
                        Pair(0.50f, 0.18f),
                        Pair(0.30f, 0.35f),
                        Pair(0.30f, 0.65f),
                        Pair(0.50f, 0.80f),
                        Pair(0.70f, 0.65f),
                        Pair(0.70f, 0.35f),
                        Pair(0.50f, 0.18f)
                    )
                )
            }
            "P" -> {
                hint = "खड़ी लाइन और ऊपर एक छोटा गोल पेट बनाएं"
                strokes = listOf(
                    listOf(Pair(0.34f, 0.18f), Pair(0.34f, 0.80f)),
                    listOf(
                        Pair(0.34f, 0.18f),
                        Pair(0.60f, 0.18f),
                        Pair(0.68f, 0.34f),
                        Pair(0.60f, 0.50f),
                        Pair(0.34f, 0.50f)
                    )
                )
            }
            "Q" -> {
                hint = "'O' बनाकर नीचे दाईं ओर छोटी तिरछी पूंछ निकालें"
                strokes = listOf(
                    listOf(
                        Pair(0.50f, 0.18f),
                        Pair(0.30f, 0.35f),
                        Pair(0.30f, 0.65f),
                        Pair(0.50f, 0.80f),
                        Pair(0.70f, 0.65f),
                        Pair(0.70f, 0.35f),
                        Pair(0.50f, 0.18f)
                    ),
                    listOf(Pair(0.55f, 0.64f), Pair(0.74f, 0.84f))
                )
            }
            "R" -> {
                hint = "'P' बनाकर नीचे दाईं ओर तिरछी टांग खींचें"
                strokes = listOf(
                    listOf(Pair(0.34f, 0.18f), Pair(0.34f, 0.80f)),
                    listOf(
                        Pair(0.34f, 0.18f),
                        Pair(0.60f, 0.18f),
                        Pair(0.68f, 0.34f),
                        Pair(0.60f, 0.50f),
                        Pair(0.34f, 0.50f)
                    ),
                    listOf(Pair(0.52f, 0.50f), Pair(0.70f, 0.80f))
                )
            }
            "S" -> {
                hint = "ऊपर से आधा गोल बाईं ओर, फिर नीचे दाईं ओर मोड़ें"
                strokes = listOf(
                    listOf(
                        Pair(0.68f, 0.28f),
                        Pair(0.50f, 0.18f),
                        Pair(0.34f, 0.32f),
                        Pair(0.50f, 0.48f),
                        Pair(0.66f, 0.62f),
                        Pair(0.50f, 0.80f),
                        Pair(0.32f, 0.70f)
                    )
                )
            }
            "T" -> {
                hint = "ऊपर बड़ी आड़ी छत और बीच में सीधी खड़ी लाइन"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.18f), Pair(0.72f, 0.18f)),
                    listOf(Pair(0.50f, 0.18f), Pair(0.50f, 0.80f))
                )
            }
            "U" -> {
                hint = "ऊपर से नीचे कटोरे जैसा गोल घुमाकर ऊपर ले जाएं"
                strokes = listOf(
                    listOf(
                        Pair(0.32f, 0.18f),
                        Pair(0.32f, 0.62f),
                        Pair(0.50f, 0.80f),
                        Pair(0.68f, 0.62f),
                        Pair(0.68f, 0.18f)
                    )
                )
            }
            "V" -> {
                hint = "बाईं तिरछी लाइन और दाईं तिरछी लाइन नीचे मिलाएं"
                strokes = listOf(
                    listOf(Pair(0.30f, 0.18f), Pair(0.50f, 0.80f)),
                    listOf(Pair(0.50f, 0.80f), Pair(0.70f, 0.18f))
                )
            }
            "W" -> {
                hint = "दो 'V' को मिलाकर सुंदर आकृति बनाएं"
                strokes = listOf(
                    listOf(Pair(0.24f, 0.18f), Pair(0.36f, 0.80f)),
                    listOf(Pair(0.36f, 0.80f), Pair(0.50f, 0.40f)),
                    listOf(Pair(0.50f, 0.40f), Pair(0.64f, 0.80f)),
                    listOf(Pair(0.64f, 0.80f), Pair(0.76f, 0.18f))
                )
            }
            "X" -> {
                hint = "दो तिरछी लाइनें एक-दूसरे को काटती हुई खींचें"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.18f), Pair(0.68f, 0.80f)),
                    listOf(Pair(0.68f, 0.18f), Pair(0.32f, 0.80f))
                )
            }
            "Y" -> {
                hint = "ऊपर छोटा 'V' और नीचे सीधी खड़ी लाइन जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.18f), Pair(0.50f, 0.48f)),
                    listOf(Pair(0.68f, 0.18f), Pair(0.50f, 0.48f)),
                    listOf(Pair(0.50f, 0.48f), Pair(0.50f, 0.80f))
                )
            }
            "Z" -> {
                hint = "ऊपर आड़ी लाइन, तिरछी लाइन और नीचे आड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(
                        Pair(0.32f, 0.18f),
                        Pair(0.68f, 0.18f),
                        Pair(0.32f, 0.80f),
                        Pair(0.68f, 0.80f)
                    )
                )
            }
            else -> {
                hint = "अक्षर के डॉट्स को मिलाओ"
                strokes = listOf(
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.80f))
                )
            }
        }

        return LetterTracingData(
            hintHindi = hint,
            strokePaths = strokes,
            guideDots = strokes.flatten()
        )
    }

    // =========================================================================
    // English Small Letters (a to z)
    // =========================================================================
    private fun getEnglishSmallData(char: String): LetterTracingData {
        val upperChar = char.uppercase()
        val upperData = getEnglishCapitalData(upperChar)
        // Scaled into midline range (0.35f to 0.78f)
        val scaledStrokes = upperData.strokePaths.map { stroke ->
            stroke.map { (x, y) ->
                Pair(x, 0.25f + y * 0.65f)
            }
        }
        return LetterTracingData(
            hintHindi = "छोटा '$char' डॉट्स को मिलाते हुए लिखें",
            strokePaths = scaledStrokes,
            guideDots = scaledStrokes.flatten()
        )
    }

    // =========================================================================
    // Hindi Vowels & Consonants (अ to ज्ञ)
    // =========================================================================
    private fun getHindiData(char: String): LetterTracingData? {
        val strokes: List<List<Pair<Float, Float>>>
        val hint: String

        when (char) {
            // ---- स्वर (Vowels) ----
            "अ" -> {
                hint = "ऊपर से दो गोल घुमाव, बीच से लाइन, खड़ी लाइन और ऊपर छत बनाएं"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.28f), Pair(0.48f, 0.24f), Pair(0.55f, 0.34f), Pair(0.46f, 0.45f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.58f, 0.55f), Pair(0.48f, 0.68f), Pair(0.32f, 0.68f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.70f, 0.45f)),
                    listOf(Pair(0.70f, 0.20f), Pair(0.70f, 0.78f)),
                    listOf(Pair(0.28f, 0.20f), Pair(0.76f, 0.20f))
                )
            }
            "आ" -> {
                hint = "'अ' बनाकर पास में एक और सीधी खड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.30f, 0.28f), Pair(0.44f, 0.24f), Pair(0.50f, 0.34f), Pair(0.42f, 0.45f)),
                    listOf(Pair(0.42f, 0.45f), Pair(0.52f, 0.55f), Pair(0.44f, 0.68f), Pair(0.30f, 0.68f)),
                    listOf(Pair(0.42f, 0.45f), Pair(0.60f, 0.45f)),
                    listOf(Pair(0.60f, 0.20f), Pair(0.60f, 0.78f)),
                    listOf(Pair(0.76f, 0.20f), Pair(0.76f, 0.78f)),
                    listOf(Pair(0.26f, 0.20f), Pair(0.82f, 0.20f))
                )
            }
            "इ" -> {
                hint = "छोटी खड़ी लाइन, 'S' जैसा मोड़ें और नीचे सुंदर पूंछ निकालें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(
                        Pair(0.50f, 0.20f),
                        Pair(0.50f, 0.30f),
                        Pair(0.36f, 0.36f),
                        Pair(0.64f, 0.50f),
                        Pair(0.40f, 0.66f),
                        Pair(0.30f, 0.78f)
                    )
                )
            }
            "ई" -> {
                hint = "'इ' बनाकर ऊपर की तरफ घुमावदार मात्रा लगाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(
                        Pair(0.50f, 0.20f),
                        Pair(0.50f, 0.30f),
                        Pair(0.36f, 0.36f),
                        Pair(0.64f, 0.50f),
                        Pair(0.40f, 0.66f),
                        Pair(0.30f, 0.78f)
                    ),
                    listOf(Pair(0.50f, 0.20f), Pair(0.66f, 0.12f))
                )
            }
            "उ" -> {
                hint = "ऊपर आधा गोल और नीचे आधा गोल बनाकर छत लगाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.34f, 0.28f), Pair(0.54f, 0.26f), Pair(0.45f, 0.45f)),
                    listOf(Pair(0.45f, 0.45f), Pair(0.62f, 0.58f), Pair(0.34f, 0.70f))
                )
            }
            "ऊ" -> {
                hint = "'उ' बनाकर बीच से पीछे की तरफ मुड़ी हुई पूंछ निकालें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.34f, 0.28f), Pair(0.54f, 0.26f), Pair(0.45f, 0.45f)),
                    listOf(Pair(0.45f, 0.45f), Pair(0.62f, 0.58f), Pair(0.34f, 0.70f)),
                    listOf(Pair(0.45f, 0.45f), Pair(0.68f, 0.52f))
                )
            }
            "ऋ" -> {
                hint = "खड़ी लाइन, बाईं ओर दो शाखाएं और दाईं ओर घुमाव बनाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.46f, 0.20f), Pair(0.46f, 0.78f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.26f, 0.35f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.26f, 0.62f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.66f, 0.45f), Pair(0.60f, 0.72f))
                )
            }
            "ए" -> {
                hint = "खड़ी लाइन अंदर मोड़ें और पास में तिरछी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.40f, 0.20f), Pair(0.40f, 0.46f), Pair(0.56f, 0.52f)),
                    listOf(Pair(0.62f, 0.20f), Pair(0.40f, 0.75f))
                )
            }
            "ऐ" -> {
                hint = "'ए' बनाकर ऊपर एक तिरछी मात्रा लगाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.40f, 0.20f), Pair(0.40f, 0.46f), Pair(0.56f, 0.52f)),
                    listOf(Pair(0.62f, 0.20f), Pair(0.40f, 0.75f)),
                    listOf(Pair(0.40f, 0.20f), Pair(0.30f, 0.10f))
                )
            }
            "ओ" -> {
                hint = "'आ' बनाकर ऊपर एक तिरछी मात्रा लगाएं"
                strokes = listOf(
                    listOf(Pair(0.30f, 0.28f), Pair(0.44f, 0.24f), Pair(0.50f, 0.34f), Pair(0.42f, 0.45f)),
                    listOf(Pair(0.42f, 0.45f), Pair(0.52f, 0.55f), Pair(0.44f, 0.68f), Pair(0.30f, 0.68f)),
                    listOf(Pair(0.42f, 0.45f), Pair(0.60f, 0.45f)),
                    listOf(Pair(0.60f, 0.20f), Pair(0.60f, 0.78f)),
                    listOf(Pair(0.76f, 0.20f), Pair(0.76f, 0.78f)),
                    listOf(Pair(0.26f, 0.20f), Pair(0.82f, 0.20f)),
                    listOf(Pair(0.76f, 0.20f), Pair(0.64f, 0.10f))
                )
            }
            "औ" -> {
                hint = "'आ' बनाकर ऊपर दो सुंदर तिरछी मात्राएं लगाएं"
                strokes = listOf(
                    listOf(Pair(0.30f, 0.28f), Pair(0.44f, 0.24f), Pair(0.50f, 0.34f), Pair(0.42f, 0.45f)),
                    listOf(Pair(0.42f, 0.45f), Pair(0.52f, 0.55f), Pair(0.44f, 0.68f), Pair(0.30f, 0.68f)),
                    listOf(Pair(0.42f, 0.45f), Pair(0.60f, 0.45f)),
                    listOf(Pair(0.60f, 0.20f), Pair(0.60f, 0.78f)),
                    listOf(Pair(0.76f, 0.20f), Pair(0.76f, 0.78f)),
                    listOf(Pair(0.26f, 0.20f), Pair(0.82f, 0.20f)),
                    listOf(Pair(0.76f, 0.20f), Pair(0.60f, 0.10f)),
                    listOf(Pair(0.76f, 0.20f), Pair(0.70f, 0.08f))
                )
            }
            "अं" -> {
                hint = "'अ' बनाकर ऊपर एक चमकती बिंदी लगाएं"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.28f), Pair(0.48f, 0.24f), Pair(0.55f, 0.34f), Pair(0.46f, 0.45f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.58f, 0.55f), Pair(0.48f, 0.68f), Pair(0.32f, 0.68f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.70f, 0.45f)),
                    listOf(Pair(0.70f, 0.20f), Pair(0.70f, 0.78f)),
                    listOf(Pair(0.28f, 0.20f), Pair(0.76f, 0.20f)),
                    listOf(Pair(0.70f, 0.12f), Pair(0.70f, 0.13f))
                )
            }
            "अः" -> {
                hint = "'अ' बनाकर आगे दो विसर्ग बिंदियां लगाएं"
                strokes = listOf(
                    listOf(Pair(0.32f, 0.28f), Pair(0.48f, 0.24f), Pair(0.55f, 0.34f), Pair(0.46f, 0.45f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.58f, 0.55f), Pair(0.48f, 0.68f), Pair(0.32f, 0.68f)),
                    listOf(Pair(0.46f, 0.45f), Pair(0.70f, 0.45f)),
                    listOf(Pair(0.70f, 0.20f), Pair(0.70f, 0.78f)),
                    listOf(Pair(0.28f, 0.20f), Pair(0.76f, 0.20f)),
                    listOf(Pair(0.82f, 0.35f), Pair(0.82f, 0.36f)),
                    listOf(Pair(0.82f, 0.55f), Pair(0.82f, 0.56f))
                )
            }

            // ---- व्यंजन (Consonants) ----
            "क" -> {
                hint = "सीधी लाइन, बाईं ओर गोल पेट, दाईं ओर पूंछ और ऊपर छत"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.78f)),
                    listOf(Pair(0.50f, 0.38f), Pair(0.30f, 0.38f), Pair(0.30f, 0.58f), Pair(0.50f, 0.58f)),
                    listOf(Pair(0.50f, 0.44f), Pair(0.70f, 0.44f), Pair(0.70f, 0.68f))
                )
            }
            "ख" -> {
                hint = "गोल घुमाकर नीचे लाएं, फिर गोल पेट और खड़ी लाइन बनाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.35f, 0.25f), Pair(0.45f, 0.25f), Pair(0.35f, 0.48f), Pair(0.45f, 0.65f), Pair(0.68f, 0.65f)),
                    listOf(Pair(0.68f, 0.20f), Pair(0.68f, 0.78f))
                )
            }
            "ग" -> {
                hint = "छोटी लाइन बनाकर अंदर मोड़ें और पास में लंबी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.38f, 0.20f), Pair(0.38f, 0.58f), Pair(0.28f, 0.58f)),
                    listOf(Pair(0.65f, 0.20f), Pair(0.65f, 0.78f))
                )
            }
            "घ" -> {
                hint = "दो बार आधा गोल घुमाएं और खड़ी लाइन से जोड़ दें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.26f), Pair(0.50f, 0.26f), Pair(0.34f, 0.44f), Pair(0.52f, 0.60f), Pair(0.66f, 0.60f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "ङ" -> {
                hint = "छोटी लाइन, 'S' जैसा मोड़ें और पास में बिंदी लगाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.32f), Pair(0.36f, 0.42f), Pair(0.64f, 0.54f), Pair(0.40f, 0.70f)),
                    listOf(Pair(0.70f, 0.54f), Pair(0.70f, 0.55f))
                )
            }
            "च" -> {
                hint = "छोटी आड़ी लाइन, नीचे गोल घुमाएं और खड़ी लाइन से मिलाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.32f, 0.42f), Pair(0.50f, 0.42f), Pair(0.38f, 0.58f), Pair(0.52f, 0.64f), Pair(0.66f, 0.58f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "छ" -> {
                hint = "'घ' जैसा दो बार गोल बनाकर ऊपर छोटी गांठ बांधें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.34f, 0.26f), Pair(0.50f, 0.26f), Pair(0.34f, 0.46f), Pair(0.56f, 0.62f), Pair(0.52f, 0.40f), Pair(0.52f, 0.20f))
                )
            }
            "ज" -> {
                hint = "खड़ी लाइन, बीच से आड़ी लाइन और नीचे गोल घुमाव बनाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.46f), Pair(0.48f, 0.46f), Pair(0.42f, 0.64f), Pair(0.30f, 0.54f))
                )
            }
            "झ" -> {
                hint = "'इ' बनाकर बीच से लाइन निकालकर खड़ी लाइन से जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.38f, 0.20f), Pair(0.38f, 0.32f), Pair(0.26f, 0.40f), Pair(0.50f, 0.50f), Pair(0.32f, 0.66f)),
                    listOf(Pair(0.38f, 0.48f), Pair(0.66f, 0.48f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "ञ" -> {
                hint = "उल्टा 'C' घुमाएं, बीच से लाइन और खड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.46f, 0.34f), Pair(0.28f, 0.48f), Pair(0.46f, 0.62f)),
                    listOf(Pair(0.46f, 0.48f), Pair(0.66f, 0.48f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "ट" -> {
                hint = "छोटी खड़ी लाइन और नीचे बड़ा आधा गोल कटोरा बनाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.36f)),
                    listOf(Pair(0.50f, 0.36f), Pair(0.32f, 0.50f), Pair(0.50f, 0.74f), Pair(0.70f, 0.58f))
                )
            }
            "ठ" -> {
                hint = "छोटी लाइन और नीचे पूरा गोल घेरा बनाएं"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.36f)),
                    listOf(Pair(0.50f, 0.36f), Pair(0.32f, 0.56f), Pair(0.50f, 0.76f), Pair(0.68f, 0.56f), Pair(0.50f, 0.36f))
                )
            }
            "ड" -> {
                hint = "छोटी लाइन और सुंदर 'S' जैसा आकार मोड़ें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.34f), Pair(0.34f, 0.46f), Pair(0.64f, 0.56f), Pair(0.40f, 0.74f))
                )
            }
            "ढ" -> {
                hint = "'ट' बनाकर अंदर छोटा गोल लूप बंद करें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.36f), Pair(0.32f, 0.52f), Pair(0.52f, 0.74f), Pair(0.64f, 0.56f), Pair(0.52f, 0.52f))
                )
            }
            "ण" -> {
                hint = "'U' जैसा लूप बनाएं और पास में खड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.20f), Pair(0.34f, 0.56f), Pair(0.48f, 0.62f), Pair(0.48f, 0.20f)),
                    listOf(Pair(0.68f, 0.20f), Pair(0.68f, 0.78f))
                )
            }
            "त" -> {
                hint = "खड़ी लाइन, बीच से लाइन निकालकर नीचे मोड़ें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.44f), Pair(0.44f, 0.44f), Pair(0.36f, 0.70f))
                )
            }
            "थ" -> {
                hint = "ऊपर छोटी गांठ, नीचे गोल घुमाव और खड़ी लाइन"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.34f), Pair(0.44f, 0.24f), Pair(0.34f, 0.48f), Pair(0.50f, 0.66f), Pair(0.68f, 0.66f)),
                    listOf(Pair(0.68f, 0.20f), Pair(0.68f, 0.78f))
                )
            }
            "द" -> {
                hint = "'ट' बनाकर नीचे तिरछी पूंछ निकालें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.36f), Pair(0.34f, 0.50f), Pair(0.56f, 0.66f), Pair(0.40f, 0.80f))
                )
            }
            "ध" -> {
                hint = "ऊपर छोटी गांठ, दो घुमाव और खड़ी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.32f, 0.30f), Pair(0.44f, 0.24f), Pair(0.34f, 0.46f), Pair(0.50f, 0.62f), Pair(0.68f, 0.62f)),
                    listOf(Pair(0.68f, 0.20f), Pair(0.68f, 0.78f))
                )
            }
            "न" -> {
                hint = "खड़ी लाइन, बीच से आड़ी लाइन और छोटा लूप बनाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.46f), Pair(0.38f, 0.46f), Pair(0.30f, 0.54f), Pair(0.38f, 0.54f))
                )
            }
            "प" -> {
                hint = "'U' जैसा आकार बनाकर खड़ी लाइन से जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.20f), Pair(0.34f, 0.52f), Pair(0.50f, 0.60f), Pair(0.66f, 0.60f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "फ" -> {
                hint = "'प' बनाकर दाईं तरफ नीचे पूंछ लटकाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.20f), Pair(0.34f, 0.52f), Pair(0.50f, 0.60f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.78f)),
                    listOf(Pair(0.50f, 0.46f), Pair(0.70f, 0.54f), Pair(0.70f, 0.72f))
                )
            }
            "ब" -> {
                hint = "खड़ी लाइन, बीच में गोल पेट और अंदर तिरछी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.38f), Pair(0.38f, 0.38f), Pair(0.38f, 0.62f), Pair(0.66f, 0.62f)),
                    listOf(Pair(0.40f, 0.40f), Pair(0.64f, 0.60f))
                )
            }
            "भ" -> {
                hint = "ऊपर गांठ, खड़ी लाइन, अंदर मोड़कर बड़ी खड़ी लाइन से जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.30f), Pair(0.34f, 0.56f), Pair(0.44f, 0.56f), Pair(0.66f, 0.56f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "म" -> {
                hint = "खड़ी लाइन, अंदर मोड़कर आड़ी लाइन और पास में खड़ी लाइन"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.20f), Pair(0.34f, 0.56f), Pair(0.46f, 0.56f), Pair(0.66f, 0.56f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "य" -> {
                hint = "आधा घुमाव, नीचे मोड़ और खड़ी लाइन से मिलाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.28f), Pair(0.46f, 0.38f), Pair(0.38f, 0.56f), Pair(0.54f, 0.64f), Pair(0.66f, 0.64f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "र" -> {
                hint = "आधा गोल घुमाकर नीचे तिरछी पूंछ निकालें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.40f, 0.26f), Pair(0.56f, 0.26f), Pair(0.60f, 0.44f), Pair(0.42f, 0.50f), Pair(0.62f, 0.78f))
                )
            }
            "ल" -> {
                hint = "खड़ी लाइन, बीच से दो बार उल्टा घुमाव बनाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.52f), Pair(0.48f, 0.42f), Pair(0.34f, 0.56f), Pair(0.30f, 0.72f))
                )
            }
            "व" -> {
                hint = "खड़ी लाइन और बीच में गोल पेट बनाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.38f), Pair(0.38f, 0.38f), Pair(0.38f, 0.60f), Pair(0.66f, 0.60f))
                )
            }
            "श" -> {
                hint = "ऊपर गोल गांठ, '2' जैसा मोड़, और पास में खड़ी लाइन"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.26f), Pair(0.46f, 0.38f), Pair(0.32f, 0.56f), Pair(0.46f, 0.72f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "ष" -> {
                hint = "'प' बनाकर अंदर से तिरछी लाइन खींचें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.20f), Pair(0.34f, 0.52f), Pair(0.50f, 0.60f), Pair(0.66f, 0.60f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.36f, 0.36f), Pair(0.64f, 0.58f))
                )
            }
            "स" -> {
                hint = "'र' बनाकर बीच से आड़ी लाइन और खड़ी लाइन से मिलाएं"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.34f, 0.26f), Pair(0.48f, 0.38f), Pair(0.34f, 0.72f)),
                    listOf(Pair(0.44f, 0.48f), Pair(0.66f, 0.48f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f))
                )
            }
            "ह" -> {
                hint = "छोटी लाइन, 'S' जैसा मोड़ें और नीचे आधा चाँद जोड़ें"
                strokes = listOf(
                    listOf(Pair(0.28f, 0.20f), Pair(0.72f, 0.20f)),
                    listOf(Pair(0.50f, 0.20f), Pair(0.50f, 0.32f), Pair(0.36f, 0.42f), Pair(0.56f, 0.50f), Pair(0.36f, 0.62f), Pair(0.60f, 0.74f))
                )
            }
            "क्ष" -> {
                hint = "खड़ी लाइन, बीच से ऊपर गांठ, 'S' मोड़ और नीचे पूंछ"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.46f), Pair(0.40f, 0.30f), Pair(0.34f, 0.50f), Pair(0.52f, 0.72f))
                )
            }
            "त्र" -> {
                hint = "खड़ी लाइन और बाईं ओर तीर के समान दो तिरछी लाइनें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.46f), Pair(0.34f, 0.32f)),
                    listOf(Pair(0.66f, 0.46f), Pair(0.34f, 0.68f))
                )
            }
            "ज्ञ" -> {
                hint = "खड़ी लाइन, बीच से लाइन, 'ज' घुमाकर नीचे पूंछ निकालें"
                strokes = listOf(
                    listOf(Pair(0.25f, 0.20f), Pair(0.75f, 0.20f)),
                    listOf(Pair(0.66f, 0.20f), Pair(0.66f, 0.78f)),
                    listOf(Pair(0.66f, 0.46f), Pair(0.44f, 0.46f), Pair(0.36f, 0.62f), Pair(0.52f, 0.78f))
                )
            }
            else -> return null
        }

        return LetterTracingData(
            hintHindi = hint,
            strokePaths = strokes,
            guideDots = strokes.flatten()
        )
    }
}

