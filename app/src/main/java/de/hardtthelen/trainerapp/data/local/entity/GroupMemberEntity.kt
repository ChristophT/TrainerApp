package de.hardtthelen.trainerapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "athleteId"],
    foreignKeys = [
        ForeignKey(
            entity = TrainingGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId"),
        Index("athleteId")
    ]
)
data class GroupMemberEntity(
    val groupId: TrainingGroupId,
    val athleteId: AthleteId
)
