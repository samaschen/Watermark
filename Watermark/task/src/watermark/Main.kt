package watermark
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import java.lang.Exception
import kotlin.system.exitProcess

fun main() {
    println("Input the image filename:")
    val imageName = readln()
    val image = fileCheck(imageName, "image")
    println("Input the watermark image filename:")
    val watermarkName = readln()
    val watermark = fileCheck(watermarkName, "watermark")
    compare(image, watermark)
    var alpha = false
    var manualTransparencyColor = Color(0, 0, 0)
    if (watermark.transparency == 3) {
        println("Do you want to use the watermark's Alpha channel?")
        alpha = readln().lowercase() == "yes"
    } else {
        println("Do you want to set a transparency color?")
        if (readln().lowercase() == "yes") {
            manualTransparencyColor  = manualTransparencyProcess()
        }
    }
    println("Input the watermark transparency percentage (Integer 0-100):")
    val transparencyStr = readln()
    val transparency = transparencyIntCheck(transparencyStr)
    println("Choose the position method (single, grid):")
    val method = readln()
    val pairXY = methodCheck(method, image, watermark)
    println("Input the output image filename (jpg or png extension):")
    val outputName = readln()
    extensionCheck(outputName)
    blend(image, watermark, transparency, outputName, alpha, manualTransparencyColor, method, pairXY)
}

fun fileCheck(fileName: String, id: String): BufferedImage {
    val file = File(fileName)
    if (!file.exists()) {
        println("The file $fileName doesn't exist.")
        exitProcess(0)
    }
    val image: BufferedImage = ImageIO.read(file)
    if (image.colorModel.numColorComponents != 3) {
        println("The number of $id color components isn't 3.")
        exitProcess(0)
    }
    if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
        println("The $id isn't 24 or 32-bit.")
        exitProcess(0)
    }
    return image
}

fun compare(i: BufferedImage, w: BufferedImage) {
    if (i.width < w.width || i.height < w.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }
}

fun manualTransparencyProcess(): Color {
    println("Input a transparency color ([Red] [Green] [Blue]):")
    try {
        val manualTransparencyRGB = readln().split(" ")
        if (manualTransparencyRGB.size != 3) throw Exception()
        val (inputR, inputG, inputB) = manualTransparencyRGB.map { it.toInt() }
        return Color(inputR, inputG, inputB)
    } catch (e: Exception) {
        println("The transparency color input is invalid.")
        exitProcess(0)
    }
}

fun transparencyIntCheck(transparencyStr: String): Int {
    try {
        val transparency = transparencyStr.toInt()
        if (transparency !in 0..100) {
            println("The transparency percentage is out of range.")
            exitProcess(0)
        }
        return transparency
    } catch (e: Exception) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
}

fun methodCheck(method: String, image: BufferedImage, watermark: BufferedImage): Pair<Int, Int> {
    when (method) {
        "single" -> {
            val diffX = image.width - watermark.width
            val diffY = image.height - watermark.height
            println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
            try {
                val (x, y) = readln().split(" ").map { it.toInt() }
                if (x !in 0..diffX || y !in 0..diffY) {
                    println("The position input is out of range.")
                    exitProcess(0)
                } else {
                    return Pair(x, y)
                }
            } catch (e: Exception) {
                println("The position input is invalid.")
                exitProcess(0)
            }
        }
        !in arrayOf("single", "grid") -> {
            println("The position method input is invalid.")
            exitProcess(0)
        }
    }
    return Pair(0, 0)
}

fun extensionCheck(outputName: String) {
    if (outputName.split(".")[1] !in arrayOf("jpg", "png")) {
        println("The position method input is invalid.")
        exitProcess(0)
    }
}

fun blend(image: BufferedImage, watermark: BufferedImage, transparency: Int, outputName: String, alpha: Boolean,
          manualTransparencyColor: Color, method: String, pairXY: Pair<Int, Int>) {
    val outputImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val (watermarkX, watermarkY) = pairXY
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val i = Color(image.getRGB(x, y))
            var w = Color(watermark.getRGB(0, 0), alpha)
            when (method) {
                "single" -> {
                    if (x in watermarkX until (watermarkX + watermark.width) && y in watermarkY until (watermarkY + watermark.height)) {
                        w = Color(watermark.getRGB(x - watermarkX, y - watermarkY), alpha)
                    }
                }
                "grid" -> w = Color(watermark.getRGB(x % watermark.width, y % watermark.height), alpha)
            }
            val color = if (w.alpha == 0 || !alpha && w == manualTransparencyColor) i
            else Color((transparency * w.red + (100 - transparency) * i.red) / 100,
                (transparency * w.green + (100 - transparency) * i.green) / 100,
                (transparency * w.blue + (100 - transparency) * i.blue) / 100)
            outputImage.setRGB(x, y, color.rgb)
        }
    }
    ImageIO.write(outputImage, outputName.split(".")[1], File(outputName))
    println("The watermarked image $outputName has been created.")
}
