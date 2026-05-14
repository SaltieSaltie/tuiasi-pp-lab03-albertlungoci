package ro.mike.tuiasi

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class RSSItem(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String = "N/A"
) {
    fun print() {
        println("  Title       : $title")
        println("  Link        : $link")
        println("  Description : $description")
        println("  PubDate     : $pubDate")
        println("  " + "-".repeat(80))
    }
}

data class RSSChannel(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String = "N/A",
    val items: List<RSSItem> = emptyList()
) {
    fun print() {
        println("=".repeat(84))
        println("RSS CHANNEL")
        println("=".repeat(84))
        println("Title       : $title")
        println("Link        : $link")
        println("Description : $description")
        println("PubDate     : $pubDate")
        println("Items count : ${items.size}")
        println("=".repeat(84))
        println("\nITEMS:")
        println("-".repeat(84))
        items.forEach { it.print() }
    }

    fun printTitlesAndLinks() {
        println("=".repeat(84))
        println("Titluri și link-uri din RSS Feed: $title")
        println("=".repeat(84))
        items.forEachIndexed { index, item ->
            println("${index + 1}. ${item.title}")
            println("   -> ${item.link}")
        }
        println("=".repeat(84))
    }
}

fun parseRSSFromUrl(url: String): RSSChannel? {
    return try {
        println("Se conectează la: $url ...")

        val doc: Document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(10_000)
            .parser(org.jsoup.parser.Parser.xmlParser())
            .get()

        parseRSSDocument(doc)
    } catch (e: Exception) {
        println("Eroare la parsarea URL-ului: ${e.message}")
        null
    }
}

fun parseRSSFromString(xmlString: String): RSSChannel {
    val doc: Document = Jsoup.parse(xmlString, "", org.jsoup.parser.Parser.xmlParser())
    return parseRSSDocument(doc)
}

private fun parseRSSDocument(doc: Document): RSSChannel {
    val channel = doc.selectFirst("channel")
        ?: throw Exception("Nu s-a găsit tag-ul <channel> în RSS feed")

    val channelTitle       = channel.selectFirst("channel > title")?.text() ?: "N/A"
    val channelLink        = channel.selectFirst("channel > link")?.text() ?: "N/A"
    val channelDescription = channel.selectFirst("channel > description")?.text() ?: "N/A"
    val channelPubDate     = channel.selectFirst("channel > pubDate")?.text() ?: "N/A"

    val items = channel.select("item").map { item ->
        RSSItem(
            title       = item.selectFirst("title")?.text() ?: "N/A",
            link        = item.selectFirst("link")?.text()
                ?: item.selectFirst("guid")?.text() ?: "N/A",
            description = item.selectFirst("description")?.text() ?: "N/A",
            pubDate     = item.selectFirst("pubDate")?.text() ?: "N/A"
        )
    }

    return RSSChannel(
        title       = channelTitle,
        link        = channelLink,
        description = channelDescription,
        pubDate     = channelPubDate,
        items       = items
    )
}