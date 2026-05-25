package com.zuyaftmiko.rootsuspender

object RootOps {
    fun runCommands(commands: List<String>): String {
        if (commands.isEmpty()) return "Tidak ada command dijalankan"
        return try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.bufferedWriter().use { writer ->
                commands.forEach { writer.write("$it\n") }
                writer.write("exit\n")
                writer.flush()
            }
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            "exit=$code\n$output\n$error"
        } catch (e: Exception) {
            "Gagal: ${e.message}"
        }
    }

    fun runCommand(command: String): RootCommandResult {
        return try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.bufferedWriter().use { writer ->
                writer.write("$command\n")
                writer.write("exit\n")
                writer.flush()
            }
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            RootCommandResult(code, output, error)
        } catch (e: Exception) {
            RootCommandResult(-1, "", e.message.orEmpty())
        }
    }

    fun isProcessRunning(packageName: String): Boolean {
        val escaped = packageName.replace("\"", "\\\"")
        val pidof = runCommand("pidof \"$escaped\"")
        if (pidof.exitCode == 0 && pidof.stdout.trim().isNotEmpty()) return true

        val ps = runCommand("ps -A | grep -F \"$escaped\"")
        return ps.exitCode == 0 && ps.stdout
            .lineSequence()
            .any { line ->
                val cols = line.trim().split(Regex("\\s+"))
                cols.isNotEmpty() && cols.lastOrNull()?.contains(packageName) == true
            }
    }

    fun buildSuspendCommands(packages: Set<String>): List<String> =
        packages.map { "pm suspend $it" }

    fun buildUnsuspendCommands(packages: Set<String>): List<String> =
        packages.map { "pm unsuspend $it" }

    fun buildForceStopCommands(packages: Set<String>): List<String> =
        packages.map { "am force-stop $it" }
}

data class RootCommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)
    fun buildSuspendCommands(packages: Set<String>): List<String> =
        packages.map { "pm suspend $it" }

    fun buildForceStopCommands(packages: Set<String>): List<String> =
        packages.map { "am force-stop $it" }
}
