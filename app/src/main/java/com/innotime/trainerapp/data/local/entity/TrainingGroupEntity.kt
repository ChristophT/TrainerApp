package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_groups")
data class TrainingGroupEntity(
    @PrimaryKey
    val id: String,
    val name: String
)
