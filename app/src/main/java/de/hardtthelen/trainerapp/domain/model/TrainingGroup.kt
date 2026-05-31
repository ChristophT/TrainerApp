package de.hardtthelen.trainerapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class TrainingGroup(
    val id: TrainingGroupId,
    val name: String,
    val memberIds: List<AthleteId>
)

@Parcelize
@JvmInline
value class TrainingGroupId(val value: String) : Parcelable {
    companion object {
        fun newId(): TrainingGroupId = TrainingGroupId(UUID.randomUUID().toString())
    }
}