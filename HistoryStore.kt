package com.example.streampulse.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class HistoryStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("streampulse", Context.MODE_PRIVATE)
    private val playlistFile get() = File(context.filesDir, "playlist.m3u")

    fun loadHistory(): List<WatchEntry> {
        val raw = prefs.getString("history", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                WatchEntry(o.getString("id"), o.getString("group"), o.getInt("count"), o.getLong("last"))
            }
        }.getOrDefault(emptyList())
    }

    fun saveHistory(list: List<WatchEntry>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.channelId)
                    .put("group", it.group)
                    .put("count", it.count)
                    .put("last", it.lastWatched)
            )
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }

    fun loadPlaylist(): String? = playlistFile.takeIf { it.exists() }?.readText()

    fun savePlaylist(text: String) {
        playlistFile.writeText(text)
    }
}
