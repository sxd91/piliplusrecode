package com.piliplus.recodeing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DebugLog.install(applicationContext)
        DebugLog.info("MainActivity.onCreate begin")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                DebugLog.info("Compose first effect entered")
                delay(1_500)
                DebugLog.info("Compose remained active for 1500ms")
            }
            App()
        }
        DebugLog.info("MainActivity content installed")
    }

    override fun onStart() {
        super.onStart()
        DebugLog.info("MainActivity.onStart")
    }

    override fun onResume() {
        super.onResume()
        DebugLog.info("MainActivity.onResume")
    }

    override fun onPause() {
        DebugLog.info("MainActivity.onPause")
        super.onPause()
    }

    override fun onStop() {
        DebugLog.info("MainActivity.onStop")
        super.onStop()
    }

    override fun onDestroy() {
        DebugLog.info("MainActivity.onDestroy; finishing=$isFinishing changingConfigurations=$isChangingConfigurations")
        super.onDestroy()
    }
}
