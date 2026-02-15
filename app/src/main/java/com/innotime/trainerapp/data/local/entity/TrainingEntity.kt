package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey
    val id: String,
    val date: Long,
    val description: String
)
