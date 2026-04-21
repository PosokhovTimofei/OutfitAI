package com.example.myapplication.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorage {

    fun saveTemp(context: Context, uri: Uri): File {

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open image")

        val file = File(
            context.filesDir,
            "temp_${UUID.randomUUID()}.jpg"
        )

        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        return file
    }
}