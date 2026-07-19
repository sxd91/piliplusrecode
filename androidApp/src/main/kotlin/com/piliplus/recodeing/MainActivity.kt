package com.piliplus.recodeing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DebugLog.install(applicationContext)
        DebugLog.info("MainActivity.onCreate begin")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
        DebugLog.info("MainActivity content installed")
    }
}
