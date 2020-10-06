package com.example.a101_monitoring.log.logger

enum class LogTypes {
    DEBUG {
        override fun toString(): String {
            return "debug"
        }
    },
    ERROR {
        override fun toString(): String {
            return "error"
        }
    },
    INFO {
        override fun toString(): String {
            return "info"
        }
    },
    VERBOSE {
        override fun toString(): String {
            return "verbose"
        }
    },
    WARNING {
        override fun toString(): String {
            return "warning"
        }
    }
}