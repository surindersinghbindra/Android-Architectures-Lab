package com.ekobits.demoapp.domain.usecase

import com.ekobits.demoapp.domain.repository.UserRepository
import javax.inject.Inject

class RefreshUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String) {
        repository.refreshUser(userId)
    }
}
