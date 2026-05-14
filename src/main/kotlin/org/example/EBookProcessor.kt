package ro.mike.tuiasi

import java.io.File

fun removeMultipleSpaces(text: String): String {
    return text.replace(Regex("[ \\t]{2,}"), " ")
}

fun removeMultipleNewlines(text: String): String {
    return text.replace(Regex("(\\r?\\n){2,}"), "\n")
}

fun removePageNumbers(text: String): String {
    val pageNumberRegex = Regex("^[ \\t]{4,}(Page\\s+)?\\d+[ \\t]*$", RegexOption.MULTILINE)
    return text.replace(pageNumberRegex, "")
}

fun removeAuthorName(text: String): String {
    val candidateRegex = Regex(
        "(?:^|\\n)[ \\t]*([A-ZĂÂÎȘȚ][a-zăâîșț]+([ \\t]+[A-ZĂÂÎȘȚ][a-zăâîșț]+){1,3})[ \\t]*(?:\\n|\$)"
    )

    val candidates = candidateRegex.findAll(text)
        .map { it.groupValues[1].trim() }
        .groupingBy { it }
        .eachCount()
        .filter { it.value >= 2 }
        .keys

    var result = text
    for (author in candidates) {
        val escapedAuthor = Regex.escape(author)
        result = result.replace(
            Regex("^[ \\t]*$escapedAuthor[ \\t]*$", RegexOption.MULTILINE), ""
        )
        println("  [Nice to Have] Autor detectat și eliminat: \"$author\"")
    }
    return result
}

fun removeChapterNames(text: String): String {
    val allCapsRegex = Regex(
        "(?<=\\n\\n)[ \\t]*[A-ZĂÂÎȘȚ ]{4,}[ \\t]*(?=\\n\\n)",
        RegexOption.MULTILINE
    )
    val chapterRegex = Regex(
        "^[ \\t]*(Chapter|Capitol|CAPITOLUL|CHAPTER)[ \\t]+[\\dIVXivx]+.*$",
        RegexOption.MULTILINE
    )
    val romanRegex = Regex(
        "^[ \\t]*(I{1,3}|IV|V?I{0,3}|IX|X{1,3})\\. .+$",
        RegexOption.MULTILINE
    )

    var result = text
    result = result.replace(allCapsRegex, "")
    result = result.replace(chapterRegex, "")
    result = result.replace(romanRegex, "")
    return result
}

fun fixRomanianCharacters(text: String): String {
    return text
        .replace('ş', 'ș')
        .replace('ţ', 'ț')
        .replace('Ş', 'Ș')
        .replace('Ţ', 'Ț')
        .replace('ã', 'ă')
        .replace('Ã', 'Ă')
}

fun processEbook(inputPath: String, outputPath: String, niceToHave: Boolean = true) {
    println("=".repeat(84))
    println("PROCESARE EBOOK: $inputPath")
    println("=".repeat(84))

    val originalText = File(inputPath).readText(Charsets.UTF_8)
    println("Dimensiune originală: ${originalText.length} caractere, ${originalText.lines().size} linii")

    var processed = originalText

    println("\n[Must Have] Eliminare numere de pagină...")
    processed = removePageNumbers(processed)

    println("[Must Have] Eliminare spații multiple...")
    processed = removeMultipleSpaces(processed)

    println("[Must Have] Eliminare linii noi multiple...")
    processed = removeMultipleNewlines(processed)

    if (niceToHave) {
        println("\n[Nice to Have] Detectare și eliminare nume autor...")
        processed = removeAuthorName(processed)

        println("[Nice to Have] Detectare și eliminare nume capitol...")
        processed = removeChapterNames(processed)

        println("[Nice to Have] Corectare caractere românești...")
        processed = fixRomanianCharacters(processed)
    }

    File(outputPath).writeText(processed, Charsets.UTF_8)
    println("\nDimensiune după procesare: ${processed.length} caractere, ${processed.lines().size} linii")
    println("Fișier salvat la: $outputPath")
    println("=".repeat(84))
}