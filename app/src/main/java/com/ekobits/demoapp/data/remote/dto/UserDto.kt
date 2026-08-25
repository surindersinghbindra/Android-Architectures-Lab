package com.ekobits.demoapp.data.remote.dto

import com.ekobits.demoapp.data.local.entity.UserEntity

data class UserDto(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String
)

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        userId = userId.toString(),
        name = "$firstName $lastName".trim(),
        email = email,
        phoneNumber = phoneNumber
    )
}
