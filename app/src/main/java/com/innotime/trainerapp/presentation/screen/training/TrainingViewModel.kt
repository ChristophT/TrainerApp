package com.innotime.trainerapp.presentation.screen.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.innotime.trainerapp.domain.model.ActiveRun
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.domain.model.Run
import com.innotime.trainerapp.domain.model.Training
import com.innotime.trainerapp.domain.model.TrainingGroup
import com.innotime.trainerapp.domain.repository.AthleteRepository
import com.innotime.trainerapp.domain.repository.GroupRepository
import com.innotime.trainerapp.domain.repository.RunRepository
import com.innotime.trainerapp.domain.repository.TrainingRepository
import com.innotime.trainerapp.presentation.util.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Shared ViewModel that manages all training-related state.
 * Mirrors the React Context pattern from the web app.
 */
@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val athleteRepository: AthleteRepository,
    private val runRepository: RunRepository,
    private val trainingRepository: TrainingRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    // ========== State ==========

    val athletes: StateFlow<List<Athlete>> = athleteRepository.getAllAthletes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val groups: StateFlow<List<TrainingGroup>> = groupRepository.getAllGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trainings: StateFlow<List<Training>> = trainingRepository.getAllTrainings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTraining = MutableStateFlow<Training?>(null)
    val currentTraining: StateFlow<Training?> = _currentTraining.asStateFlow()

    private val _activeRuns = MutableStateFlow<List<ActiveRun>>(emptyList())
    val activeRuns: StateFlow<List<ActiveRun>> = _activeRuns.asStateFlow()

    // Real-time timer updates (60fps)
    val currentElapsedTimes: StateFlow<Map<String, Long>> = flow {
        while (true) {
            emit(Unit)
            delay(16) // ~60fps
        }
    }.combine(_activeRuns) { _, activeRuns ->
        val currentMs = TimerManager.now()
        activeRuns.associate { run ->
            run.athleteId to (currentMs - run.startMs)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // ========== Athlete CRUD ==========

    fun addAthlete(name: String) {
        viewModelScope.launch {
            val athlete = Athlete(
                id = UUID.randomUUID().toString(),
                name = name
            )
            athleteRepository.addAthlete(athlete)
        }
    }

    fun updateAthlete(id: String, name: String) {
        viewModelScope.launch {
            val athlete = athleteRepository.getAthleteById(id) ?: return@launch
            athleteRepository.updateAthlete(athlete.copy(name = name))
        }
    }

    fun deleteAthlete(id: String) {
        viewModelScope.launch {
            // Remove from current training
            _currentTraining.value?.let { training ->
                if (training.participantIds.contains(id)) {
                    val updated = training.copy(
                        participantIds = training.participantIds.filter { it != id }
                    )
                    trainingRepository.updateTraining(updated)
                    _currentTraining.value = updated
                }
            }

            // Stop and remove active run
            _activeRuns.value = _activeRuns.value.filter { it.athleteId != id }

            // Delete athlete (cascade will handle group memberships and runs)
            athleteRepository.deleteAthlete(id)
        }
    }

    // ========== Training Session ==========

    fun startTraining(description: String) {
        viewModelScope.launch {
            val training = Training(
                id = UUID.randomUUID().toString(),
                date = TimerManager.wallClockNow(),
                description = description,
                participantIds = emptyList(),
                runIds = emptyList()
            )
            trainingRepository.addTraining(training)
            _currentTraining.value = training
            _activeRuns.value = emptyList()
        }
    }

    fun endTraining() {
        viewModelScope.launch {
            // Stop all active runs and persist them
            _activeRuns.value.forEach { activeRun ->
                val durationMs = TimerManager.now() - activeRun.startMs
                val run = Run(
                    id = activeRun.id,
                    athleteId = activeRun.athleteId,
                    trainingId = activeRun.trainingId,
                    startedAt = activeRun.startedAt,
                    finishedAt = TimerManager.wallClockNow(),
                    durationMs = durationMs,
                    note = activeRun.note
                )
                runRepository.addRun(run)
            }

            _currentTraining.value = null
            _activeRuns.value = emptyList()
        }
    }

    fun addParticipant(athleteId: String) {
        viewModelScope.launch {
            val training = _currentTraining.value ?: return@launch
            if (training.participantIds.contains(athleteId)) return@launch

            trainingRepository.addParticipant(training.id, athleteId)

            val updated = training.copy(
                participantIds = training.participantIds + athleteId
            )
            _currentTraining.value = updated
        }
    }

    fun removeParticipant(athleteId: String) {
        viewModelScope.launch {
            val training = _currentTraining.value ?: return@launch

            // Stop and persist active run if exists
            _activeRuns.value.find { it.athleteId == athleteId }?.let { activeRun ->
                val durationMs = TimerManager.now() - activeRun.startMs
                val run = Run(
                    id = activeRun.id,
                    athleteId = activeRun.athleteId,
                    trainingId = activeRun.trainingId,
                    startedAt = activeRun.startedAt,
                    finishedAt = TimerManager.wallClockNow(),
                    durationMs = durationMs,
                    note = activeRun.note
                )
                runRepository.addRun(run)
            }

            _activeRuns.value = _activeRuns.value.filter { it.athleteId != athleteId }

            trainingRepository.removeParticipant(training.id, athleteId)

            val updated = training.copy(
                participantIds = training.participantIds.filter { it != athleteId }
            )
            _currentTraining.value = updated
        }
    }

    fun addGroupToTraining(groupId: String) {
        viewModelScope.launch {
            val training = _currentTraining.value ?: return@launch
            val group = groups.value.find { it.id == groupId } ?: return@launch

            val newIds = group.memberIds.filter { !training.participantIds.contains(it) }
            if (newIds.isEmpty()) return@launch

            newIds.forEach { athleteId ->
                trainingRepository.addParticipant(training.id, athleteId)
            }

            val updated = training.copy(
                participantIds = training.participantIds + newIds
            )
            _currentTraining.value = updated
        }
    }

    // ========== Runs ==========

    fun startRun(athleteId: String) {
        val training = _currentTraining.value ?: return
        if (_activeRuns.value.any { it.athleteId == athleteId }) return

        val activeRun = ActiveRun(
            id = UUID.randomUUID().toString(),
            athleteId = athleteId,
            trainingId = training.id,
            startedAt = TimerManager.wallClockNow(),
            startMs = TimerManager.now(),
            note = ""
        )
        _activeRuns.value = _activeRuns.value + activeRun
    }

    fun stopRun(athleteId: String) {
        viewModelScope.launch {
            val activeRun = _activeRuns.value.find { it.athleteId == athleteId } ?: return@launch
            val durationMs = TimerManager.now() - activeRun.startMs

            val run = Run(
                id = activeRun.id,
                athleteId = activeRun.athleteId,
                trainingId = activeRun.trainingId,
                startedAt = activeRun.startedAt,
                finishedAt = TimerManager.wallClockNow(),
                durationMs = durationMs,
                note = activeRun.note
            )
            runRepository.addRun(run)

            _currentTraining.value?.let { training ->
                val updated = training.copy(
                    runIds = training.runIds + run.id
                )
                trainingRepository.updateTraining(updated)
                _currentTraining.value = updated
            }

            _activeRuns.value = _activeRuns.value.filter { it.athleteId != athleteId }
        }
    }

    fun updateRunNote(runId: String, note: String) {
        viewModelScope.launch {
            // Update active run if exists
            val activeRunIndex = _activeRuns.value.indexOfFirst { it.id == runId }
            if (activeRunIndex >= 0) {
                val updated = _activeRuns.value.toMutableList()
                updated[activeRunIndex] = updated[activeRunIndex].copy(note = note)
                _activeRuns.value = updated
            }

            // Also update persisted run if exists
            runRepository.getRunById(runId)?.let { run ->
                runRepository.updateRun(run.copy(note = note))
            }
        }
    }

    fun getActiveRun(athleteId: String): ActiveRun? {
        return _activeRuns.value.find { it.athleteId == athleteId }
    }

    fun getCompletedRuns(athleteId: String): StateFlow<List<Run>> {
        val trainingId = _currentTraining.value?.id
        return if (trainingId != null) {
            runRepository.getRunsForTraining(trainingId)
                .combine(flow { emit(athleteId) }) { runs, id ->
                    runs.filter { it.athleteId == id && it.durationMs != null }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        } else {
            MutableStateFlow(emptyList())
        }
    }

    // ========== Groups ==========

    fun addGroup(name: String) {
        viewModelScope.launch {
            val group = TrainingGroup(
                id = UUID.randomUUID().toString(),
                name = name,
                memberIds = emptyList()
            )
            groupRepository.addGroup(group)
        }
    }

    fun updateGroup(id: String, name: String) {
        viewModelScope.launch {
            val group = groupRepository.getGroupById(id) ?: return@launch
            groupRepository.updateGroup(group.copy(name = name))
        }
    }

    fun deleteGroup(id: String) {
        viewModelScope.launch {
            groupRepository.deleteGroup(id)
        }
    }

    fun toggleGroupMember(groupId: String, athleteId: String) {
        viewModelScope.launch {
            val group = groups.value.find { it.id == groupId } ?: return@launch
            if (group.memberIds.contains(athleteId)) {
                groupRepository.removeMember(groupId, athleteId)
            } else {
                groupRepository.addMember(groupId, athleteId)
            }
        }
    }

    // ========== Results ==========

    fun getRunsForAthlete(athleteId: String): StateFlow<List<Run>> {
        return runRepository.getRunsForAthlete(athleteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getRunsForTraining(trainingId: String): StateFlow<List<Run>> {
        return runRepository.getRunsForTraining(trainingId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
