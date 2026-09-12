package calc.u.core

class Trie {
    private class Node {
        val children: MutableMap<Char, Node> = LinkedHashMap()
        var value: String? = null
        var depth: Int = 0
    }

    private val root = Node()

    fun insert(key: String, value: String) {
        if (key.isEmpty()) return
        var node = root
        for (ch in key.lowercase()) {
            val next = node.children[ch]
            if (next != null) {
                node = next
            } else {
                val created = Node()
                node.children[ch] = created
                node = created
            }
        }
        node.value = value
        node.depth = key.length
    }

    fun search(prefix: String): List<Pair<String, Int>> {
        var node = root
        for (ch in prefix.lowercase()) {
            val next = node.children[ch] ?: return emptyList()
            node = next
        }
        val collected = ArrayList<Pair<String, Int>>()
        val stack = ArrayDeque<Node>()
        stack.addFirst(node)
        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()
            val stored = current.value
            if (stored != null) {
                collected.add(Pair(stored, current.depth))
            }
            for (child in current.children.values) {
                stack.addFirst(child)
            }
        }
        return collected.sortedWith(compareBy({ it.first.lowercase() }, { it.first }))
    }
}

enum class SuggestionKind {
    FUNCTION,
    UNIT,
    CURRENCY
}

data class Suggestion(
    val name: String,
    val title: String,
    val description: String,
    val kind: SuggestionKind,
    val insertBefore: String,
    val insertAfter: String
) {
    companion object {
        fun function(
            name: String,
            title: String = "",
            description: String = ""
        ): Suggestion = Suggestion(name, title, description, SuggestionKind.FUNCTION, "$name(", ")")

        fun unit(
            name: String,
            title: String = "",
            description: String = ""
        ): Suggestion = Suggestion(name, title, description, SuggestionKind.UNIT, name, "")

        fun currency(
            name: String,
            title: String = "",
            description: String = ""
        ): Suggestion = Suggestion(name, title, description, SuggestionKind.CURRENCY, name, "")
    }
}

object AutocompleteIndex {
    data class QueryResult(
        val relevantText: String,
        val start: Int,
        val end: Int,
        val items: List<Suggestion>
    )

    private var trie = Trie()
    private var lookup: Map<String, Suggestion> = emptyMap()

    fun build(functions: List<Suggestion>, units: List<Suggestion>): Trie {
        val next = Trie()
        val map = LinkedHashMap<String, Suggestion>()
        for (suggestion in functions + units) {
            next.insert(suggestion.name, suggestion.name)
            val key = suggestion.name.lowercase()
            if (!map.containsKey(key)) {
                map[key] = suggestion
            }
        }
        trie = next
        lookup = map
        return next
    }

    fun query(input: String, cursor: Int): QueryResult {
        val end = cursor.coerceIn(0, input.length)
        var start = end
        while (start > 0) {
            val c = input[start - 1]
            if (c in 'a'..'z' || c in 'A'..'Z' || c == '_') {
                start--
            } else {
                break
            }
        }
        val relevantText = input.substring(start, end)
        if (relevantText.isEmpty()) {
            return QueryResult("", end, end, emptyList())
        }
        val snapshot = lookup
        val items = trie.search(relevantText).mapNotNull { (value, _) ->
            snapshot[value.lowercase()]
        }
        return QueryResult(relevantText, start, end, items)
    }
}
