package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athletes")
data class AthleteEntity(
    @PrimaryKey
    val id: String,
    val name: String
)
