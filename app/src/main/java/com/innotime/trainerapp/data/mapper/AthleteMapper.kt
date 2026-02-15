package com.innotime.trainerapp.data.mapper

import com.innotime.trainerapp.data.local.entity.AthleteEntity
import com.innotime.trainerapp.domain.model.Athlete

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
