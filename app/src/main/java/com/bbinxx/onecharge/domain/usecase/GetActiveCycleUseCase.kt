package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveCycleUseCase @Inject constructor(
    private val repository: BatteryCycleRepository
) {
    operator fun invoke(): Flow<BatteryCycle?> {
        return repository.getActiveCycleFlow()
    }
    
    suspend fun getDirect(): BatteryCycle? {
        return repository.getActiveCycle()
    }
}
