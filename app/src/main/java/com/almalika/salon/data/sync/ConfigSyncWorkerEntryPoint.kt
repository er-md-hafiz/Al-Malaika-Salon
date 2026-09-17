package com.almalika.salon.data.sync

import com.almalika.salon.domain.usecase.RefreshConfigUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ConfigSyncWorkerEntryPoint {
    fun getRefreshConfigUseCase(): RefreshConfigUseCase
}
