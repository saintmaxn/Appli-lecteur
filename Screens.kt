package com.example.streampulse.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.streampulse.data.Channel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(vm: MainViewModel) {
    val snackbar = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("StreamPulse IPTV", fontWeight = FontWeight.Bold) },
                actions = { TextButton(onClick = { showDialog = true }) { Text("Playlist") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (vm.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            PlayerArea(vm)
            Library(vm)
        }
    }

    if (showDialog) PlaylistDialog(vm) { showDialog = false }
}

@Composable
private fun PlayerArea(vm: MainViewModel) {
    val ch = vm.current
    Box(
        Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (ch == null) {
            Text(
                "Choisissez une chaîne pour lancer la lecture.\nL'algorithme apprend de vos choix.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = vm.player; useController = true } },
                update = { it.player = vm.player },
                modifier = Modifier.fillMaxSize()
            )
            vm.playerError?.let {
                Text(
                    it,
                    color = Color(0xFFF87171),
                    modifier = Modifier.background(Color(0xCC000000)).padding(8.dp)
                )
            }
        }
    }
    if (ch != null) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(ch.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ch.group.uppercase(), color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Library(vm: MainViewModel) {
    val catalog = vm.catalog
    val history = vm.history

    val recs = remember(catalog, history) { vm.recommendations() }
    val byId = remember(catalog) { catalog.associateBy { it.id } }
    val recent = remember(catalog, history) {
        history.sortedByDescending { it.lastWatched }.mapNotNull { byId[it.channelId] }
    }
    val groups = remember(catalog) { catalog.map { it.group }.distinct().sorted() }
    val genreStats = remember(history) {
        history.groupBy { it.group }.mapValues { e -> e.value.sumOf { it.count } }
            .entries.sortedByDescending { it.value }
    }

    var selected by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    LaunchedEffect(catalog) { selected = null }
    val filtered = remember(catalog, selected, query) {
        catalog.filter {
            (selected == null || it.group == selected) &&
                (query.isBlank() || it.name.contains(query, ignoreCase = true))
        }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
        item { SectionTitle("✨ Pour vous") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recs, key = { it.id }) { ChannelCard(it, vm.current?.id == it.id) { c -> vm.play(c) } }
            }
        }

        if (recent.isNotEmpty()) {
            item { SectionTitle("🕘 Historique") }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recent, key = { it.id }) { ChannelCard(it, vm.current?.id == it.id) { c -> vm.play(c) } }
                }
            }
            item {
                Text(
                    "Vos genres : " + genreStats.joinToString("  ·  ") { "${it.key} ×${it.value}" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        item { SectionTitle("📺 Catalogue (${catalog.size})") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = selected == null, onClick = { selected = null }, label = { Text("Tous") }) }
                items(groups) { g ->
                    FilterChip(selected = selected == g, onClick = { selected = g }, label = { Text(g) })
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher une chaîne…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
        }
        items(filtered, key = { it.id }) { ChannelRow(it, vm.current?.id == it.id) { c -> vm.play(c) } }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
}

@Composable
private fun Thumb(ch: Channel, modifier: Modifier) {
    val (c1, c2) = groupColors(ch.group)
    var logoOk by remember(ch.id) { mutableStateOf(false) }
    Box(modifier.background(Brush.linearGradient(listOf(c1, c2))), contentAlignment = Alignment.Center) {
        if (!logoOk) {
            val initials = ch.name.split(" ").filter { it.isNotBlank() }.take(2)
                .joinToString("") { it.first().uppercase() }.ifEmpty { "TV" }
            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        if (ch.logo != null) {
            AsyncImage(
                model = ch.logo,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onSuccess = { logoOk = true },
                modifier = Modifier.fillMaxSize().padding(10.dp)
            )
        }
    }
}

@Composable
private fun ChannelCard(ch: Channel, playing: Boolean, onClick: (Channel) -> Unit) {
    Card(
        onClick = { onClick(ch) },
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (playing) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(8.dp)) {
            Thumb(ch, Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)))
            Text(ch.group.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp), maxLines = 1)
            Text(ch.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ChannelRow(ch: Channel, playing: Boolean, onClick: (Channel) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(if (playing) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick(ch) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Thumb(ch, Modifier.size(56.dp, 40.dp).clip(RoundedCornerShape(6.dp)))
        Column(Modifier.padding(start = 12.dp)) {
            Text(ch.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ch.group, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun PlaylistDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    var url by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.loadFromUri(it) }
        onDismiss()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playlist M3U") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL M3U / Xtream (get.php…)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { picker.launch(arrayOf("*/*")) }) { Text("📁 Choisir un fichier M3U") }
                TextButton(onClick = { vm.loadSample(); onDismiss() }) { Text("🧪 Charger la démo") }
                TextButton(onClick = { vm.resetHistory(); onDismiss() }) { Text("🗑 Réinitialiser l'historique", color = Color(0xFFF87171)) }
            }
        },
        confirmButton = { TextButton(onClick = { vm.loadFromUrl(url); onDismiss() }) { Text("Charger l'URL") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
