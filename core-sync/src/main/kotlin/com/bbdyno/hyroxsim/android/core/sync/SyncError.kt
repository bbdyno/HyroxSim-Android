//
//  SyncError.kt
//  core-sync
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.core.sync

import java.io.Serializable

sealed class SyncError(
    message: String,
) : IllegalStateException(message), Serializable {
    data object SessionUnavailable : SyncError("Session unavailable")
    data object CounterpartUnreachable : SyncError("Counterpart unreachable")
    data object EncodingFailed : SyncError("Failed to encode sync payload")
    data object DecodingFailed : SyncError("Failed to decode sync payload")
    data class FileTransferFailed(val reason: String) : SyncError("File transfer failed: $reason")
}
