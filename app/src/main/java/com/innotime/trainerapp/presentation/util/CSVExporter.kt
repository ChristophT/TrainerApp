package com.innotime.trainerapp.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.domain.model.Run
import com.innotime.trainerapp.domain.model.Training
import java.io.File

/**
 * Exports athlete training data to CSV format.
 */
object CSVExporter {

    /**
     * Generates a CSV string for a specific athlete's runs.
     *
     * CSV Format:
     * Date,Training,Duration,Note
     *
     * @param athlete The athlete whose data to export
     * @param runs List of runs for the athlete
     * @param trainings List of all trainings to get training descriptions
     * @return CSV string
     */
    fun generateAthleteCSV(
        athlete: Athlete,
        runs: List<Run>,
        trainings: List<Training>
    ): String {
        val trainingMap = trainings.associateBy { it.id }
        val sb = StringBuilder()

        // Header
        sb.appendLine("Date,Training,Duration,Note")

        // Data rows (sorted by date, newest first)
        runs
            .filter { it.durationMs != null }
            .sortedByDescending { it.startedAt }
            .forEach { run ->
                val training = trainingMap[run.trainingId]
                val date = formatDate(run.startedAt)
                val trainingDesc = training?.description ?: "Unknown"
                val duration = formatDuration(run.durationMs!!)
                val note = run.note.replace("\"", "\"\"") // Escape quotes

                sb.appendLine("\"$date\",\"$trainingDesc\",\"$duration\",\"$note\"")
            }

        return sb.toString()
    }

    /**
     * Exports athlete data to CSV and shares it via Android share sheet.
     *
     * @param context Android context
     * @param athlete The athlete whose data to export
     * @param runs List of runs for the athlete
     * @param trainings List of all trainings
     */
    fun exportAndShare(
        context: Context,
        athlete: Athlete,
        runs: List<Run>,
        trainings: List<Training>
    ) {
        val csv = generateAthleteCSV(athlete, runs, trainings)
        val fileName = "${athlete.name.replace(" ", "_")}_results.csv"

        // Write to cache directory
        val file = File(context.cacheDir, fileName)
        file.writeText(csv)

        // Create content URI
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Share via intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Training Results - ${athlete.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))
    }
}
