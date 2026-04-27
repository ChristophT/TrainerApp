package com.innotime.trainerapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class Run(
    val id: RunId,
    val athleteId: AthleteId,
    val trainingId: String,
    val startedAt: Long,           // Wall-clock timestamp (System.currentTimeMillis())
    val finishedAt: Long?,         // Wall-clock timestamp when stopped
    val durationMs: Long?,         // Precise duration in milliseconds
    val note: String
)

@Parcelize
@JvmInline
value class RunId(val value: String) : Parcelable {
    companion object {
        fun newId(): RunId = RunId(UUID.randomUUID().toString())
    }
}