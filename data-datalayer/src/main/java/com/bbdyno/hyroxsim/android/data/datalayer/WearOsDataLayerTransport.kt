//
//  WearOsDataLayerTransport.kt
//  data-datalayer
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.datalayer

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WearOsDataLayerTransport(
    context: Context,
) : DataLayerTransport {
    private val appContext = context.applicationContext
    private val messageClient = Wearable.getMessageClient(appContext)
    private val dataClient = Wearable.getDataClient(appContext)
    private val nodeClient = Wearable.getNodeClient(appContext)

    override fun addListener(listener: DataLayerEventListener) {
        DataLayerListenerRegistry.addListener(listener)
    }

    override fun removeListener(listener: DataLayerEventListener) {
        DataLayerListenerRegistry.removeListener(listener)
    }

    override fun refreshConnectedNodes(
        onSuccess: (List<ConnectedNode>) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        nodeClient.connectedNodes
            .addOnSuccessListener { nodes ->
                onSuccess(
                    nodes.map { node ->
                        ConnectedNode(
                            id = node.id,
                            displayName = node.displayName,
                            isNearby = node.isNearby,
                        )
                    },
                )
            }
            .addOnFailureListener(onError)
    }

    override fun sendMessage(
        path: String,
        payload: ByteArray,
        nearbyOnly: Boolean,
        onComplete: (Result<Unit>) -> Unit,
    ) {
        refreshConnectedNodes(
            onSuccess = { nodes ->
                val targets = if (nearbyOnly) nodes.filter { it.isNearby } else nodes
                if (targets.isEmpty()) {
                    onComplete(Result.failure(IllegalStateException("No connected wearable nodes")))
                    return@refreshConnectedNodes
                }

                var remaining = targets.size
                var terminalError: Throwable? = null
                targets.forEach { node ->
                    messageClient.sendMessage(node.id, path, payload)
                        .addOnSuccessListener {
                            remaining -= 1
                            if (remaining == 0 && terminalError == null) {
                                onComplete(Result.success(Unit))
                            }
                        }
                        .addOnFailureListener { throwable ->
                            terminalError = throwable
                            remaining -= 1
                            if (remaining == 0) {
                                onComplete(Result.failure(terminalError ?: throwable))
                            }
                        }
                }
            },
            onError = { onComplete(Result.failure(it)) },
        )
    }

    override fun putData(
        path: String,
        payload: ByteArray,
        urgent: Boolean,
        onComplete: (Result<Unit>) -> Unit,
    ) {
        val putDataRequest = PutDataMapRequest.create(path).apply {
            dataMap.putByteArray(HyroxDataLayerPaths.payloadKey, payload)
        }.asPutDataRequest().run {
            if (urgent) setUrgent() else this
        }

        dataClient.putDataItem(putDataRequest)
            .toUnitResult(onComplete)
    }

    override fun deleteData(
        path: String,
        onComplete: (Result<Unit>) -> Unit,
    ) {
        val uri = Uri.Builder()
            .scheme("wear")
            .authority("*")
            .path(path)
            .build()
        dataClient.deleteDataItems(uri)
            .toUnitResult(onComplete)
    }

    private fun <T> Task<T>.toUnitResult(onComplete: (Result<Unit>) -> Unit) {
        addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }
}
