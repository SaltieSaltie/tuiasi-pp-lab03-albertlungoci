package ro.mike.tuiasi

import org.jsoup.Jsoup
import java.io.File

data class TreeNode(
    val url: String,
    val children: MutableList<TreeNode> = mutableListOf()
) {
    fun addChild(child: TreeNode) {
        children.add(child)
    }

    fun print(indent: Int = 0) {
        val prefix = if (indent == 0) "" else "  ".repeat(indent - 1) + "└─ "
        println("$prefix$url")
        children.forEach { it.print(indent + 1) }
    }
}

fun extractDomain(url: String): String {
    return try {
        val cleaned = url.removePrefix("https://").removePrefix("http://")
        cleaned.split("/").first().split("?").first()
    } catch (e: Exception) {
        url
    }
}

fun extractLinks(url: String, domain: String): List<String> {
    return try {
        println("  Extrag link-uri de la: $url")
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(8_000)
            .get()

        doc.select("a[href]")
            .map { it.absUrl("href") }
            .filter { link ->
                link.isNotBlank() &&
                        (link.startsWith("http://") || link.startsWith("https://")) &&
                        extractDomain(link) == domain &&
                        link != url
            }
            .distinct()
            .take(10)
    } catch (e: Exception) {
        println("  Eroare la extragerea link-urilor de la $url: ${e.message}")
        emptyList()
    }
}

fun buildLinkTree(rootUrl: String): TreeNode {
    val domain = extractDomain(rootUrl)
    println("=".repeat(84))
    println("Construiesc arborele pentru domeniul: $domain")
    println("=".repeat(84))

    val root = TreeNode(domain)

    println("\nNivel 1 - link-uri de la URL-ul inițial:")
    val level1Links = extractLinks(rootUrl, domain)

    for (link1 in level1Links) {
        val child = TreeNode(link1)
        root.addChild(child)

        println("\nNivel 2 - link-uri de la: $link1")
        val level2Links = extractLinks(link1, domain)
        for (link2 in level2Links) {
            child.addChild(TreeNode(link2))
        }
    }

    return root
}

fun serializeTree(root: TreeNode, filePath: String? = null): String {
    val sb = StringBuilder()

    fun serializeNode(node: TreeNode) {
        if (node.children.isNotEmpty()) {
            val childrenStr = node.children.joinToString(", ") { child ->
                child.url.removePrefix("https://")
                    .removePrefix("http://")
                    .trimEnd('/')
            }
            sb.appendLine("${node.url}: $childrenStr")
        } else {
            sb.appendLine("${node.url}:")
        }
        node.children.forEach { serializeNode(it) }
    }

    serializeNode(root)
    val result = sb.toString().trimEnd()

    filePath?.let {
        File(it).writeText(result, Charsets.UTF_8)
        println("Arbore serializat în: $it")
    }

    return result
}

fun deserializeTree(source: String, data: String): TreeNode {
    val content = when (source) {
        "file"   -> File(data).readText(Charsets.UTF_8)
        "string" -> data
        else     -> throw Exception("Sursă necunoscută: $source")
    }

    val lines = content.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) throw Exception("Conținut gol, nu se poate deserializa")

    val adjacencyMap = mutableMapOf<String, List<String>>()
    for (line in lines) {
        val parts = line.split(": ", limit = 2)
        val parentUrl = parts[0].trim()
        val childrenUrls = if (parts.size > 1 && parts[1].isNotBlank())
            parts[1].split(", ").map { it.trim() }
        else
            emptyList()
        adjacencyMap[parentUrl] = childrenUrls
    }

    fun buildNode(url: String): TreeNode {
        val node = TreeNode(url)
        adjacencyMap[url]?.forEach { childUrl ->
            if (childUrl.isNotBlank()) {
                node.addChild(buildNode(childUrl))
            }
        }
        return node
    }

    val rootUrl = lines.first().split(": ").first().trim()
    return buildNode(rootUrl)
}