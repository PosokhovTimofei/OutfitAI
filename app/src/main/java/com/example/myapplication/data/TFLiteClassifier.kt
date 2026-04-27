package com.example.myapplication.data

import android.content.Context
import android.graphics.*
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(
    context: Context,
    modelName: String,
    labelName: String
) {

    private val interpreter: Interpreter
    private val labels: List<String>

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    init {
        interpreter = Interpreter(loadModelFile(context, modelName))

        labels = context.assets.open(labelName)
            .bufferedReader()
            .readLines()
    }

    // 🔥 Убираем прозрачность (очень важно из-за ластика)
    private fun removeTransparency(src: Bitmap): Bitmap {
        val bmp = Bitmap.createBitmap(
            src.width,
            src.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(src, 0f, 0f, null)
        return bmp
    }

    fun classify(bitmap: Bitmap): String {

        val shape = interpreter.getInputTensor(0).shape()
        val height = shape[1]
        val width = shape[2]
        val channels = shape[3]

        Log.d("MODEL", "shape=${shape.contentToString()}")

        // ✅ фикс прозрачности
        val cleanBitmap = removeTransparency(bitmap)

        val resized = Bitmap.createScaledBitmap(cleanBitmap, width, height, true)

        val input = ByteBuffer.allocateDirect(4 * width * height * channels)
        input.order(ByteOrder.nativeOrder())

        for (y in 0 until height) {
            for (x in 0 until width) {

                val px = resized.getPixel(x, y)

                val r = (px shr 16 and 0xFF) / 255f
                val g = (px shr 8 and 0xFF) / 255f
                val b = (px and 0xFF) / 255f

                if (channels == 1) {
                    // ✅ grayscale + нормализация как в Teachable Machine
                    val gray = 0.299f * r + 0.587f * g + 0.114f * b
                    val normalized = (gray - 0.5f) * 2f
                    input.putFloat(normalized)
                } else {
                    // (на будущее, если модель будет RGB)
                    input.putFloat((r - 0.5f) * 2f)
                    input.putFloat((g - 0.5f) * 2f)
                    input.putFloat((b - 0.5f) * 2f)
                }
            }
        }

        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(input, output)

        Log.d("MODEL", "probs=${output[0].joinToString()}")

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: 0

        return labels[maxIndex]
    }
}