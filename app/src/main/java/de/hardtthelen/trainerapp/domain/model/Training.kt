package de.hardtthelen.trainerapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class Training(
    val id: TrainingId,
    val date: Long,                // Timestamp in milliseconds
    val description: String,
    val participants: List<TrainingParticipant>,
    val runIds: List<RunId>
) {
    fun participantsByDisplayOrder(): List<TrainingParticipant> {
        return participants.sortedBy { it.displayOrder }
    }

    fun participantIds(): List<AthleteId> {
        return participants.map { it.athlete.id }
    }
}

@Parcelize
@JvmInline
value class TrainingId(val value: String) : Parcelable {
    companion object {
        fun newId(): TrainingId = TrainingId(UUID.randomUUID().toString())
    }
}