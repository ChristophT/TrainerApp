package com.innotime.trainerapp.domain.model

/**
 * Represents an active (ongoing) run that hasn't been stopped yet.
 * This is kept in memory only and not persisted until the run is stopped.
 */
data class ActiveRun(
    val id: String,
    val athleteId: String,
    val trainingId: String,
    val startedAt: Long,           // Wall-clock: System.currentTimeMillis()
    val startMs: Long,             // Precision timer: SystemClock.elapsedRealtime()
    val note: String = ""
)
