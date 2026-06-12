package de.hardtthelen.trainerapp.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.domain.model.Training
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Exports athlete training data to CSV format.
 */
object CSVExporter {

    /**
     * Generates a CSV string for a specific session's runs.
     *
     * CSV Format:
     * Date,Training description,Athlete name,Duration,Note
     *
     * @param runs      List of runs for the session
     * @param athletes  List of all athletes participating in the runs
     * @param training  the training session to export
     * @return CSV string
     */
    fun generateSessionCSV(
        runs: List<Run>,
        athletes: List<Athlete>,
        training: Training
    ): String {
        val sb = StringBuilder()
        val athleteMap = athletes.associateBy { it.id }

        // Header
        sb.appendLine("Date,Training,Athlete,Duration,Note")

        // Data rows (sorted by date, newest first)
        runs
            .filter { it.durationMs != null }
            .sortedByDescending { it.startedAt }
            .forEach { run ->
                val date = formatDate(run.startedAt)
                val trainingDesc = training.description
                val athleteName = athleteMap[run.athleteId]?.name ?: "Unknown athlete"
                val duration = formatDuration(run.durationMs!!)
                val note = run.note.replace("\"", "\"\"") // Escape quotes

                sb.appendLine("\"$date\",\"$trainingDesc\",\"$athleteName\",\"$duration\",\"$note\"")
            }

        return sb.toString()
    }

    /**
     * Exports session data to CSV and shares it via Android share sheet.
     *
     * @param context Android context
     * @param runs List of runs for the session
     * @param athletes  List of all athletes participating in the runs
     * @param training The exported training session
     */
    @OptIn(ExperimentalTime::class)
    fun exportAndShare(
        context: Context,
        runs: List<Run>,
        athletes: List<Athlete>,
        training: Training
    ) {
        val trainingDate = Instant.fromEpochMilliseconds(training.date)
        val trainingDateString = trainingDate.toString()
        val csv = generateSessionCSV(runs, athletes, training)
        val fileName = "${trainingDateString}_${training.description.replace(" ", "_")}_results.csv"

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
            putExtra(Intent.EXTRA_SUBJECT, "Training Results - ${training.description}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))
    }
}
