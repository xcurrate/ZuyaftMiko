package com.zuyaftmiko.rootsuspender

import android.content.Context

class ConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences("root_suspender", Context.MODE_PRIVATE)

    fun save(config: WatchConfig) {
        prefs.edit()
            .putString("suspendAlways", config.suspendAlways.joinToString("\n"))
            .putString("forceAlways", config.forceAlways.joinToString("\n"))
            .putString("triggerApps", config.triggerApps.joinToString("\n"))
            .putString("suspendOnTrigger", config.suspendOnTrigger.joinToString("\n"))
            .putString("unsuspendOnTriggerExit", config.unsuspendOnTriggerExit.joinToString("\n"))
            .putString("forceOnTrigger", config.forceOnTrigger.joinToString("\n"))
            .apply()
    }

    fun load(): WatchConfig = WatchConfig(
        suspendAlways = prefs.getString("suspendAlways", "").toList(),
        forceAlways = prefs.getString("forceAlways", "").toList(),
        triggerApps = prefs.getString("triggerApps", "").toList(),
        suspendOnTrigger = prefs.getString("suspendOnTrigger", "").toList(),
        unsuspendOnTriggerExit = prefs.getString("unsuspendOnTriggerExit", "").toList(),
        forceOnTrigger = prefs.getString("forceOnTrigger", "").toList()
    )

    private fun String?.toList(): Set<String> = this
        .orEmpty()
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()
}

data class WatchConfig(
    val suspendAlways: Set<String>,
    val forceAlways: Set<String>,
    val triggerApps: Set<String>,
    val suspendOnTrigger: Set<String>,
    val unsuspendOnTriggerExit: Set<String>,
    val forceOnTrigger: Set<String>
)
