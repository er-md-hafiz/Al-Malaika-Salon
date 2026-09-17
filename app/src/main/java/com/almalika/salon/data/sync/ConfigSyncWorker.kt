package com.almalika.salon.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.almalika.salon.domain.usecase.RefreshConfigUseCase
import dagger.hilt.android.EntryPointAccessors

class ConfigSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                ConfigSyncWorkerEntryPoint::class.java
            )
            val refreshConfigUseCase = entryPoint.getRefreshConfigUseCase()
            val result = refreshConfigUseCase(force = false)
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
