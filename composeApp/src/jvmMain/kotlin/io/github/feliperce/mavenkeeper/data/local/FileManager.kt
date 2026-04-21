package io.github.feliperce.mavenkeeper.data.local

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path

object FileManager {
    fun open(path: Path): Result<Unit> = runCatching {
        if (!Files.exists(path)) error("Path not found: $path")
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(path.toFile())
        } else {
            error("Opening files is not supported on this platform")
        }
    }
}
