//
//  DataLayerModels.kt
//  data-datalayer
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.datalayer

import java.io.Serializable

data class ConnectedNode(
    val id: String,
    val displayName: String,
    val isNearby: Boolean,
) : Serializable

sealed interface DataLayerEvent : Serializable {
    data class MessageReceived(
        val sourceNodeId: String,
        val path: String,
        val payload: ByteArray,
    ) : DataLayerEvent

    data class DataItemChanged(
        val sourceNodeId: String?,
        val path: String,
        val payload: ByteArray,
    ) : DataLayerEvent
}

fun interface DataLayerEventListener {
    fun onEvent(event: DataLayerEvent)
}

object HyroxDataLayerPaths {
    const val root: String = "/hyrox"
    const val livePacket: String = "$root/live/packet"
    const val templatePrefix: String = "$root/sync/template"
    const val completedWorkoutPrefix: String = "$root/sync/completed-workout"
    const val templateDeletedPrefix: String = "$root/sync/template-deleted"
    const val payloadKey: String = "payload"
}
