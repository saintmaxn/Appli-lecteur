package com.example.streampulse.data

object M3uParser {
    private val attrRegex = Regex("""([\w-]+)="([^"]*)"""")

    fun parse(text: String): List<Channel> {
        val out = ArrayList<Channel>()
        val seen = HashSet<String>()
        var name: String? = null
        var group = "Autres"
        var logo: String? = null

        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    val attrs = attrRegex.findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    group = attrs["group-title"]?.takeIf { it.isNotBlank() } ?: "Autres"
                    logo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    name = line.substringAfterLast(",", "").trim()
                        .ifBlank { attrs["tvg-name"] ?: "Sans nom" }
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    val n = name
                    if (n != null) {
                        var id = "${line.hashCode()}_${n.hashCode()}"
                        var i = 1
                        while (!seen.add(id)) id = "${line.hashCode()}_${n.hashCode()}_${i++}"
                        out += Channel(id, n, group, line, logo)
                    }
                    name = null
                    logo = null
                    group = "Autres"
                }
            }
        }
        return out
    }
}
