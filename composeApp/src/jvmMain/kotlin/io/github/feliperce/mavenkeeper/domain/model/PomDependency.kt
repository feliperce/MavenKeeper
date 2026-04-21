package io.github.feliperce.mavenkeeper.domain.model

data class PomDependency(
    val coordinate: MavenCoordinate,
    val scope: Scope = Scope.COMPILE,
    val optional: Boolean = false,
) {
    enum class Scope {
        COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT;

        companion object {
            fun fromXml(value: String?): Scope = when (value?.lowercase()) {
                "provided" -> PROVIDED
                "runtime" -> RUNTIME
                "test" -> TEST
                "system" -> SYSTEM
                "import" -> IMPORT
                else -> COMPILE
            }
        }
    }
}
