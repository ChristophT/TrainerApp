package de.hardtthelen.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.hardtthelen.trainerapp.domain.model.TrainingId

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey
    val id: TrainingId,
    val date: Long,
    val description: String
)
