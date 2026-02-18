package com.example.agritwin.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.agritwin.ui.dialogs.DiseaseAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object PlantDiseaseService {

    private var interpreter: Interpreter? = null
    private val INPUT_SIZE = 224
    private val NUM_CLASSES = 15
    private val CONFIDENCE_THRESHOLD = 0.4f

    private val diseaseClasses = listOf(
        "Healthy",
        "Leaf Spot",
        "Powdery Mildew",
        "Rust",
        "Blight",
        "Mosaic Virus",
        "Yellow Leaf Curl",
        "Wilting",
        "Nutrient Deficiency",
        "Pest Damage",
        "Fungal Infection",
        "Bacterial Infection",
        "Environmental Stress",
        "Root Rot",
        "Unknown Disease"
    )

    private val diseaseDescriptions = mapOf(
        "Healthy" to "Your plant appears to be healthy with no visible signs of disease or stress.",
        "Leaf Spot" to "Fungal or bacterial leaf spots detected. Small, dark lesions appear on the leaves causing discoloration.",
        "Powdery Mildew" to "White powdery coating on leaves detected. This is a common fungal disease affecting plant appearance and health.",
        "Rust" to "Orange/brown rust-like spots detected. A fungal infection that causes characteristic spore pustules on leaf undersides.",
        "Blight" to "Blight infection detected. This serious fungal disease causes rapid wilting and can kill the plant if untreated.",
        "Mosaic Virus" to "Mosaic virus infection detected. Causes mottling, mosaicking, and distortion of leaves reducing photosynthesis.",
        "Yellow Leaf Curl" to "Yellow leaf curl virus detected. Causes yellowing, curling, and stunting of leaves transmitted by whiteflies.",
        "Wilting" to "Plant wilting detected. Could be due to water stress, root rot, or vascular diseases affecting water transport.",
        "Nutrient Deficiency" to "Nutrient deficiency detected. Plant is lacking essential nutrients like nitrogen, phosphorus, or potassium.",
        "Pest Damage" to "Pest damage detected. Insect infestation or pest damage visible on leaves and stems.",
        "Fungal Infection" to "Fungal infection detected. Multiple fungal spores affecting plant health.",
        "Bacterial Infection" to "Bacterial infection detected. Bacterial pathogens affecting plant tissues.",
        "Environmental Stress" to "Environmental stress detected. Plant is affected by extreme temperature, humidity, or light conditions.",
        "Root Rot" to "Root rot detected. Fungal or bacterial infection of the root system.",
        "Unknown Disease" to "Unknown disease detected. Unable to classify with certainty. Consult agricultural expert."
    )

    private val diseaseTreatments = mapOf(
        "Healthy" to "Continue with regular maintenance and monitoring. Keep following good agricultural practices.",
        "Leaf Spot" to "Remove affected leaves immediately. Improve air circulation between plants. Apply fungicide spray weekly. Avoid wetting foliage during irrigation.",
        "Powdery Mildew" to "Apply sulfur or neem oil spray every 7-10 days. Increase air circulation. Reduce humidity levels. Remove heavily infected leaves.",
        "Rust" to "Apply copper fungicide or sulfur spray. Remove and destroy infected leaves. Improve drainage. Reduce leaf wetness duration by morning irrigation.",
        "Blight" to "Remove infected plant parts immediately and destroy. Apply copper fungicide spray. Improve drainage. Quarantine infected plants away from others.",
        "Mosaic Virus" to "Remove and destroy affected plants completely. Control insect vectors (aphids). Disinfect tools with 10% bleach. Use resistant varieties for replanting.",
        "Yellow Leaf Curl" to "Control whitefly populations with insecticide. Remove severely infected plants. Use reflective mulches. Plant resistant varieties.",
        "Wilting" to "Check soil moisture and adjust watering. Improve drainage if overwatered. Provide shade if sun-stressed. Check for root rot and treat accordingly.",
        "Nutrient Deficiency" to "Apply appropriate fertilizer based on deficiency. Use balanced NPK fertilizer. Consider soil testing. Apply micronutrient supplements if needed.",
        "Pest Damage" to "Identify the pest species first. Apply appropriate pesticide or neem oil. Remove heavily infested leaves. Use biological pest control methods.",
        "Fungal Infection" to "Apply broad-spectrum fungicide. Improve air circulation. Remove infected plant parts. Ensure proper drainage and reduce humidity.",
        "Bacterial Infection" to "Remove affected plant parts immediately. Apply copper-based bactericide. Disinfect tools. Quarantine infected plants.",
        "Environmental Stress" to "Adjust watering schedule. Provide shade cloth if needed. Maintain optimal temperature range. Improve humidity levels.",
        "Root Rot" to "Repot in fresh, well-draining soil. Remove rotted roots. Reduce watering frequency. Apply fungicide to soil. Ensure proper drainage.",
        "Unknown Disease" to "Take multiple photos from different angles. Consult with agricultural extension office. Send sample to laboratory for analysis."
    )

    private val preventiveMeasures = mapOf(
        "Healthy" to "Maintain proper watering schedule. Ensure good air circulation. Monitor regularly for signs of disease. Practice crop rotation. Keep area clean.",
        "Leaf Spot" to "Water at soil level to keep foliage dry. Space plants properly for air flow. Remove fallen leaves promptly. Practice crop rotation yearly.",
        "Powdery Mildew" to "Ensure adequate spacing between plants. Maintain low humidity (40-50%). Remove infected leaves early. Avoid overhead watering. Use resistant varieties.",
        "Rust" to "Improve air circulation around plants. Reduce leaf wetness duration. Remove affected leaves promptly. Sanitary practices with tools. Use resistant varieties.",
        "Blight" to "Plant resistant or tolerant varieties. Practice crop rotation. Ensure good drainage. Avoid overhead watering. Remove volunteer plants.",
        "Mosaic Virus" to "Control aphid vectors using insecticide. Use resistant varieties. Sanitize equipment and tools. Remove infected plants early. Manage weeds.",
        "Yellow Leaf Curl" to "Control whitefly populations actively. Use reflective mulches and row covers. Plant resistant varieties. Quarantine new plants before planting.",
        "Wilting" to "Maintain consistent soil moisture (not waterlogged). Improve drainage in heavy soils. Provide proper light conditions. Mulch around plants. Monitor root health.",
        "Nutrient Deficiency" to "Regular soil testing. Apply balanced fertilizer seasonally. Add compost or organic matter. Maintain proper soil pH. Use slow-release fertilizers.",
        "Pest Damage" to "Regular plant inspection. Remove weeds that harbor pests. Use companion planting. Install physical barriers. Monitor for early pest signs.",
        "Fungal Infection" to "Improve air circulation. Reduce humidity. Remove fallen leaves. Practice sanitation. Use disease-resistant varieties.",
        "Bacterial Infection" to "Plant resistant varieties. Maintain good sanitation. Remove infected plants promptly. Sterilize pruning tools. Avoid overhead irrigation.",
        "Environmental Stress" to "Provide proper shelter or shade. Maintain optimal temperature range. Use mulch for temperature regulation. Improve humidity control.",
        "Root Rot" to "Ensure proper drainage in growing medium. Use well-draining potting soil. Avoid overwatering. Monitor soil moisture regularly. Improve aeration.",
        "Unknown Disease" to "Maintain detailed records. Take regular photos. Monitor spread pattern. Document symptoms progression. Seek professional help early."
    )

    fun initializeModel(context: Context) {
        try {
            if (interpreter == null) {
                val modelBuffer = FileUtil.loadMappedFile(context, "plant_disease_model.tflite")
                interpreter = Interpreter(modelBuffer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun analyzePlantDiseaseWithTFLite(
        imageUri: Uri,
        context: Context
    ): DiseaseAnalysis = withContext(Dispatchers.Default) {
        return@withContext try {
            if (interpreter == null) {
                initializeModel(context)
            }

            if (interpreter == null) {
                return@withContext DiseaseAnalysis(
                    diseaseName = "Error",
                    confidence = 0f,
                    description = "Model initialization failed. Please ensure plant_disease_model.tflite is in assets folder.",
                    treatment = "N/A",
                    severity = "Unknown",
                    preventiveMeasures = "N/A"
                )
            }

            val bitmap = loadBitmap(imageUri, context) ?: return@withContext DiseaseAnalysis(
                diseaseName = "Error",
                confidence = 0f,
                description = "Failed to load image. Please select a valid image file.",
                treatment = "N/A",
                severity = "Unknown",
                preventiveMeasures = "N/A"
            )

            val inputBuffer = preprocessImage(bitmap)

            val outputBuffer = Array(1) { FloatArray(NUM_CLASSES) }

            // Run inference
            interpreter?.run(inputBuffer, outputBuffer)

            val outputData = outputBuffer[0]

            val maxIndex = outputData.indices.maxByOrNull { outputData[it] } ?: 0
            val confidence = outputData[maxIndex].coerceIn(0f, 1f)

            if (confidence < CONFIDENCE_THRESHOLD) {
                return@withContext DiseaseAnalysis(
                    diseaseName = "Unable to analyze",
                    confidence = confidence,
                    description = "Confidence too low (${(confidence * 100).toInt()}%). Please provide a clearer image with better lighting and focus on the affected area.",
                    treatment = "Take another photo with better lighting and focus",
                    severity = "Unknown",
                    preventiveMeasures = "Ensure good lighting, clear focus on plant, and minimal shadows"
                )
            }

            // Get disease name
            val diseaseName = if (maxIndex < diseaseClasses.size) {
                diseaseClasses[maxIndex]
            } else {
                "Unknown"
            }

            val severity = when {
                diseaseName == "Healthy" -> "None"
                confidence > 0.9f -> "High"
                confidence > 0.7f -> "Medium"
                else -> "Low"
            }

            DiseaseAnalysis(
                diseaseName = diseaseName,
                confidence = confidence,
                description = diseaseDescriptions[diseaseName] ?: "Disease detected in the plant.",
                treatment = diseaseTreatments[diseaseName] ?: "Consult with an agricultural expert for treatment.",
                severity = severity,
                preventiveMeasures = preventiveMeasures[diseaseName] ?: "Implement good agricultural practices."
            )

        } catch (e: Exception) {
            e.printStackTrace()
            DiseaseAnalysis(
                diseaseName = "Error",
                confidence = 0f,
                description = "Analysis failed: ${e.localizedMessage}",
                treatment = "Try again with another image",
                severity = "Unknown",
                preventiveMeasures = "N/A"
            )
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())


        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (value in intValues) {
            val red = ((value shr 16) and 0xFF)
            val green = ((value shr 8) and 0xFF)
            val blue = (value and 0xFF)

            byteBuffer.putFloat(red / 255.0f)
            byteBuffer.putFloat(green / 255.0f)
            byteBuffer.putFloat(blue / 255.0f)
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * Load bitmap from URI
     */
    private fun loadBitmap(imageUri: Uri, context: Context): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)?.also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Release model resources
     */
    fun releaseModel() {
        interpreter?.close()
        interpreter = null
    }
}