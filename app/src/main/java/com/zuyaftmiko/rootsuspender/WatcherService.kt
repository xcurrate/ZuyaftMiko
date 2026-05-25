package com.zuyaftmiko.rootsuspender

import android.app.Service
import android.content.Intent
import android.app.usage.UsageStatsManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class WatcherService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var store: ConfigStore
    private var triggerActive = false
    private var lastHandledPackage: String? = null

    private val watcher = object : Runnable {
        override fun run() {
            val config = store.load()
            val inTrigger = isAnyTriggerProcessRunning(config)
            val foreground = getForegroundPackage()

            RootOps.runCommands(
                RootOps.buildSuspendCommands(config.suspendAlways) +
                    RootOps.buildForceStopCommands(config.forceAlways)
            )

            if (inTrigger && !triggerActive) {
                val cmds = RootOps.buildSuspendCommands(config.suspendOnTrigger) +
                    RootOps.buildForceStopCommands(config.forceOnTrigger)
                RootOps.runCommands(cmds)
                triggerActive = true
            }

            if (!inTrigger && triggerActive) {
                RootOps.runCommands(RootOps.buildUnsuspendCommands(config.unsuspendOnTriggerExit))
                triggerActive = false
            }

            handler.postDelayed(this, 2000)
            if (foreground != null && config.triggerApps.contains(foreground) && foreground != lastHandledPackage) {
                val cmds = RootOps.buildSuspendCommands(config.suspendOnTrigger) +
                    RootOps.buildForceStopCommands(config.forceOnTrigger)
                RootOps.runCommands(cmds)
                lastHandledPackage = foreground
            }

            if (foreground == null || !config.triggerApps.contains(foreground)) {
                lastHandledPackage = null
            }

            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        store = ConfigStore(this)
        handler.post(watcher)
    }

    override fun onDestroy() {
        handler.removeCallbacks(watcher)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isAnyTriggerProcessRunning(config: WatchConfig): Boolean {
        if (config.triggerApps.isEmpty()) return false
        return config.triggerApps.any { pkg -> RootOps.isProcessRunning(pkg) }
    private fun getForegroundPackage(): String? {
        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 10_000
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
