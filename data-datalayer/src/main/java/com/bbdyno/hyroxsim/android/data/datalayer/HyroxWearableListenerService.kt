//
//  HyroxWearableListenerService.kt
//  data-datalayer
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.datalayer

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class HyroxWearableListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (!messageEvent.path.startsWith(HyroxDataLayerPaths.root)) return
        DataLayerListenerRegistry.dispatch(
            DataLayerEvent.MessageReceived(
                sourceNodeId = messageEvent.sourceNodeId,
                path = messageEvent.path,
                payload = messageEvent.data ?: ByteArray(0),
            ),
        )
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            val item = event.dataItem
            val path = item.uri.path ?: return@forEach
            if (!path.startsWith(HyroxDataLayerPaths.root)) return@forEach
            val payload = DataMapItem.fromDataItem(item).dataMap.getByteArray(HyroxDataLayerPaths.payloadKey)
                ?: return@forEach
            DataLayerListenerRegistry.dispatch(
                DataLayerEvent.DataItemChanged(
                    sourceNodeId = item.uri.host,
                    path = path,
                    payload = payload,
                ),
            )
        }
    }
}
