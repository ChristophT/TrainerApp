package com.innotime.trainerapp.domain.model

data class TrainingGroup(
    val id: String,
    val name: String,
    val memberIds: List<String>
)
