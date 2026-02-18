package com.example.agritwin.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.agritwin.config.ApiKeys
import com.example.agritwin.ui.dialogs.DiseaseAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAIService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun analyzePlantDisease(
        imageUri: Uri,
        context: Context
    ): DiseaseAnalysis = withContext(Dispatchers.IO) {
        return@withContext try {
            if (ApiKeys.GEMINI_API_KEY.isEmpty() || ApiKeys.GEMINI_API_KEY == "YOUR_GEMINI_API_KEY_HERE") {
                throw Exception("Gemini API Key is not configured. Please add your API key to ApiKeys.kt")
            }

            val imageData = uriToBase64(imageUri, context)
            if (imageData.isEmpty()) {
                throw Exception("Failed to convert image to base64")
            }

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray()
                            .put(
                                JSONObject().apply {
                                    put("text", "Analyze this plant image for diseases, pests, or health issues. " +
                                            "Provide your response ONLY as valid JSON (no markdown, no extra text) in this format: " +
                                            "{\"diseaseName\": \"name or Healthy\", \"confidence\": 0.95, \"description\": \"details\", " +
                                            "\"treatment\": \"steps\", \"severity\": \"Low/Medium/High\", \"preventiveMeasures\": \"measures\"}")
                                }
                            )
                            .put(
                                JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", "image/jpeg")
                                        put("data", imageData)
                                    })
                                }
                            )
                        )
                    }
                ))
            }.toString()

            val request = Request.Builder()
                .url("$GEMINI_API_URL?key=${ApiKeys.GEMINI_API_KEY}")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("API Error: ${response.code} - ${response.body?.string() ?: "Unknown error"}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty response from API")

            val jsonResponse = JSONObject(responseBody)

            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                throw Exception("API Error: ${error.optString("message", "Unknown error")}")
            }

            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                throw Exception("No response from API")
            }

            val textContent = candidates
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val cleanedJson = cleanJsonResponse(textContent)
            val analysisJson = JSONObject(cleanedJson)

            DiseaseAnalysis(
                diseaseName = analysisJson.optString("diseaseName", "Unknown").trim(),
                confidence = analysisJson.optDouble("confidence", 0.0).coerceIn(0.0, 1.0).toFloat(),
                description = analysisJson.optString("description", "No description available").trim(),
                treatment = analysisJson.optString("treatment", "Consult agricultural expert").trim(),
                severity = analysisJson.optString("severity", "Medium").trim(),
                preventiveMeasures = analysisJson.optString("preventiveMeasures", "Implement good agricultural practices").trim()
            )
        } catch (e: JSONException) {
            DiseaseAnalysis(
                diseaseName = "Error",
                confidence = 0f,
                description = "Failed to parse response: ${e.message}",
                treatment = "N/A",
                severity = "Unknown",
                preventiveMeasures = "N/A"
            )
        } catch (e: Exception) {
            DiseaseAnalysis(
                diseaseName = "Error",
                confidence = 0f,
                description = e.message ?: "Unknown error occurred",
                treatment = "N/A",
                severity = "Unknown",
                preventiveMeasures = "N/A"
            )
        }
    }

    suspend fun getChatbotResponse(
        userMessage: String,
        farmMetrics: FarmHealthMetrics
    ): String = withContext(Dispatchers.IO) {
        return@withContext try {
            if (ApiKeys.GEMINI_API_KEY.isEmpty() || ApiKeys.GEMINI_API_KEY == "YOUR_GEMINI_API_KEY_HERE") {
                return@withContext "Please configure your Gemini API key to use the chatbot."
            }

            val prompt = """
                You are an agricultural AI assistant for a farm management app. 
                The farmer asked: "$userMessage"
                
                Current farm metrics:
                - Temperature: ${farmMetrics.temperature}°C
                - Humidity: ${farmMetrics.humidity}%
                - Soil Moisture: ${farmMetrics.soilMoisture * 100}%
                - NDVI: 0.65
                - Wind Speed: ${farmMetrics.windSpeed} m/s
                - Rain Probability: ${farmMetrics.rainProbability}%
                
                Provide a helpful, practical response for the farmer in 1-2 sentences.
            """.trimIndent()

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(
                            JSONObject().apply {
                                put("text", prompt)
                            }
                        ))
                    }
                ))
            }.toString()

            val request = Request.Builder()
                .url("$GEMINI_API_URL?key=${ApiKeys.GEMINI_API_KEY}")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext "Sorry, I couldn't process your request. Please try again."
            }

            val responseBody = response.body?.string() ?: return@withContext "No response from server"
            val jsonResponse = JSONObject(responseBody)

            jsonResponse
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "Sorry, I encountered an error: ${e.message}"
        }
    }

    private fun uriToBase64(uri: Uri, context: Context): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: byteArrayOf()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw Exception("Failed to read image file: ${e.message}")
        }
    }

    private fun cleanJsonResponse(text: String): String {
        return try {
            val startIndex = text.indexOf("{")
            val endIndex = text.lastIndexOf("}") + 1

            if (startIndex != -1 && endIndex > startIndex) {
                text.substring(startIndex, endIndex)
            } else {
                """{"diseaseName": "Unable to analyze", "confidence": 0.0, "description": "Please try another image", "treatment": "N/A", "severity": "Unknown", "preventiveMeasures": "N/A"}"""
            }
        } catch (e: Exception) {
            """{"diseaseName": "Error", "confidence": 0.0, "description": "Analysis failed", "treatment": "N/A", "severity": "Unknown", "preventiveMeasures": "N/A"}"""
        }
    }
}