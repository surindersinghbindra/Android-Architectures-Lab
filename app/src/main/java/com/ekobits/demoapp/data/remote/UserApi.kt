package com.ekobits.demoapp.data.remote

import com.ekobits.demoapp.data.remote.dto.UserDto
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {
    @POST("api/Users/GetLeadDetail")
    suspend fun getUser(@Query("phoneNumber") userId: String): UserDto
}
