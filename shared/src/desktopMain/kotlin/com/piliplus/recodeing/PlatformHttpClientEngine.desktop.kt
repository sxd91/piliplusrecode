package com.piliplus.recodeing.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun platformHttpClientEngine(): HttpClientEngineFactory<*> = CIO
