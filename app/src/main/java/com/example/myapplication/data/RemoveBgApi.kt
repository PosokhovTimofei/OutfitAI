package com.example.myapplication.data

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object RemoveBgApi {

    private val client = OkHttpClient()

    fun removeBackground(
        inputFile: File,
        apiKey: String
    ): File {

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image_file",
                inputFile.name,
                inputFile.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.remove.bg/v1.0/removebg")
            .addHeader("X-Api-Key", apiKey)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code}")
        }

        val outputFile = File(
            inputFile.parent,
            "no_bg_${inputFile.name}.png"
        )

        outputFile.writeBytes(response.body!!.bytes())

        return outputFile
    }
}