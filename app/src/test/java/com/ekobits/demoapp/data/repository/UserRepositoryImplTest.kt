package com.ekobits.demoapp.data.repository

import com.ekobits.demoapp.data.local.UserDao
import com.ekobits.demoapp.data.local.entity.UserEntity
import com.ekobits.demoapp.data.remote.UserApi
import com.ekobits.demoapp.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var userApi: UserApi
    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setup() {
        userApi = mockk()
        userDao = mockk()
        userRepository = UserRepositoryImpl(userApi, userDao)
    }

    @Test
    fun `getUserStream returns mapped domain user from dao`() = runTest {
        // Arrange
        val userId = "123"
        val userEntity = UserEntity(userId, "John Doe", "john@example.com")
        every { userDao.observeUser(userId) } returns flowOf(userEntity)

        // Act
        val result = userRepository.getUserStream(userId).first()

        // Assert
        assertEquals("John Doe", result?.name)
        assertEquals("john@example.com", result?.email)
    }

    @Test
    fun `refreshUser fetches from api and inserts into dao`() = runTest {
        // Arrange
        val userId = "123"
        val userDto = UserDto(userId, "John Remote", "remote@example.com")
        val expectedEntity = UserEntity(userId, "John Remote", "remote@example.com")
        
        coEvery { userApi.getUser(userId) } returns userDto
        coEvery { userDao.insertUser(any()) } returns Unit

        // Act
        userRepository.refreshUser(userId)

        // Assert
        coVerify { userApi.getUser(userId) }
        coVerify { userDao.insertUser(expectedEntity) }
    }
}
