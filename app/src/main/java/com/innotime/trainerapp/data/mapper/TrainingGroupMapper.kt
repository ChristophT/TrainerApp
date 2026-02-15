package com.innotime.trainerapp.data.mapper

import com.innotime.trainerapp.data.local.entity.TrainingGroupEntity
import com.innotime.trainerapp.domain.model.TrainingGroup

fun TrainingGroupEntity.toDomain(memberIds: List<String>): TrainingGroup {
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
