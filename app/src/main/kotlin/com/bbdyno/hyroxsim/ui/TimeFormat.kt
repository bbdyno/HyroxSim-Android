package com.bbdyno.hyroxsim.ui

/** Formats milliseconds as "MM:SS" or "H:MM:SS" for long sessions. */
fun formatElapsedMs(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

/** Signed delta formatter. Positive values are printed with a leading "+". */
fun formatDeltaMs(ms: Long): String {
    val sign = if (ms >= 0) "+" else "-"
    val abs = if (ms >= 0) ms else -ms
    return sign + formatElapsedMs(abs)
}
