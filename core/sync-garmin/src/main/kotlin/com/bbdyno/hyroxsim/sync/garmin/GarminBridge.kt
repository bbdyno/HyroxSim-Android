package com.bbdyno.hyroxsim.sync.garmin

import android.content.Context

/**
 * Transport abstraction over the Connect IQ Android SDK.
 *
 * The **real implementation** lives alongside `ConnectIQ-Android.aar`
 * once the developer drops it into `libs/`. See [RealGarminBridge] below
 * — the template code is deliberately behind a reflection guard so this
 * module compiles without the AAR present. When the AAR is added, swap
 * the factory in [provide] to `RealGarminBridge(context)`.
 *
 * Downstream layers (feature modules, app) depend only on this interface.
 */
interface GarminBridge {
    /** Open the Garmin Connect Mobile device picker. */
    fun requestDeviceSelection()

    /** Transmit a v1 envelope (see [GarminMessageCodec]). */
    fun sendEnvelope(envelope: Map<String, Any?>)

    fun setOnMessageReceived(handler: (Map<String, Any?>) -> Unit)
    fun setOnConnectionChanged(handler: (Boolean) -> Unit)

    /** Human-readable name of the currently paired device, or null. */
    val connectedDeviceName: String?

    companion object {
        fun provide(context: Context): GarminBridge = StubGarminBridge(context)
    }
}

/**
 * No-op implementation used before the AAR is wired up. Logs a single
 * warning on first interaction so nothing silently misbehaves.
 */
internal class StubGarminBridge(
    @Suppress("unused") private val context: Context,
) : GarminBridge {

    private var warned = false
    private var messageHandler: (Map<String, Any?>) -> Unit = {}
    private var connHandler: (Boolean) -> Unit = {}

    override val connectedDeviceName: String? = null

    override fun requestDeviceSelection() = warnOnce()
    override fun sendEnvelope(envelope: Map<String, Any?>) = warnOnce()
    override fun setOnMessageReceived(handler: (Map<String, Any?>) -> Unit) {
        messageHandler = handler
    }
    override fun setOnConnectionChanged(handler: (Boolean) -> Unit) {
        connHandler = handler
    }

    private fun warnOnce() {
        if (warned) return
        warned = true
        println("⚠️ GarminBridge: ConnectIQ-Android.aar not linked. See libs/README.md")
    }
}

/*
 * Template for the real SDK-backed bridge. Uncomment + compile after
 * adding the AAR:
 *
 * class RealGarminBridge(private val context: Context) : GarminBridge,
 *     com.garmin.android.connectiq.ConnectIQ.ConnectIQListener,
 *     com.garmin.android.connectiq.IQApp.IQAppEventListener,
 *     com.garmin.android.connectiq.IQDevice.IQDeviceEventListener
 * {
 *     private val ciq = com.garmin.android.connectiq.ConnectIQ.getInstance(
 *         context,
 *         com.garmin.android.connectiq.ConnectIQ.IQConnectType.WIRELESS
 *     )
 *     private val appUuid = java.util.UUID.fromString(
 *         "AB20831C-3CC3-A8F6-B692-02DD7E0CA823"
 *     )
 *     // ... wire up onSdkReady, registerForDeviceEvents, registerForAppEvents,
 *     //     sendMessage, onMessageReceived callbacks.
 * }
 */
