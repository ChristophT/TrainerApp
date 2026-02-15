package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "runs",
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrainingEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("athleteId"),
        Index("trainingId")
    ]
)
data class RunEntity(
    @PrimaryKey
    val id: String,
    val athleteId: String,
    val trainingId: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val durationMs: Long?,
    val note: String
)
