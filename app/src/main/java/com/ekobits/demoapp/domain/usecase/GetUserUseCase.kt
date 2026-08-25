package com.ekobits.demoapp.domain.usecase

import com.ekobits.demoapp.domain.model.User
import com.ekobits.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(userId: String): Flow<User?> {
        return repository.getUserStream(userId)
    }
}

