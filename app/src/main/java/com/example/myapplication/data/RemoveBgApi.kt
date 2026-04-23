package com.example.myapplication.data

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object RemoveBgApi {

    private const val BASE_URL = "http://192.168.50.91:8000/remove-bg"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .build()

    fun removeBackground(file: File): File {

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("image/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->

            val json = response.body?.string()
                ?: throw Exception("Empty response")

            val base64Image = JSONObject(json).getString("image")

            val bytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)

            val outFile = File(
                file.parentFile,
                "bg_removed_${System.currentTimeMillis()}.png"
            )

            outFile.writeBytes(bytes)

            return outFile
        }
    }
}