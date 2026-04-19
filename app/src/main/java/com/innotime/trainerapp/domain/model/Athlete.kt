package com.innotime.trainerapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class Athlete(
    val id: AthleteId,
    val name: String
)

@Parcelize
@JvmInline
value class AthleteId(val value: String) : Parcelable {
    companion object {
        fun newId(): AthleteId = AthleteId(UUID.randomUUID().toString())
    }
}