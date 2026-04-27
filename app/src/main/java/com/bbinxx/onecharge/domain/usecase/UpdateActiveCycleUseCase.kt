package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import javax.inject.Inject

class UpdateActiveCycleUseCase @Inject constructor(
    private val repository: BatteryCycleRepository
) {
    suspend operator fun invoke(cycle: BatteryCycle) {
        repository.updateCycle(cycle)
    }
}
