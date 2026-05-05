package com.o7solutions.android_compose.Topics

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

class AudioViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null

    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)
    var trackTitle by mutableStateOf("Loading...")

    fun initializePlayer(context: Context, url: String, title: String) {
        if (exoPlayer == null) {
            trackTitle = title
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()


                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        this@AudioViewModel.isPlaying = playing
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            this@AudioViewModel.duration = if (exoPlayer?.duration ?: 0L > 0) exoPlayer?.duration ?: 0L else 0L
                        }
                    }
                })


            }
        }
    }

    @Composable
    fun UpdatePosition() {
        LaunchedEffect(isPlaying) {
            while (isPlaying) {
                currentPosition = exoPlayer?.currentPosition ?: 0L
                delay(1000)
            }
        }
    }

    fun togglePlayback() {
        if (isPlaying) exoPlayer?.pause() else exoPlayer?.play()
    }

    fun seekTo(position: Float) {
        exoPlayer?.seekTo(position.toLong())
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}

@Composable
fun FullAudioPlayerScreen() {

    var audioViewModel: AudioViewModel = viewModel()
    val context = LocalContext.current
    val sampleUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    LaunchedEffect(Unit) {
        audioViewModel.initializePlayer(context, sampleUrl, "Android 16 Beats")
    }

    audioViewModel.UpdatePosition()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = audioViewModel.trackTitle,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Slider(
            value = audioViewModel.currentPosition.toFloat(),
            onValueChange = { audioViewModel.seekTo(it) },
            valueRange = 0f..(audioViewModel.duration.toFloat().coerceAtLeast(1f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(audioViewModel.currentPosition), fontSize = 12.sp)
            Text(text = formatTime(audioViewModel.duration), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { audioViewModel.seekTo((audioViewModel.currentPosition - 10000).toFloat()) }) {
                Icon(Icons.Default.Replay10, contentDescription = null, modifier = Modifier.size(32.dp))
            }

            LargeFloatingActionButton(
                onClick = { audioViewModel.togglePlayback() },
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (audioViewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(onClick = { audioViewModel.seekTo((audioViewModel.currentPosition + 10000).toFloat()) }) {
                Icon(Icons.Default.Forward10, contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}