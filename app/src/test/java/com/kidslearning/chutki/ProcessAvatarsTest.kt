package com.kidslearning.chutki

import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import java.util.ArrayDeque
import javax.imageio.ImageIO
import kotlin.math.hypot

class ProcessAvatarsTest {

    @Test
    fun processUploadedAvatars() {
        val girlFile = File("C:/Users/Dharmendra Singh/.gemini/antigravity/brain/c25298dc-5827-486a-bdbf-4d4f8662ab22/.user_uploaded/media_1793694251372_56dbaba4.jpg")
        val boyFile = File("C:/Users/Dharmendra Singh/.gemini/antigravity/brain/c25298dc-5827-486a-bdbf-4d4f8662ab22/.user_uploaded/media_1793694350522_7676245d.jpg")

        require(girlFile.exists()) { "Girl image file not found at ${girlFile.absolutePath}" }
        require(boyFile.exists()) { "Boy image file not found at ${boyFile.absolutePath}" }

        val girlSrc = ImageIO.read(girlFile)
        val boySrc = ImageIO.read(boyFile)

        val outDir = File("src/main/res/drawable")
        require(outDir.exists()) { "Drawable dir not found" }

        val girlTransparent = makeBackgroundTransparent(girlSrc)
        val boyTransparent = makeBackgroundTransparent(boySrc)

        val girlFlipped = flipHorizontal(girlTransparent)
        val boyFlipped = flipHorizontal(boyTransparent)

        // Save avatar_girl and avatar_boy
        ImageIO.write(girlTransparent, "png", File(outDir, "avatar_girl.png"))
        ImageIO.write(girlFlipped, "png", File(outDir, "avatar_girl_flipped.png"))
        ImageIO.write(boyTransparent, "png", File(outDir, "avatar_boy.png"))
        ImageIO.write(boyFlipped, "png", File(outDir, "avatar_boy_flipped.png"))

        // Also update teacher_priya and teacher_priya_flipped with the high-res 3D Girl
        ImageIO.write(girlTransparent, "png", File(outDir, "teacher_priya.png"))
        ImageIO.write(girlFlipped, "png", File(outDir, "teacher_priya_flipped.png"))

        println("Successfully processed and saved:")
        println(" - avatar_girl.png (${girlTransparent.width}x${girlTransparent.height})")
        println(" - avatar_girl_flipped.png")
        println(" - avatar_boy.png (${boyTransparent.width}x${boyTransparent.height})")
        println(" - avatar_boy_flipped.png")
        println(" - teacher_priya.png & teacher_priya_flipped.png")
    }

    private fun makeBackgroundTransparent(src: BufferedImage): BufferedImage {
        val width = src.width
        val height = src.height
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        // BFS to find all background pixels starting from borders
        val isBackground = Array(height) { BooleanArray(width) }
        val queue = ArrayDeque<Pair<Int, Int>>()

        fun isNearWhite(rgb: Int): Boolean {
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            return r >= 238 && g >= 238 && b >= 238
        }

        // Add all border pixels that are near-white
        for (x in 0 until width) {
            if (isNearWhite(src.getRGB(x, 0))) {
                isBackground[0][x] = true
                queue.add(Pair(x, 0))
            }
            if (isNearWhite(src.getRGB(x, height - 1))) {
                isBackground[height - 1][x] = true
                queue.add(Pair(x, height - 1))
            }
        }
        for (y in 0 until height) {
            if (isNearWhite(src.getRGB(0, y))) {
                isBackground[y][0] = true
                queue.add(Pair(0, y))
            }
            if (isNearWhite(src.getRGB(width - 1, y))) {
                isBackground[y][width - 1] = true
                queue.add(Pair(width - 1, y))
            }
        }

        // 4-directional BFS
        val dx = intArrayOf(1, -1, 0, 0)
        val dy = intArrayOf(0, 0, 1, -1)

        while (queue.isNotEmpty()) {
            val (cx, cy) = queue.removeFirst()

            for (i in 0 until 4) {
                val nx = cx + dx[i]
                val ny = cy + dy[i]

                if (nx in 0 until width && ny in 0 until height && !isBackground[ny][nx]) {
                    if (isNearWhite(src.getRGB(nx, ny))) {
                        isBackground[ny][nx] = true
                        queue.add(Pair(nx, ny))
                    }
                }
            }
        }

        // Write to result with smooth anti-aliased edge
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isBackground[y][x]) {
                    result.setRGB(x, y, 0x00000000)
                } else {
                    val rgb = src.getRGB(x, y)
                    val r = (rgb shr 16) and 0xFF
                    val g = (rgb shr 8) and 0xFF
                    val b = rgb and 0xFF

                    // Check if bordering transparent background for smooth edge defringing
                    var touchesBg = false
                    for (i in 0 until 4) {
                        val nx = x + dx[i]
                        val ny = y + dy[i]
                        if (nx in 0 until width && ny in 0 until height && isBackground[ny][nx]) {
                            touchesBg = true
                            break
                        }
                    }

                    if (touchesBg && r > 220 && g > 220 && b > 220) {
                        // Smoothly fade alpha at the outer edge to prevent white halos
                        val brightness = (r + g + b) / 3f
                        val alpha = ((255f - brightness) / 35f * 255f).toInt().coerceIn(30, 255)
                        val blendedRgb = (alpha shl 24) or (r shl 16) or (g shl 8) or b
                        result.setRGB(x, y, blendedRgb)
                    } else {
                        // Keep full opacity for foreground
                        result.setRGB(x, y, (0xFF shl 24) or (rgb and 0x00FFFFFF))
                    }
                }
            }
        }

        return result
    }

    private fun flipHorizontal(src: BufferedImage): BufferedImage {
        val width = src.width
        val height = src.height
        val flipped = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                flipped.setRGB(width - 1 - x, y, src.getRGB(x, y))
            }
        }
        return flipped
    }
}

