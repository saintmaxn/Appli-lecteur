package com.example.streampulse.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.streampulse.data.Channel
import com.example.streampulse.data.HistoryStore
import com.example.streampulse.data.M3uParser
import com.example.streampulse.data.SamplePlaylist
import com.example.streampulse.data.WatchEntry
import com.example.streampulse.recommendation.RecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = HistoryStore(app)
    private val engine = RecommendationEngine()

    val player: ExoPlayer = ExoPlayer.Builder(app).build()

    var catalog by mutableStateOf<List<Channel>>(emptyList()); private set
    var history by mutableStateOf<List<WatchEntry>>(emptyList()); private set
    var current by mutableStateOf<Channel?>(null); private set
    var loading by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null); private set
    var playerError by mutableStateOf<String?>(null); private set

    init {
        history = store.loadHistory()
        catalog = SamplePlaylist.channels
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                store.loadPlaylist()?.let { M3uParser.parse(it) }
            }
            if (!saved.isNullOrEmpty()) catalog = saved
        }
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playerError = "Lecture impossible (${error.errorCodeName})"
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) playerError = null
            }
        })
    }

    fun recommendations(): List<Channel> = engine.recommend(catalog, history)

    fun play(ch: Channel) {
        current = ch
        playerError = null
        player.setMediaItem(MediaItem.fromUri(ch.url))
        player.prepare()
        player.playWhenReady = true
        recordWatch(ch)
    }

    private fun recordWatch(ch: Channel) {
        val now = System.currentTimeMillis()
        val existing = history.find { it.channelId == ch.id }
        history = if (existing != null) {
            history.map {
                if (it.channelId == ch.id) it.copy(group = ch.group, count = it.count + 1, lastWatched = now) else it
            }
        } else {
            history + WatchEntry(ch.id, ch.group, 1, now)
        }
        val snapshot = history
        viewModelScope.launch(Dispatchers.IO) { store.saveHistory(snapshot) }
    }

    fun resetHistory() {
        history = emptyList()
        viewModelScope.launch(Dispatchers.IO) { store.saveHistory(emptyList()) }
        message = "Historique réinitialisé"
    }

    fun loadSample() {
        catalog = SamplePlaylist.channels
        viewModelScope.launch(Dispatchers.IO) { store.savePlaylist(SamplePlaylist.M3U) }
        message = "Playlist de démo chargée"
    }

    fun loadFromUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            loading = true
            try {
                val text = withContext(Dispatchers.IO) { download(url.trim()) }
                importText(text)
            } catch (e: Exception) {
                message = "Échec du chargement : ${e.localizedMessage ?: "erreur réseau"}"
            } finally {
                loading = false
            }
        }
    }

    fun loadFromUri(uri: Uri) {
        viewModelScope.launch {
            loading = true
            try {
                val text = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.use { it.readText() }
                        ?: throw IllegalStateException("fichier illisible")
                }
                importText(text)
            } catch (e: Exception) {
                message = "Échec de lecture : ${e.localizedMessage ?: "erreur"}"
            } finally {
                loading = false
            }
        }
    }

    private suspend fun importText(text: String) {
        val parsed = withContext(Dispatchers.Default) { M3uParser.parse(text) }
        if (parsed.isEmpty()) {
            message = "Aucune chaîne trouvée dans cette playlist"
            return
        }
        withContext(Dispatchers.IO) { store.savePlaylist(text) }
        catalog = parsed
        message = "${parsed.size} chaînes chargées"
    }

    private fun download(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 15_000
            c.readTimeout = 30_000
            c.instanceFollowRedirects = true
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    fun consumeMessage() {
        message = null
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
