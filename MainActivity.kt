package com.example.streampulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.streampulse.ui.AppScreen
import com.example.streampulse.ui.MainViewModel
import com.example.streampulse.ui.StreamPulseTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StreamPulseTheme { AppScreen(vm) } }
    }

    override fun onStop() {
        super.onStop()
        vm.player.pause()
    }
}
