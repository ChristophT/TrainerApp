package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.innotime.trainerapp.domain.model.TrainingGroupId

@Entity(tableName = "training_groups")
data class TrainingGroupEntity(
    @PrimaryKey
    val id: TrainingGroupId,
    val name: String
)
