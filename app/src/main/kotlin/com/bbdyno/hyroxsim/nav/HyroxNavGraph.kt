package com.bbdyno.hyroxsim.nav

/**
 * Centralised route identifiers. Single source of truth so feature modules
 * don't duplicate magic strings.
 */
object Route {
    const val HOME = "home"
    const val BUILDER = "builder"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val GOAL_SETUP = "goal_setup/{templateId}"
    const val ACTIVE_WORKOUT = "active/{divisionRaw}"
    const val ACTIVE_WORKOUT_FROM_TEMPLATE = "active_template/{templateId}"
    const val SUMMARY = "summary/{workoutId}"

    fun activeWorkout(divisionRaw: String) = "active/$divisionRaw"
    fun activeTemplate(templateId: String) = "active_template/$templateId"
    fun summary(workoutId: String) = "summary/$workoutId"
    fun goalSetup(templateId: String) = "goal_setup/$templateId"
}
