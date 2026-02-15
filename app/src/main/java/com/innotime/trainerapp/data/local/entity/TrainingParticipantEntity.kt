package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "training_participants",
    primaryKeys = ["trainingId", "athleteId"],
    foreignKeys = [
        ForeignKey(
            entity = TrainingEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("trainingId"),
        Index("athleteId")
    ]
)
data class TrainingParticipantEntity(
    val trainingId: String,
    val athleteId: String
)
