package com.example.agritwin.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.agritwin.ai.PlantDiseaseService
import com.example.agritwin.ui.theme.*
import kotlinx.coroutines.launch

data class DiseaseAnalysis(
    val diseaseName: String = "",
    val confidence: Float = 0f,
    val description: String = "",
    val treatment: String = "",
    val severity: String = "",
    val preventiveMeasures: String = ""
)

@Composable
fun DiseaseDetectionDialog(
    onDismiss: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diseaseAnalysis by remember { mutableStateOf<DiseaseAnalysis?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            coroutineScope.launch {
                isAnalyzing = true
                errorMessage = null
                try {
                    val analysis = PlantDiseaseService.analyzePlantDiseaseWithTFLite(uri, context)
                    diseaseAnalysis = analysis
                    if (analysis.diseaseName == "Error") {
                        errorMessage = analysis.description
                    }
                } catch (e: Exception) {
                    errorMessage = "Error analyzing image: ${e.message}"
                    e.printStackTrace()
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            color = Neutral50
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorRed)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFlorist,
                            contentDescription = "Disease Detection",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Disease Detection",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedImageUri == null) {
                        // ==================== UPLOAD SECTION ====================
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Upload Plant Image",
                                style = MaterialTheme.typography.labelLarge,
                                color = Neutral900,
                                fontWeight = FontWeight.SemiBold
                            )

                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarningOrange,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    "Take Photo / Select from Gallery",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "📸 Best Practices:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    listOf(
                                        "Take a clear, well-lit photo of the affected leaf/plant",
                                        "Focus on the diseased area or pest damage",
                                        "Include affected and healthy parts for comparison",
                                        "Avoid shadows and reflections",
                                        "Provide close-up view for better analysis"
                                    ).forEach { tip ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("✓", fontSize = 14.sp, color = SuccessGreen)
                                            Text(
                                                tip,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Neutral700
                                            )
                                        }
                                    }
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = InfoBlue.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Powered by AI",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Uses advanced machine learning for accurate detection. Works offline!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral700
                                    )
                                    Text(
                                        "Detects:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    listOf(
                                        "Fungal infections (Mildew, Rust, etc.)",
                                        "Bacterial diseases (Blight, Spot, etc.)",
                                        "Viral infections",
                                        "Pest damage",
                                        "Nutrient deficiencies",
                                        "Environmental stress"
                                    ).forEach { disease ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("•", fontSize = 12.sp, color = InfoBlue)
                                            Text(
                                                disease,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Neutral700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isAnalyzing) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                color = ErrorRed,
                                strokeWidth = 4.dp
                            )
                            Text(
                                "Analyzing plant disease...",
                                style = MaterialTheme.typography.labelLarge,
                                color = Neutral700
                            )
                            Text(
                                "Using AI model for detection",
                                style = MaterialTheme.typography.labelSmall,
                                color = Neutral500
                            )
                        }
                    } else if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = ErrorRed.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "Analysis Failed",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ErrorRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        errorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral700
                                    )
                                }
                            }
                        }

                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected plant image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Button(
                            onClick = {
                                selectedImageUri = null
                                diseaseAnalysis = null
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green600),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Try Another Image")
                        }
                    } else if (diseaseAnalysis != null) {
                        val analysis = diseaseAnalysis!!

                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected plant image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = when {
                                    analysis.confidence > 0.8f -> ErrorRed.copy(alpha = 0.1f)
                                    analysis.confidence > 0.5f -> WarningOrange.copy(alpha = 0.1f)
                                    else -> SuccessGreen.copy(alpha = 0.1f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Disease Detected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Neutral600
                                        )
                                        Text(
                                            analysis.diseaseName.ifEmpty { "Healthy Plant" },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = when {
                                                analysis.confidence > 0.8f -> ErrorRed
                                                analysis.confidence > 0.5f -> WarningOrange
                                                else -> SuccessGreen
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Confidence",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Neutral600
                                        )
                                        Text(
                                            "${(analysis.confidence * 100).toInt()}%",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = when {
                                                analysis.confidence > 0.8f -> ErrorRed
                                                analysis.confidence > 0.5f -> WarningOrange
                                                else -> SuccessGreen
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = analysis.confidence,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        analysis.confidence > 0.8f -> ErrorRed
                                        analysis.confidence > 0.5f -> WarningOrange
                                        else -> SuccessGreen
                                    },
                                    trackColor = Neutral200
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = when (analysis.severity) {
                                "High" -> ErrorRed.copy(alpha = 0.1f)
                                "Medium" -> WarningOrange.copy(alpha = 0.1f)
                                else -> SuccessGreen.copy(alpha = 0.1f)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Severity Level",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Neutral600
                                )
                                Badge(
                                    containerColor = when (analysis.severity) {
                                        "High" -> ErrorRed
                                        "Medium" -> WarningOrange
                                        else -> SuccessGreen
                                    }
                                ) {
                                    Text(
                                        analysis.severity,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        if (analysis.description.isNotEmpty() && analysis.description != "No description available") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = Neutral100
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Description",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        analysis.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral700
                                    )
                                }
                            }
                        }

                        if (analysis.treatment.isNotEmpty() && analysis.treatment != "N/A") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = SuccessGreen.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Treatment",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        analysis.treatment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral700
                                    )
                                }
                            }
                        }

                        if (analysis.preventiveMeasures.isNotEmpty() && analysis.preventiveMeasures != "N/A") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                color = InfoBlue.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Preventive Measures",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Neutral900,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        analysis.preventiveMeasures,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral700
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedImageUri != null && diseaseAnalysis != null) {
                        Button(
                            onClick = {
                                selectedImageUri = null
                                diseaseAnalysis = null
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green600),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Analyze Another")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close", color = Green600)
                    }
                }
            }
        }
    }
}