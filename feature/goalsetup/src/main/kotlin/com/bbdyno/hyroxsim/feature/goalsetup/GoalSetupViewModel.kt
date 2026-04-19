package com.bbdyno.hyroxsim.feature.goalsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.PacePlan
import com.bbdyno.hyroxsim.core.domain.PacePlanner
import com.bbdyno.hyroxsim.core.domain.PaceReference
import com.bbdyno.hyroxsim.core.domain.StationKind
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.Goal
import com.bbdyno.hyroxsim.core.persistence.repository.GoalRepository
import com.bbdyno.hyroxsim.core.persistence.repository.TemplateRepository
import com.bbdyno.hyroxsim.sync.garmin.GarminBridge
import com.bbdyno.hyroxsim.sync.garmin.GarminGoalSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Goal setup UI state. `plan` is the data-driven projection (percentile,
 * per-run fatigue curve, per-station averages from race data); when
 * `plan == null` we fall back to the coarse linear split from PaceReference.
 */
data class GoalSetupUiState(
    val template: WorkoutTemplate? = null,
    val totalSeconds: Int = 75 * 60,
    val plan: PacePlan? = null,
    val tier: String? = null,
    val perSegmentSeconds: List<Int> = emptyList(),
    /** True when a watch is currently paired — controls "Save + Sync" vs "Save only" button label. */
    val isPaired: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null,
)

/**
 * Goal setup.
 *
 * - Total target → [PacePlanner.computePlan] gives percentile, tier,
 *   8-lap adaptive run split, and per-station averages from real race data.
 * - On Save: persists locally; if a watch is paired, also pushes the
 *   goal to Garmin so the delta badge is live from the first tick.
 */
@HiltViewModel
class GoalSetupViewModel @Inject constructor(
    private val templates: TemplateRepository,
    private val goals: GoalRepository,
    private val garminSync: GarminGoalSyncService,
    private val bridge: GarminBridge,
    private val planner: PacePlanner,
) : ViewModel() {

    private val _ui = MutableStateFlow(GoalSetupUiState(isPaired = bridge.isPaired))
    val ui: StateFlow<GoalSetupUiState> = _ui.asStateFlow()

    fun load(templateId: String) {
        viewModelScope.launch {
            val template = templates.findById(templateId)
                ?: HyroxDivision.fromRaw(templateId.removePrefix(BUILTIN_PREFIX))?.let {
                    WorkoutTemplate.hyroxPreset(it)
                }
                ?: return@launch
            val existing = goals.find(template.id)
            val totalSec = (existing?.targetTotalMs?.div(1000)?.toInt())
                ?: template.division?.let(PaceReference::referenceTotalSeconds)
                ?: (80 * 60)
            _ui.value = recompute(template, totalSec)
        }
    }

    companion object {
        /** Route key prefix for goal setup from built-in presets (no DB row). */
        const val BUILTIN_PREFIX = "builtin:"
    }

    fun onTotalChanged(newTotalSec: Int) {
        val template = _ui.value.template ?: return
        _ui.value = recompute(template, newTotalSec)
    }

    fun refreshPairing() {
        _ui.value = _ui.value.copy(isPaired = bridge.isPaired)
    }

    fun onSave(onDone: () -> Unit) {
        val state = _ui.value
        val template = state.template ?: return
        viewModelScope.launch {
            _ui.value = state.copy(saving = true, message = null)
            runCatching {
                val perSegMs = state.perSegmentSeconds.map { it.toLong() * 1000 }
                goals.save(
                    Goal(
                        templateId = template.id,
                        targetTotalMs = state.totalSeconds.toLong() * 1000,
                        targetSegmentsMs = perSegMs,
                    )
                )

                val syncResult = template.division?.let { division ->
                    garminSync.sendGoal(
                        division = division,
                        templateName = template.name,
                        targetTotalMs = state.totalSeconds.toLong() * 1000,
                        targetSegmentsMs = perSegMs,
                    )
                } ?: GarminGoalSyncService.SyncResult.NotPaired
                syncResult
            }.onSuccess { syncResult ->
                val msg = when (syncResult) {
                    GarminGoalSyncService.SyncResult.Sent -> "Saved + synced to Garmin"
                    GarminGoalSyncService.SyncResult.NotPaired -> "Saved (watch not paired)"
                    GarminGoalSyncService.SyncResult.Failed -> "Saved locally — Garmin push failed"
                }
                _ui.value = _ui.value.copy(saving = false, message = msg)
                onDone()
            }.onFailure { err ->
                _ui.value = _ui.value.copy(saving = false, message = "Save failed: ${err.message}")
            }
        }
    }

    // MARK: - Internal

    private fun recompute(template: WorkoutTemplate, totalSec: Int): GoalSetupUiState {
        val division = template.division
        val plan = division?.let { planner.computePlan(totalSec, it, PacePlanner.RunMode.Adaptive) }
        val perSeg = plan?.let { resolvePerSegment(template, it) }
            ?: PaceReference.defaultTargetSegmentsSeconds(template, totalSec)
        return GoalSetupUiState(
            template = template,
            totalSeconds = totalSec,
            plan = plan,
            tier = plan?.let { PacePlanner.tier(it.percentile) },
            perSegmentSeconds = perSeg,
            isPaired = bridge.isPaired,
            saving = false,
            message = _ui.value.message,
        )
    }

    /**
     * Map the computed plan onto the template's actual segment list.
     *
     * Plan's `runTimes[i]` = Run + surrounding ROX zone budget (how
     * `targetRun = goalTotalS - stnTotal` resolves). We split each lap's
     * total using `plan.roxFraction` and distribute the rox portion evenly
     * across all ROX zone segments of that lap.
     *
     * Lap boundaries are "Run → next Run (or end of list)", so this works
     * for the canonical 31-segment layout as well as custom templates.
     */
    private fun resolvePerSegment(template: WorkoutTemplate, plan: PacePlan): List<Int> {
        val segments = template.segments
        val result = IntArray(segments.size)

        // Walk segments grouping by lap (Run..next Run exclusive).
        var lap = -1
        var currentLapStart = -1

        fun flushLap(lapEndExclusive: Int) {
            if (currentLapStart < 0) return
            val lapRange = currentLapStart until lapEndExclusive
            val runTotal = plan.runTimes.getOrNull(lap) ?: 0
            val (runPure, roxBudget) = plan.splitRunRox(runTotal)
            val roxCount = lapRange.count {
                segments[it].type == com.bbdyno.hyroxsim.core.domain.SegmentType.RoxZone
            }
            val perRox = if (roxCount > 0) roxBudget / roxCount else 0
            for (idx in lapRange) {
                result[idx] = when (segments[idx].type) {
                    com.bbdyno.hyroxsim.core.domain.SegmentType.Run -> runPure
                    com.bbdyno.hyroxsim.core.domain.SegmentType.RoxZone -> perRox
                    com.bbdyno.hyroxsim.core.domain.SegmentType.Station -> {
                        val raw = segments[idx].stationKind?.raw
                        plan.stationTimes[raw] ?: (plan.stationTotal / 8)
                    }
                }
            }
        }

        for ((i, seg) in segments.withIndex()) {
            if (seg.type == com.bbdyno.hyroxsim.core.domain.SegmentType.Run) {
                flushLap(i)
                lap++
                currentLapStart = i
            }
        }
        flushLap(segments.size)
        return result.toList()
    }
}
