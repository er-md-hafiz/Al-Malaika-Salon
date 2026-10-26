package com.almalika.salon.core.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

interface PerformanceHelper {
    fun startTrace(traceName: String): Trace
    fun <T> trace(traceName: String, block: () -> T): T
    suspend fun <T> traceSuspend(traceName: String, block: suspend () -> T): T
}

@Singleton
class FirebasePerformanceHelper @Inject constructor(
    private val performance: FirebasePerformance
) : PerformanceHelper {

    override fun startTrace(traceName: String): Trace {
        val trace = performance.newTrace(traceName)
        trace.start()
        return trace
    }

    override fun <T> trace(traceName: String, block: () -> T): T {
        val trace = startTrace(traceName)
        return try {
            block()
        } finally {
            trace.stop()
        }
    }

    override suspend fun <T> traceSuspend(traceName: String, block: suspend () -> T): T {
        val trace = startTrace(traceName)
        return try {
            block()
        } finally {
            trace.stop()
        }
    }
}
