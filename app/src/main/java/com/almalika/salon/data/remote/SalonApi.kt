package com.almalika.salon.data.remote

import com.almalika.salon.data.remote.dto.MenuResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface SalonApi {

    @GET("menu.json")
    suspend fun getMenuConfig(): MenuResponseDto

    @GET
    suspend fun getMenuConfigByUrl(@Url url: String): MenuResponseDto

    @GET
    suspend fun getMenuConfigRaw(@Url url: String): Response<ResponseBody>

    @GET
    suspend fun getDynamicConfigByUrl(@Url url: String): Map<String, Any>
}

