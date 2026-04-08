//
//  DataLayerTransport.kt
//  data-datalayer
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.datalayer

interface DataLayerTransport {
    fun addListener(listener: DataLayerEventListener)
    fun removeListener(listener: DataLayerEventListener)

    fun refreshConnectedNodes(
        onSuccess: (List<ConnectedNode>) -> Unit,
        onError: (Throwable) -> Unit = {},
    )

    fun sendMessage(
        path: String,
        payload: ByteArray,
        nearbyOnly: Boolean = true,
        onComplete: (Result<Unit>) -> Unit = {},
    )

    fun putData(
        path: String,
        payload: ByteArray,
        urgent: Boolean = false,
        onComplete: (Result<Unit>) -> Unit = {},
    )

    fun deleteData(
        path: String,
        onComplete: (Result<Unit>) -> Unit = {},
    )
}
