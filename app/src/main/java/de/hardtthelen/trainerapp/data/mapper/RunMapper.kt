package de.hardtthelen.trainerapp.data.mapper

import de.hardtthelen.trainerapp.data.local.entity.RunEntity
import de.hardtthelen.trainerapp.domain.model.Run

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
