package de.hardtthelen.trainerapp.data.mapper

import de.hardtthelen.trainerapp.data.local.entity.AthleteEntity
import de.hardtthelen.trainerapp.domain.model.Athlete

fun AthleteEntity.toDomain(): Athlete {
    return Athlete(
        id = id,
        name = name
    )
}

fun Athlete.toEntity(): AthleteEntity {
    return AthleteEntity(
        id = id,
        name = name
    )
}
