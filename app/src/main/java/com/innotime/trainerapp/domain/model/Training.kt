package com.innotime.trainerapp.domain.model

data class Training(
    val id: String,
    val date: Long,                // Timestamp in milliseconds
    val description: String,
    val participantIds: List<AthleteId>,
    val runIds: List<String>
)
