package com.example.streampulse.data

data class Channel(
    val id: String,
    val name: String,
    val group: String,
    val url: String,
    val logo: String?
)

/** Historique de visionnage d'une chaîne (fréquence + récence). */
data class WatchEntry(
    val channelId: String,
    val group: String,
    val count: Int,
    val lastWatched: Long
)
