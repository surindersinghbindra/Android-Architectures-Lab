package com.ekobits.demoapp.data.repository

import com.ekobits.demoapp.data.local.UserDao
import com.ekobits.demoapp.data.local.entity.toDomain
import com.ekobits.demoapp.data.remote.UserApi
import com.ekobits.demoapp.data.remote.dto.toEntity
import com.ekobits.demoapp.domain.model.User
import com.ekobits.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi, private val userDao: UserDao
) : UserRepository {
    override fun getUserStream(userId: String): Flow<User?> =
        userDao.observeUserByPhone(userId)
            .distinctUntilChanged()
            .map { it?.toDomain() }

    override suspend fun refreshUser(userId: String) = withContext(Dispatchers.IO){
        val remoteUser = userApi.getUser(userId)
        userDao.insertUser(remoteUser.toEntity())
    }
}
