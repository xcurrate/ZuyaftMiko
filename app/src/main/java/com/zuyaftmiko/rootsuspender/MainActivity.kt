package com.zuyaftmiko.rootsuspender

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var store: ConfigStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = ConfigStore(this)

        val etSuspendAlways = findViewById<EditText>(R.id.etSuspendAlways)
        val etForceAlways = findViewById<EditText>(R.id.etForceAlways)
        val etTriggerApps = findViewById<EditText>(R.id.etTriggerApps)
        val etSuspendOnTrigger = findViewById<EditText>(R.id.etSuspendOnTrigger)
        val etUnsuspendOnExit = findViewById<EditText>(R.id.etUnsuspendOnExit)
        val etForceOnTrigger = findViewById<EditText>(R.id.etForceOnTrigger)
        val tvLog = findViewById<TextView>(R.id.tvLog)

        val loaded = store.load()
        etSuspendAlways.setText(loaded.suspendAlways.joinToString("\n"))
        etForceAlways.setText(loaded.forceAlways.joinToString("\n"))
        etTriggerApps.setText(loaded.triggerApps.joinToString("\n"))
        etSuspendOnTrigger.setText(loaded.suspendOnTrigger.joinToString("\n"))
        etUnsuspendOnExit.setText(loaded.unsuspendOnTriggerExit.joinToString("\n"))
        etForceOnTrigger.setText(loaded.forceOnTrigger.joinToString("\n"))

        fun readConfig(): WatchConfig {
            val config = WatchConfig(
                suspendAlways = etSuspendAlways.text.toString().toSetLines(),
                forceAlways = etForceAlways.text.toString().toSetLines(),
                triggerApps = etTriggerApps.text.toString().toSetLines(),
                suspendOnTrigger = etSuspendOnTrigger.text.toString().toSetLines(),
                unsuspendOnTriggerExit = etUnsuspendOnExit.text.toString().toSetLines(),
                forceOnTrigger = etForceOnTrigger.text.toString().toSetLines()
            )
            store.save(config)
            return config
        }

        findViewById<Button>(R.id.btnRunNow).setOnClickListener {
            val c = readConfig()
            val allCmd = RootOps.buildSuspendCommands(c.suspendAlways) + RootOps.buildForceStopCommands(c.forceAlways)
            tvLog.text = RootOps.runCommands(allCmd)
        }

        findViewById<Button>(R.id.btnStartWatcher).setOnClickListener {
            readConfig()
            startService(Intent(this, WatcherService::class.java))
            tvLog.text = "Watcher started"
        }

        findViewById<Button>(R.id.btnStopWatcher).setOnClickListener {
            stopService(Intent(this, WatcherService::class.java))
            tvLog.text = "Watcher stopped"
        }

        findViewById<Button>(R.id.btnUsageAccess).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
    }

    private fun String.toSetLines(): Set<String> =
        lines().map { it.trim() }.filter { it.isNotBlank() }.toSet()
}
