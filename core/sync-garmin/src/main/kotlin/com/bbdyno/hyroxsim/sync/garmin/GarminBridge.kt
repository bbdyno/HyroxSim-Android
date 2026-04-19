package com.bbdyno.hyroxsim.sync.garmin

import android.content.Context
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException
import java.util.UUID

/**
 * Transport abstraction over the Connect IQ Android SDK. Downstream layers
 * depend only on this interface so tests can substitute a [StubGarminBridge].
 */
interface GarminBridge {
    fun requestDeviceSelection()
    /** Returns true when the envelope was accepted for delivery (device connected, app tracked). */
    fun sendEnvelope(envelope: Map<String, Any?>): Boolean
    fun setOnMessageReceived(handler: (Map<String, Any?>) -> Unit)
    fun setOnConnectionChanged(handler: (Boolean) -> Unit)
    val connectedDeviceName: String?
    /** True when a paired watch is currently connected — gate for all push flows. */
    val isPaired: Boolean

    companion object {
        fun provide(context: Context): GarminBridge = RealGarminBridge(context)
    }
}

/**
 * SDK-backed implementation. Lifecycle:
 *   1. [initialize] starts the SDK; `onSdkReady` flips [isSdkReady] true.
 *   2. [requestDeviceSelection] opens Garmin Connect Mobile's device picker.
 *   3. On connect we register for app events against the hardcoded CIQ app
 *      UUID (matches `../HyroxSim-Garmin/manifest.xml` applicationId).
 *
 * The Monkey C manifest stores the UUID without dashes; we expand it to
 * the 8-4-4-4-12 form the SDK expects.
 */
class RealGarminBridge(context: Context) : GarminBridge,
    ConnectIQ.ConnectIQListener,
    ConnectIQ.IQApplicationEventListener,
    ConnectIQ.IQDeviceEventListener {

    private val ciq: ConnectIQ =
        ConnectIQ.getInstance(context, ConnectIQ.IQConnectType.WIRELESS)
    private val appCtx = context.applicationContext

    private var connectedDevice: IQDevice? = null
    private var trackedApp: IQApp? = null
    private var isSdkReady: Boolean = false

    private var messageHandler: (Map<String, Any?>) -> Unit = {}
    private var connectionHandler: (Boolean) -> Unit = {}

    private val appUuid: UUID = UUID.fromString("ab20831c-3cc3-a8f6-b692-02dd7e0ca823")

    init {
        try {
            // autoUI=false: otherwise the SDK tries to pop its own
            // "install Garmin Connect" dialog with the application context,
            // which throws BadTokenException on devices without the
            // companion app. Our Settings screen handles messaging.
            ciq.initialize(appCtx, false, this)
        } catch (e: Exception) {
            println("⚠️ GarminBridge init failed: $e")
        }
    }

    override val connectedDeviceName: String?
        get() = connectedDevice?.friendlyName

    override val isPaired: Boolean
        get() = connectedDevice != null && trackedApp != null

    override fun requestDeviceSelection() {
        if (!isSdkReady) {
            println("⚠️ GarminBridge: SDK not ready — is Garmin Connect Mobile installed?")
            return
        }
        runCatching {
            val paired = ciq.knownDevices ?: emptyList()
            val first = paired.firstOrNull()
            if (first != null) {
                connect(first)
            } else {
                println("⚠️ GarminBridge: no paired devices. Open Garmin Connect Mobile and pair a watch first.")
            }
        }.onFailure { println("⚠️ GarminBridge discovery failed: $it") }
    }

    override fun sendEnvelope(envelope: Map<String, Any?>): Boolean {
        val device = connectedDevice ?: run {
            println("⚠️ GarminBridge: no connected device")
            return false
        }
        val app = trackedApp ?: run {
            println("⚠️ GarminBridge: no tracked app")
            return false
        }
        return runCatching {
            ciq.sendMessage(device, app, envelope) { _, _, status ->
                if (status != ConnectIQ.IQMessageStatus.SUCCESS) {
                    println("⚠️ sendMessage non-success: $status")
                }
            }
            true
        }.getOrElse {
            println("⚠️ sendMessage threw: $it")
            false
        }
    }

    override fun setOnMessageReceived(handler: (Map<String, Any?>) -> Unit) {
        messageHandler = handler
    }

    override fun setOnConnectionChanged(handler: (Boolean) -> Unit) {
        connectionHandler = handler
    }

    // MARK: - ConnectIQListener

    override fun onSdkReady() {
        isSdkReady = true
        runCatching {
            val paired = ciq.knownDevices ?: emptyList()
            paired.firstOrNull()?.let(::connect)
        }
    }

    override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus?) {
        isSdkReady = false
        println("⚠️ GarminBridge init error: $status")
    }

    override fun onSdkShutDown() {
        isSdkReady = false
        connectionHandler(false)
    }

    // MARK: - IQDeviceEventListener

    override fun onDeviceStatusChanged(device: IQDevice?, status: IQDevice.IQDeviceStatus?) {
        val isConnected = status == IQDevice.IQDeviceStatus.CONNECTED
        connectionHandler(isConnected)
    }

    // MARK: - IQApplicationEventListener

    override fun onMessageReceived(
        device: IQDevice?,
        app: IQApp?,
        messageData: MutableList<Any>?,
        status: ConnectIQ.IQMessageStatus?,
    ) {
        if (status != ConnectIQ.IQMessageStatus.SUCCESS) return
        val payload = messageData?.firstOrNull() as? Map<*, *> ?: return
        @Suppress("UNCHECKED_CAST")
        messageHandler(payload as Map<String, Any?>)
    }

    private fun connect(device: IQDevice) {
        connectedDevice = device
        try {
            ciq.registerForDeviceEvents(device, this)
        } catch (_: InvalidStateException) {
        } catch (_: ServiceUnavailableException) {
        }
        val app = IQApp(appUuid.toString())
        trackedApp = app
        try {
            ciq.registerForAppEvents(device, app, this)
        } catch (_: InvalidStateException) {
        } catch (_: ServiceUnavailableException) {
        }
    }
}

/** No-op implementation for tests. `paired` / `capturedEnvelopes` make assertions possible. */
class StubGarminBridge(
    var paired: Boolean = false,
    val capturedEnvelopes: MutableList<Map<String, Any?>> = mutableListOf(),
) : GarminBridge {
    override val connectedDeviceName: String? get() = if (paired) "Stub Watch" else null
    override val isPaired: Boolean get() = paired
    private var messageHandler: (Map<String, Any?>) -> Unit = {}
    override fun requestDeviceSelection() {}
    override fun sendEnvelope(envelope: Map<String, Any?>): Boolean {
        if (!paired) return false
        capturedEnvelopes += envelope
        return true
    }
    override fun setOnMessageReceived(handler: (Map<String, Any?>) -> Unit) {
        messageHandler = handler
    }
    override fun setOnConnectionChanged(handler: (Boolean) -> Unit) {}
    /** Test helper: simulate a watch-sourced envelope arriving. */
    fun simulateMessage(envelope: Map<String, Any?>) {
        messageHandler(envelope)
    }
}
