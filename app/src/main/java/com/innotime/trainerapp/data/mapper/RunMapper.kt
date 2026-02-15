package com.innotime.trainerapp.data.mapper

import com.innotime.trainerapp.data.local.entity.RunEntity
import com.innotime.trainerapp.domain.model.Run

fun RunEntity.toDomain(): Run {
    return Run(
        id = id,
        athleteId = athleteId,
        trainingId = trainingId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        durationMs = durationMs,
        note = note
    )
}

fun Run.toEntity(): RunEntity {
    return RunEntity(
        id = id,
        athleteId = athleteId,
        trainingId = trainingId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        durationMs = durationMs,
        note = note
    )
}
