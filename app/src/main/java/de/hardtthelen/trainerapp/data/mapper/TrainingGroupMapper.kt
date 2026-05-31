package de.hardtthelen.trainerapp.data.mapper

import de.hardtthelen.trainerapp.data.local.entity.TrainingGroupEntity
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.TrainingGroup

fun TrainingGroupEntity.toDomain(memberIds: List<AthleteId>): TrainingGroup {
    return TrainingGroup(
        id = id,
        name = name,
        memberIds = memberIds
    )
}

fun TrainingGroup.toEntity(): TrainingGroupEntity {
    return TrainingGroupEntity(
        id = id,
        name = name
    )
}
