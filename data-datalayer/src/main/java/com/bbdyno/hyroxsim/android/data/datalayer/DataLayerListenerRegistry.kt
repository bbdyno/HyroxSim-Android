//
//  DataLayerListenerRegistry.kt
//  data-datalayer
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.datalayer

object DataLayerListenerRegistry {
    private val listeners = linkedSetOf<DataLayerEventListener>()

    @Synchronized
    fun addListener(listener: DataLayerEventListener) {
        listeners += listener
    }

    @Synchronized
    fun removeListener(listener: DataLayerEventListener) {
        listeners -= listener
    }

    @Synchronized
    fun dispatch(event: DataLayerEvent) {
        listeners.forEach { it.onEvent(event) }
    }
}
