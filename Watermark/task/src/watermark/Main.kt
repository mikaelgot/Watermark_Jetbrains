package watermark

import java.awt.Color
import java.awt.Transparency.TRANSLUCENT
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    val basePath = ""//"C:/Users/OM1907LT_Admin/Desktop/"
    println("Input the image filename:")
    val imageFile = File(basePath + readln().trim())
    if(!imageFile.exists()) {
        println("The file $imageFile doesn't exist.")
        exitProcess(0)
    }
    val image: BufferedImage = ImageIO.read(imageFile)
    if (image.colorModel.numComponents != 3) {
        println("The number of image color components isn't 3.")
        exitProcess(0)
    }
    if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
        println("The image isn't 24 or 32-bit.")
        exitProcess(0)
    }

    println("Input the watermark image filename:")
    val watermarkFile = File(basePath + readln().trim())
    if(!watermarkFile.exists()) {
        println("The file $watermarkFile doesn't exist.")
        exitProcess(0)
    }
    val watermark: BufferedImage = ImageIO.read(watermarkFile)
    if (watermark.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        exitProcess(0)
    }
    if (watermark.colorModel.pixelSize != 24 && watermark.colorModel.pixelSize != 32) {
        println("The watermark isn't 24 or 32-bit.")
        exitProcess(0)
    }
    if (watermark.width > image.width || watermark.height > image.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }
    var transColor: Color? = null
    var useAlpha = false
    if(watermark.colorModel.transparency == TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        useAlpha = readln().trim().uppercase() == "YES"
    }
    else {
        println("Do you want to set a transparency color?")
        if(readln().trim().uppercase() == "YES") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            val inputColors = readln().trim().split(" ").map { it.toIntOrNull() }
            if (inputColors.any{ it == null} || !inputColors.all{ it in 0..255 } || inputColors.size != 3) {
                println("The transparency color input is invalid.")
                exitProcess(0)
            }
            else transColor = Color(inputColors[0]!!, inputColors[1]!!, inputColors[2]!!)
        }
    }

    println("Input the watermark transparency percentage (Integer 0-100):")
    val weightRead = readln().toIntOrNull()
    if (weightRead == null) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    val weight = weightRead ?: 0
    if (weight !in 0..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }
    var inputPos: List<Int?> = listOf(0, 0)
    println("Choose the position method (single, grid):")
    val posMethod = readln().trim()
    if (posMethod != "single" && posMethod != "grid") {
        println("The position method input is invalid.")
        exitProcess(0)
    }
    else if(posMethod == "single") {
        val diffX = image.width - watermark.width
        val diifY = image.height - watermark.height
        println("Input the watermark position ([x 0-$diffX] [y 0-$diifY]):")
        inputPos = readln().trim().split(" ").map { it.toIntOrNull() }
        if (inputPos.any { it == null } || inputPos.size != 2) {
            println("The position input is invalid.")
            exitProcess(0)
        } else if (inputPos[0] !in 0..diffX || inputPos[1] !in 0..diifY) {
            println("The position input is out of range.")
            exitProcess(0)
        }
    }

    println("Input the output image filename (jpg or png extension):")
    val outputFile = File(basePath + readln().trim())
    if( outputFile.extension != "jpg" && outputFile.extension != "png") {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }
    val output = BufferedImage(image.width, image.height, image.type)
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val i = Color(image.getRGB(x, y))
            var w = i
            if(posMethod == "single") {
                if (x in inputPos[0]!! until inputPos[0]!! + watermark.width && y in inputPos[1]!! until inputPos[1]!! + watermark.height) {
                    w = Color(watermark.getRGB(x - inputPos[0]!!, y - inputPos[1]!!), useAlpha)
                }
            }
            else if (posMethod == "grid") {
                w = Color(watermark.getRGB(x % watermark.width, y % watermark.height), useAlpha)
            }

            val color = Color(
                (weight * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
            if(w.alpha == 0 || w.rgb == transColor?.rgb) output.setRGB(x, y, i.rgb)
            else output.setRGB(x, y, color.rgb)
        }
    }

    ImageIO.write(output, outputFile.extension, outputFile)
    println("The watermarked image $outputFile has been created.")
}

enum class Transparency{
    OPAQUE,
    BITMASK,
    TRANSLUCENT
}