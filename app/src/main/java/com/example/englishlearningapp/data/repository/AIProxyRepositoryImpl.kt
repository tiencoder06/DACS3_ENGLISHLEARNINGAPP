package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.AIMessage
import com.example.englishlearningapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIProxyRepositoryImpl @Inject constructor() : AIProxyRepository {

    // For Android Emulator, 10.0.2.2 refers to the host machine's localhost
    private val workerUrl = "http://10.0.2.2:8787/api/v1/chat"

    override suspend fun askWorker(
        userMessage: String,
        placementLevel: String?,
        placementWeakSkill: String?,
        recentMessages: List<AIMessage>
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(workerUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            // Create JSON Request
            val requestBody = JSONObject().apply {
                put("userMessage", userMessage)
                put("placementLevel", placementLevel ?: "Beginner")
                put("placementWeakSkill", placementWeakSkill ?: "Balanced")
                
                val historyArray = JSONArray()
                recentMessages.forEach { msg ->
                    historyArray.put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
                put("recentMessages", historyArray)
            }

            // Send Request
            OutputStreamWriter(connection.outputStream).use { it.write(requestBody.toString()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseString)
                
                if (jsonResponse.getBoolean("success")) {
                    val assistantMessage = jsonResponse.getString("assistantMessage")
                    Resource.Success(assistantMessage)
                } else {
                    val error = jsonResponse.optString("errorMessage", "Unknown AI error")
                    Resource.Error(error)
                }
            } else {
                val errorString = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Log.e("AIProxy", "Server error: $errorString")
                Resource.Error("Không thể kết nối với máy chủ AI ($responseCode)")
            }
        } catch (e: Exception) {
            Log.e("AIProxy", "Network error", e)
            Resource.Error("Lỗi mạng: Không thể kết nối với AI Tutor. Vui lòng đảm bảo Worker đang chạy.")
        }
    }
}
