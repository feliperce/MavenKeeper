package io.github.feliperce.mavenkeeper.data.parser

import io.github.feliperce.mavenkeeper.domain.model.MavenCoordinate
import io.github.feliperce.mavenkeeper.domain.model.PomDependency
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class PomParser {
    private val factory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = false
    }

    // Resolves ${prop} references against <properties> declared in the same POM only.
    // Why: full Maven property resolution requires walking parent POMs and settings.xml,
    // which is out of scope for the local MVP — this handles the ~90% practical case.
    fun parse(pomPath: Path): ParsedPom? = runCatching {
        val doc = factory.newDocumentBuilder().parse(pomPath.toFile())
        val project = doc.documentElement

        val parent = project.childElement("parent")
        val parentGroupId = parent?.childText("groupId")
        val parentVersion = parent?.childText("version")

        val groupId = project.childText("groupId") ?: parentGroupId ?: return@runCatching null
        val artifactId = project.childText("artifactId") ?: return@runCatching null
        val version = project.childText("version") ?: parentVersion ?: return@runCatching null
        val packaging = project.childText("packaging") ?: "jar"

        val properties = project.childElement("properties")
            ?.childElements()
            ?.associate { it.tagName to (it.textContent ?: "") }
            .orEmpty()
            .toMutableMap().apply {
                this["project.groupId"] = groupId
                this["project.artifactId"] = artifactId
                this["project.version"] = version
                this["pom.version"] = version
            }

        val deps = project.childElement("dependencies")
            ?.childElements()
            ?.filter { it.tagName == "dependency" }
            ?.mapNotNull { it.toDependency(properties) }
            ?.toList()
            .orEmpty()

        val licenses = project.childElement("licenses")
            ?.childElements()
            ?.filter { it.tagName == "license" }
            ?.mapNotNull { it.childText("name")?.resolve(properties) }
            ?.toList()
            .orEmpty()

        ParsedPom(
            coordinate = MavenCoordinate(
                groupId = groupId.resolve(properties),
                artifactId = artifactId.resolve(properties),
                version = version.resolve(properties),
            ),
            packaging = packaging.resolve(properties),
            licenses = licenses,
            dependencies = deps,
        )
    }.getOrNull()

    private fun Element.toDependency(props: Map<String, String>): PomDependency? {
        val depGroup = childText("groupId")?.resolve(props) ?: return null
        val depArtifact = childText("artifactId")?.resolve(props) ?: return null
        val depVersion = childText("version")?.resolve(props) ?: "unspecified"
        val scope = PomDependency.Scope.fromXml(childText("scope")?.resolve(props))
        val optional = childText("optional")?.resolve(props)?.toBoolean() ?: false
        return PomDependency(
            coordinate = MavenCoordinate(depGroup, depArtifact, depVersion),
            scope = scope,
            optional = optional,
        )
    }

    private fun String.resolve(props: Map<String, String>): String {
        if (!contains("\${")) return this
        val regex = Regex("""\$\{([^}]+)}""")
        return regex.replace(this) { match -> props[match.groupValues[1]] ?: match.value }
    }

    private fun Element.childElement(name: String): Element? =
        childElements().firstOrNull { it.tagName == name }

    private fun Element.childText(name: String): String? =
        childElement(name)?.textContent?.trim()?.takeIf { it.isNotEmpty() }

    private fun Element.childElements(): Sequence<Element> = sequence {
        val nodes = childNodes
        for (i in 0 until nodes.length) {
            val n = nodes.item(i)
            if (n.nodeType == Node.ELEMENT_NODE) yield(n as Element)
        }
    }
}

data class ParsedPom(
    val coordinate: MavenCoordinate,
    val packaging: String,
    val licenses: List<String>,
    val dependencies: List<PomDependency>,
)
