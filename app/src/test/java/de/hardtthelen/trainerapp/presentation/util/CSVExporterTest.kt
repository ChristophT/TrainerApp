package de.hardtthelen.trainerapp.presentation.util

import com.google.common.truth.Truth.assertThat
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingId
import org.junit.Test

/**
 * Unit tests for [CSVExporter].
 */
class CSVExporterTest {

    @Test
    fun `generateSessionCSV with empty runs returns only header`() {
        val training = Training(
            id = TrainingId("t1"),
            date = 1770840000000L,
            description = "Intervals",
            participantIds = emptyList(),
            runIds = emptyList()
        )
        val athletes = emptyList<Athlete>()
        val runs = emptyList<Run>()

        val csv = CSVExporter.generateSessionCSV(runs, athletes, training)

        assertThat(csv.trim()).isEqualTo("Date,Training,Athlete,Duration,Note")
    }

    @Test
    fun `generateSessionCSV exports runs correctly and sorted by date`() {
        val athlete1 = Athlete(AthleteId("a1"), "John Doe")
        val athlete2 = Athlete(AthleteId("a2"), "Jane Smith")
        val training = Training(
            id = TrainingId("t1"),
            date = 1770840000000L,
            description = "Sprint session",
            participantIds = listOf(athlete1.id, athlete2.id),
            runIds = listOf(RunId("r1"), RunId("r2"))
        )
        
        val run1 = Run(
            id = RunId("r1"),
            athleteId = athlete1.id,
            trainingId = training.id,
            startedAt = 1000L,
            finishedAt = null,
            durationMs = 12340L,
            note = "Good run"
        )
        val run2 = Run(
            id = RunId("r2"),
            athleteId = athlete2.id,
            trainingId = training.id,
            startedAt = 2000L,
            finishedAt = null,
            durationMs = 15000L,
            note = "Slow start"
        )
        
        val csv = CSVExporter.generateSessionCSV(listOf(run1, run2), listOf(athlete1, athlete2), training)
        
        val lines = csv.lines().filter { it.isNotBlank() }
        assertThat(lines).hasSize(3) // Header + 2 rows
        assertThat(lines[0]).isEqualTo("Date,Training,Athlete,Duration,Note")
        
        // run2 should be first because it started later (2000L > 1000L)
        // Format: "Date","Training","Athlete","Duration","Note"
        assertThat(lines[1]).contains("\"Jane Smith\"")
        assertThat(lines[1]).contains("\"Sprint session\"")
        assertThat(lines[1]).contains("\"15.00\"")
        assertThat(lines[1]).contains("\"Slow start\"")

        assertThat(lines[2]).contains("\"John Doe\"")
        assertThat(lines[2]).contains("\"Sprint session\"")
        assertThat(lines[2]).contains("\"12.34\"")
        assertThat(lines[2]).contains("\"Good run\"")
    }

    @Test
    fun `generateSessionCSV filters out runs without duration`() {
        val athlete = Athlete(AthleteId("a1"), "John Doe")
        val training = Training(TrainingId("t1"), 0L, "Test", listOf(athlete.id), listOf(RunId("r1"), RunId("r2")))
        
        val runWithDuration = Run(RunId("r1"), athlete.id, training.id, 1000L, null, 5000L, "Finished")
        val runWithoutDuration = Run(RunId("r2"), athlete.id, training.id, 2000L, null, null, "Not finished")
        
        val csv = CSVExporter.generateSessionCSV(listOf(runWithDuration, runWithoutDuration), listOf(athlete), training)
        
        val lines = csv.lines().filter { it.isNotBlank() }
        assertThat(lines).hasSize(2) // Header + 1 row
        assertThat(lines[1]).contains("\"Finished\"")
        assertThat(csv).doesNotContain("Not finished")
    }

    @Test
    fun `generateSessionCSV escapes quotes in notes`() {
        val athlete = Athlete(AthleteId("a1"), "John Doe")
        val training = Training(TrainingId("t1"), 0L, "Test", listOf(athlete.id), listOf(RunId("r1")))
        
        val run = Run(RunId("r1"), athlete.id, training.id, 1000L, null, 5000L, "Said \"Hello\"")
        
        val csv = CSVExporter.generateSessionCSV(listOf(run), listOf(athlete), training)
        
        assertThat(csv).contains("\"Said \"\"Hello\"\"\"")
    }

    @Test
    fun `generateSessionCSV handles unknown athletes`() {
        val training = Training(TrainingId("t1"), 0L, "Test", emptyList(), listOf(RunId("r1")))
        val run = Run(RunId("r1"), AthleteId("unknown"), training.id, 1000L, null, 5000L, "")
        
        val csv = CSVExporter.generateSessionCSV(listOf(run), emptyList(), training)
        
        assertThat(csv).contains("\"Unknown athlete\"")
    }
}
