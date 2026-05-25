package com.zuyaftmiko.rootsuspender

object RootOps {
    fun runCommands(commands: List<String>): String {
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

    fun buildSuspendCommands(packages: Set<String>): List<String> =
        packages.map { "pm suspend $it" }

    fun buildForceStopCommands(packages: Set<String>): List<String> =
        packages.map { "am force-stop $it" }
}
