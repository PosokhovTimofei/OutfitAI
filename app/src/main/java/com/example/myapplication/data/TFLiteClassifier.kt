package com.example.myapplication.data

import android.content.Context
import android.graphics.Bitmap
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
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    init {
        val modelBuffer = loadModelFile(context, modelName)
        interpreter = Interpreter(modelBuffer)

        labels = context.assets.open(labelName)
            .bufferedReader()
            .readLines()
    }

    fun classify(bitmap: Bitmap): String {

        val shape = interpreter.getInputTensor(0).shape()
        val height = shape[1]
        val width = shape[2]

        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)

        val input = ByteBuffer.allocateDirect(4 * width * height * 1)
        input.order(ByteOrder.nativeOrder())

        for (y in 0 until height) {
            for (x in 0 until width) {

                val px = resized.getPixel(x, y)

                val r = (px shr 16 and 0xFF)
                val g = (px shr 8 and 0xFF)
                val b = (px and 0xFF)

                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toFloat() / 255f

                input.putFloat(gray)
            }
        }

        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(input, output)

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: 0

        return labels[maxIndex]
        Log.d("MODEL", "bitmap=${bitmap.width}x${bitmap.height}")
    }
}