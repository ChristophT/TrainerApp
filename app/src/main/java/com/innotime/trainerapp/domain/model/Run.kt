package com.innotime.trainerapp.domain.model

data class Run(
    val id: String,
    val athleteId: String,
    val trainingId: String,
    val startedAt: Long,           // Wall-clock timestamp (System.currentTimeMillis())
    val finishedAt: Long?,         // Wall-clock timestamp when stopped
    val durationMs: Long?,         // Precise duration in milliseconds
    val note: String
)
