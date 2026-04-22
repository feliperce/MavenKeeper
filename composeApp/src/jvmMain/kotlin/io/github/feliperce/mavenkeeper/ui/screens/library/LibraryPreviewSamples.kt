package io.github.feliperce.mavenkeeper.ui.screens.library

import io.github.feliperce.mavenkeeper.domain.model.Artifact
import io.github.feliperce.mavenkeeper.domain.model.ArtifactGroup
import io.github.feliperce.mavenkeeper.domain.model.GroupSummary
import io.github.feliperce.mavenkeeper.domain.model.MavenCoordinate
import io.github.feliperce.mavenkeeper.domain.model.PomDependency
import io.github.feliperce.mavenkeeper.domain.model.ScanProgress
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path

internal object LibraryPreviewSamples {
    private val repoRoot = Path("/fake/.m2/repository")

    fun artifact(
        groupId: String = "com.worldpackers.multiplatform",
        artifactId: String = "hosts-kmm",
        version: String = "0.15.7.7",
        sizeBytes: Long = 867_900,
        daysAgo: Long = 1,
        licenses: List<String> = listOf("Apache 2.0"),
        dependencies: List<PomDependency> = emptyList(),
    ): Artifact {
        val dir = repoRoot
            .resolve(groupId.replace('.', '/'))
            .resolve(artifactId)
            .resolve(version)
        return Artifact(
            coordinate = MavenCoordinate(groupId, artifactId, version),
            packaging = "jar",
            sizeBytes = sizeBytes,
            lastModified = Instant.now().minus(daysAgo, ChronoUnit.DAYS),
            versionDirectory = dir,
            pomPath = dir.resolve("$artifactId-$version.pom"),
            sha1 = "aabbccddee1122334455",
            licenses = licenses,
            dependencies = dependencies,
        )
    }

    fun group(
        groupId: String = "com.worldpackers.multiplatform",
        artifactId: String = "hosts-kmm",
        versions: List<String> = listOf("0.15.7.7", "0.15.7.4.1", "0.15.7.1.2"),
        dependencies: List<PomDependency> = emptyList(),
    ): ArtifactGroup = ArtifactGroup(
        groupId = groupId,
        artifactId = artifactId,
        versions = versions.mapIndexed { index, ver ->
            artifact(
                groupId = groupId,
                artifactId = artifactId,
                version = ver,
                daysAgo = 1L + index * 5L,
                dependencies = if (index == 0) dependencies else emptyList(),
            )
        },
    )

    fun sampleDependencies(): List<PomDependency> = listOf(
        PomDependency(MavenCoordinate("org.jetbrains.kotlin", "kotlin-stdlib", "2.0.20"), PomDependency.Scope.COMPILE),
        PomDependency(MavenCoordinate("io.ktor", "ktor-client-core", "3.0.0"), PomDependency.Scope.COMPILE),
        PomDependency(MavenCoordinate("junit", "junit", "4.13.2"), PomDependency.Scope.TEST),
        PomDependency(MavenCoordinate("com.google.code.findbugs", "jsr305", "3.0.2"), PomDependency.Scope.PROVIDED),
    )

    fun uiState(
        selected: Boolean = true,
        withDependencies: Boolean = true,
        query: String = "",
    ): LibraryUiState {
        val hostsGroup = group(
            dependencies = if (withDependencies) sampleDependencies() else emptyList(),
        )
        val kotlinGroup = group(
            groupId = "org.jetbrains.kotlin",
            artifactId = "kotlin-stdlib",
            versions = listOf("2.0.20", "2.0.10"),
        )
        val summaries = listOf(
            GroupSummary("com.worldpackers.multiplatform", 2, 55, 206_400_000L),
            GroupSummary("org.jetbrains.kotlin", 44, 44, 81_600_000L),
            GroupSummary("org.robolectric", 1, 3, 244_200_000L),
        )
        return LibraryUiState(
            query = query,
            allGroups = listOf(hostsGroup, kotlinGroup),
            groupSummaries = summaries,
            progress = ScanProgress.Complete(totalArtifacts = 68, totalGroups = 10),
            repositoryRoot = repoRoot,
            selectedArtifactKey = if (selected) "${hostsGroup.groupId}:${hostsGroup.artifactId}" else null,
        )
    }
}
