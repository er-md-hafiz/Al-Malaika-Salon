package com.almalika.salon.data.remote

import com.almalika.salon.data.remote.dto.MenuResponseDto
import retrofit2.http.GET
import retrofit2.http.Url

interface SalonApi {

    @GET("menu.json")
    suspend fun getMenuConfig(): MenuResponseDto

    @GET
    suspend fun getDynamicConfigByUrl(@Url url: String): Map<String, Any>
}

