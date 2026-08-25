package com.ekobits.demoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ekobits.demoapp.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String
)

fun UserEntity.toDomain(): User {
    return User(
        userId = userId,
        name = name,
        email = email,
        phoneNumber = phoneNumber
    )
}
