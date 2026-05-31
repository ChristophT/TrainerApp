package de.hardtthelen.trainerapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class Training(
    val id: TrainingId,
    val date: Long,                // Timestamp in milliseconds
    val description: String,
    val participantIds: List<AthleteId>,
    val runIds: List<RunId>
)

@Parcelize
@JvmInline
value class TrainingId(val value: String) : Parcelable {
    companion object {
        fun newId(): TrainingId = TrainingId(UUID.randomUUID().toString())
    }
}