package com.test.galaxyUP.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<ApiResponse<AuthData>>

    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ApiResponse<ProfileData>>

    @POST("scores")
    suspend fun saveScore(
        @Header("Authorization") token: String,
        @Body request: ScorePostRequest
    ): Response<ApiResponse<ScorePostResponse>>

    @GET("scores/leaderboard/{limit}")
    suspend fun getLeaderboard(@Path("limit") limit: Int = 20): Response<ApiResponse<List<LeaderboardEntry>>>

    @POST("user/spend-coins")
    suspend fun spendCoins(
        @Header("Authorization") token: String,
        @Body request: SpendCoinsRequest
    ): Response<ApiResponse<SpendCoinsResponse>>
}