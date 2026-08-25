package com.ekobits.demoapp.domain.repository

import com.ekobits.demoapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserStream(userId: String): Flow<User?>
    suspend fun refreshUser(userId: String)
}
