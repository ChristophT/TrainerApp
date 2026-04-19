package com.innotime.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.innotime.trainerapp.domain.model.AthleteId

@Entity(tableName = "athletes")
data class AthleteEntity(
    @PrimaryKey
    val id: AthleteId,
    val name: String
)
