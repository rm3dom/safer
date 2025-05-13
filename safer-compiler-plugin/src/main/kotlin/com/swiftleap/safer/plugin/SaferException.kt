package com.swiftleap.safer.plugin

/**
 * Custom exception class for the Safer plugin.
 * Used to indicate errors specific to the plugin's operation.
 */
class SaferException : Exception {
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message
     */
    constructor(message: String?) : super(message)
}
