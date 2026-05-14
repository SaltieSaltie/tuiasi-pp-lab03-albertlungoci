package org.example

import ro.mike.tuiasi.TreeNode
import ro.mike.tuiasi.buildLinkTree
import ro.mike.tuiasi.deserializeTree
import ro.mike.tuiasi.fixRomanianCharacters
import ro.mike.tuiasi.parseRSSFromString
import ro.mike.tuiasi.parseRSSFromUrl
import ro.mike.tuiasi.processEbook
import ro.mike.tuiasi.removeMultipleNewlines
import ro.mike.tuiasi.removeMultipleSpaces
import ro.mike.tuiasi.serializeTree
import java.io.File

fun main(args: Array<String>) {

    // Problema 1
    println("*".repeat(84))
    val rssUrl = "http://rss.cnn.com/rss/edition.rss"
    val channelFromUrl = parseRSSFromUrl(rssUrl)

    if (channelFromUrl != null) {
        channelFromUrl.print()
        println("\n")

        channelFromUrl.printTitlesAndLinks()
    }
    else {
        println("Nu s-a putut parsa feed-ul RSS de la URL.")
    }

    // Problema 2
    val projectPath: String = System.getProperty("user.dir")

    if (args.isNotEmpty()) {
        val inputPath = args[0]
        val outputPath = if (args.size > 1) args[1]
        else "${inputPath.removeSuffix(".txt")}_procesat.txt"
        processEbook(inputPath, outputPath, niceToHave = true)
        return
    }

    val demoText = """
Ion  Creangă
Amintiri    din   Copilărie
 
 
Ion  Creangă
 
 
CAPITOLUL I
 
 
Stau câteodată  şi-mi aduc aminte   de vremurile trecute.
 
          1
 
 
Şi parcă îmi vine  să   plâng...    sau  să    râd.
 
Ion  Creangă
 
CAPITOLUL II
 
 
Copilăria  este   cea mai   frumoasă   perioadă.
 
 
Ţara noastră este   frumoasă.
Munţii şi văile   sunt   pline  de   verdeaţă.
 
 
          2
 
 
Oamenii  sunt   harnici   şi   veseli.
 
 
 
Sfârşit.
    """.trimIndent()

    val demoInput  = "$projectPath/src/main/resources/demo_ebook.txt"
    val demoOutput = "$projectPath/src/main/resources/demo_ebook_procesat.txt"
    File(demoInput).parentFile?.mkdirs()
    File(demoInput).writeText(demoText, Charsets.UTF_8)

    println("TEXT ORIGINAL:")
    println("-".repeat(84))
    println(demoText)
    println("-".repeat(84))

    processEbook(demoInput, demoOutput, niceToHave = true)

    println("\nTEXT PROCESAT:")
    println("-".repeat(84))
    println(File(demoOutput).readText(Charsets.UTF_8))
    println("-".repeat(84))

    println("\n" + "=".repeat(84))
    println("DEMO TRANSFORMĂRI INDIVIDUALE")
    println("=".repeat(84))

    val testSpaces = "Acesta   este    un   text   cu   spatii   multiple."
    println("Original  : $testSpaces")
    println("Procesat  : ${removeMultipleSpaces(testSpaces)}")
    println()

    val testNewlines = "Linia 1\n\n\n\nLinia 2\n\n\nLinia 3"
    println("Original  (newlines): ${testNewlines.replace("\n", "\\n")}")
    println("Procesat  (newlines): ${removeMultipleNewlines(testNewlines).replace("\n", "\\n")}")
    println()

    val testRomanian = "Ţara noastră are munţi şi văi. Ştiinţa este importantă."
    println("Original  (ro vechi): $testRomanian")
    println("Procesat  (ro corect): ${fixRomanianCharacters(testRomanian)}")

    // PROBLEMA 3
    val project2Path: String = System.getProperty("user.dir")
    val serializedPath = "$project2Path/src/main/resources/link_tree.txt"
    File("$project2Path/src/main/resources").mkdirs()

    println("*".repeat(84))
    println("DEMO: Arbore construit manual (ca în exemplul din laborator)")
    println("*".repeat(84))

    val demoRoot = TreeNode("stackoverflow.com").apply {
        addChild(TreeNode("stackoverflow.com/tags").apply {
            addChild(TreeNode("stackoverflow.com/tags/java"))
            addChild(TreeNode("stackoverflow.com/tags/python"))
            addChild(TreeNode("stackoverflow.com/tags/sql"))
        })
        addChild(TreeNode("stackoverflow.com/users").apply {
            addChild(TreeNode("stackoverflow.com/users/1144034"))
            addChild(TreeNode("stackoverflow.com/users/3732271"))
        })
        addChild(TreeNode("stackoverflow.com/questions").apply {
            addChild(TreeNode("stackoverflow.com/questions/927358"))
            addChild(TreeNode("stackoverflow.com/questions/292357"))
        })
    }

    println("\nStructura arborelui:")
    println("-".repeat(84))
    demoRoot.print()

    println("\n" + "=".repeat(84))
    println("SERIALIZARE")
    println("=".repeat(84))
    val serialized = serializeTree(demoRoot, serializedPath)
    println("\nConținut serializat:")
    println("-".repeat(84))
    println(serialized)

    println("\n" + "=".repeat(84))
    println("DESERIALIZARE din string")
    println("=".repeat(84))
    val restoredFromString = deserializeTree("string", serialized)
    println("Arbore restaurat din string:")
    println("-".repeat(84))
    restoredFromString.print()

    println("\n" + "=".repeat(84))
    println("DESERIALIZARE din fișier: $serializedPath")
    println("=".repeat(84))
    val restoredFromFile = deserializeTree("file", serializedPath)
    println("Arbore restaurat din fișier:")
    println("-".repeat(84))
    restoredFromFile.print()

    println("\n" + "*".repeat(84))
    println("LIVE: Extragere link-uri de la URL real")
    println("*".repeat(84))
    val targetUrl = "https://books.toscrape.com"
    val liveTree = buildLinkTree(targetUrl)

    println("\nStructura arborelui:")
    liveTree.print()

    val liveSerializedPath = "$project2Path/src/main/resources/live_link_tree.txt"
    val liveSerialized = serializeTree(liveTree, liveSerializedPath)
    println("\nConținut serializat (primele 20 linii):")
    liveSerialized.lines().take(20).forEach { println(it) }

    val liveRestored = deserializeTree("file", liveSerializedPath)
    println("\nArbore deserializat:")
    liveRestored.print()
}
