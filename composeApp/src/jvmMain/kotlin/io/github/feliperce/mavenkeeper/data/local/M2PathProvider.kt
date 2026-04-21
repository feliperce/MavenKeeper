package io.github.feliperce.mavenkeeper.data.local

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

class M2PathProvider {
    private var override: Path? = null

    fun resolve(): Path {
        override?.let { return it }
        val m2Home = System.getenv("M2_HOME")
        if (!m2Home.isNullOrBlank()) {
            val fromEnv = Path(m2Home).resolve("repository")
            if (Files.exists(fromEnv)) return fromEnv
        }
        return Path(System.getProperty("user.home")).resolve(".m2").resolve("repository")
    }

    fun exists(path: Path = resolve()): Boolean = Files.isDirectory(path)

    fun setOverride(path: Path?) {
        override = path
    }

    fun currentOverride(): Path? = override
}
