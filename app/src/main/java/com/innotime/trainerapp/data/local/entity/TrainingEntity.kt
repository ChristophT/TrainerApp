package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.innotime.trainerapp.domain.model.TrainingId

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey
    val id: TrainingId,
    val date: Long,
    val description: String
)
