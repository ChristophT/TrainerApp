package de.hardtthelen.trainerapp.presentation.screen.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hardtthelen.trainerapp.domain.model.ActiveRun
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingGroup
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.domain.repository.AthleteRepository
import de.hardtthelen.trainerapp.domain.repository.GroupRepository
import de.hardtthelen.trainerapp.domain.repository.RunRepository
import de.hardtthelen.trainerapp.domain.repository.TrainingRepository
import de.hardtthelen.trainerapp.presentation.util.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val runsPerTraining: StateFlow<Map<TrainingId, List<Run>>> = trainings
        .flatMapLatest { trainingList ->
            if (trainingList.isEmpty()) return@flatMapLatest flowOf(emptyMap())
            combine(trainingList.map { training ->
                runRepository.getRunsForTraining(training.id).map { runs -> training.id to runs }
            }) { pairs -> pairs.toMap() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val completedRunsByAthlete: StateFlow<Map<AthleteId, List<Run>>> =
        _currentTraining
            .flatMapLatest { training ->
                if (training != null) {
                    runRepository.getRunsForTraining(training.id)
                        .map { runs ->
                            runs.filter { it.durationMs != null }
                                .groupBy { it.athleteId }
                        }
                } else {
                    flowOf(emptyMap())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    // Real-time timer updates (60fps)
    val currentElapsedTimes: StateFlow<Map<AthleteId, Long>> = flow {
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
                id = AthleteId.newId(),
                name = name
            )
            athleteRepository.addAthlete(athlete)
        }
    }

    fun updateAthlete(id: AthleteId, name: String) {
        viewModelScope.launch {
            val athlete = athleteRepository.getAthleteById(id) ?: return@launch
            athleteRepository.updateAthlete(athlete.copy(name = name))
        }
    }

    fun deleteAthlete(id: AthleteId) {
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
                id = TrainingId.newId(),
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

    fun addParticipant(athleteId: AthleteId) {
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

    fun addGroupToTraining(groupId: TrainingGroupId) {
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

    fun startRun(athleteId: AthleteId) {
        val training = _currentTraining.value ?: return
        if (_activeRuns.value.any { it.athleteId == athleteId }) return

        val activeRun = ActiveRun(
            id = RunId.newId(),
            athleteId = athleteId,
            trainingId = training.id,
            startedAt = TimerManager.wallClockNow(),
            startMs = TimerManager.now(),
            note = ""
        )
        _activeRuns.value = _activeRuns.value + activeRun
    }

    fun stopRun(athleteId: AthleteId) {
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

    fun updateRunNote(runId: RunId, note: String) {
        viewModelScope.launch {
            // Update active run if exists
            val activeRunIndex = _activeRuns.value.indexOfFirst { it.id == runId }
            if (activeRunIndex >= 0) {
                val updated = _activeRuns.value.toMutableList()
                updated[activeRunIndex] = updated[activeRunIndex].copy(note = note)
                _activeRuns.value = updated
            }

            // Always try to update persisted run
            runRepository.getRunById(runId)?.let { run ->
                runRepository.updateRun(run.copy(note = note))
            }
        }
    }

    fun getActiveRun(athleteId: AthleteId): ActiveRun? {
        return _activeRuns.value.find { it.athleteId == athleteId }
    }

    // ========== Groups ==========

    fun addGroup(name: String) {
        viewModelScope.launch {
            val group = TrainingGroup(
                id = TrainingGroupId.newId(),
                name = name,
                memberIds = emptyList()
            )
            groupRepository.addGroup(group)
        }
    }

    fun updateGroup(id: TrainingGroupId, name: String) {
        viewModelScope.launch {
            val group = groupRepository.getGroupById(id) ?: return@launch
            groupRepository.updateGroup(group.copy(name = name))
        }
    }

    fun deleteGroup(id: TrainingGroupId) {
        viewModelScope.launch {
            groupRepository.deleteGroup(id)
        }
    }

    fun toggleGroupMember(groupId: TrainingGroupId, athleteId: AthleteId) {
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

    fun getRunsForAthlete(athleteId: AthleteId): StateFlow<List<Run>> {
        return runRepository.getRunsForAthlete(athleteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getRunsForTraining(trainingId: TrainingId): StateFlow<List<Run>> {
        return runRepository.getRunsForTraining(trainingId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun deleteFinishedTrainingSession(trainingId: TrainingId) {
        viewModelScope.launch {
            trainingRepository.deleteTraining(trainingId)
        }
    }
}