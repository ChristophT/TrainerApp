package de.hardtthelen.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.TrainingId

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
    val id: RunId,
    val athleteId: AthleteId,
    val trainingId: TrainingId,
    val startedAt: Long,
    val finishedAt: Long?,
    val durationMs: Long?,
    val note: String
)
