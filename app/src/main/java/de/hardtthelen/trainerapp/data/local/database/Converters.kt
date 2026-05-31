package de.hardtthelen.trainerapp.data.local.database

import androidx.room.TypeConverter
import de.hardtthelen.trainerapp.domain.model.AthleteId

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromAthleteId(athleteId: AthleteId?): String? = athleteId?.value

    @TypeConverter
    @JvmStatic
    fun toAthleteId(value: String?): AthleteId? = value?.let { AthleteId(it) }
}