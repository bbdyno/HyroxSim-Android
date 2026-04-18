package com.bbdyno.hyroxsim.sync.garmin

/**
 * v1 phone-watch protocol constants. Mirrors
 * `HyroxSim-Garmin/source/Sync/MessageProtocol.mc` and the iOS
 * `GarminMessageCodec`. Single source of truth: `MESSAGE_PROTOCOL.md`.
 */
object MessageProtocol {
    const val VERSION: Int = 1

    object Key {
        const val VERSION = "v"
        const val TYPE = "t"
        const val ID = "id"
        const val PAYLOAD = "payload"
    }

    object Type {
        // Phone → watch
        const val HELLO            = "hello"
        const val GOAL_SET         = "goal.set"
        const val TEMPLATE_UPSERT  = "template.upsert"
        const val TEMPLATE_DELETE  = "template.delete"
        const val CMD_ADVANCE      = "cmd.advance"
        const val CMD_PAUSE        = "cmd.pause"
        const val CMD_RESUME       = "cmd.resume"
        const val CMD_END          = "cmd.end"

        // Watch → phone
        const val HELLO_ACK        = "hello.ack"
        const val WORKOUT_COMPLETED = "workout.completed"
        const val LIVE_STATE       = "live.state"
        const val ACK              = "ack"
    }
}
