package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCompletedCyclesUseCase @Inject constructor(
    private val repository: BatteryCycleRepository
) {
    operator fun invoke(): Flow<List<BatteryCycle>> {
        return repository.getCompletedCyclesFlow()
    }

    suspend fun getDirect(): List<BatteryCycle> {
        return repository.getCompletedCycles()
    }
}
