package com.example.agritwin.ai

data class CropPhenologyStage(
    val stage: String,
    val daysFromPlanting: Int,
    val waterRequirement: Double,
    val nitrogenRequirement: Double,
    val description: String,
    val riskFactors: List<String>,
    val recommendedActions: List<String>
)