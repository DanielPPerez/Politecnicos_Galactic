package com.test.galaxyUP.api

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

// --- Modelos para Autenticación ---
data class RegisterRequest(val username: String, val email: String, val password: String)
data class AuthRequest(val login: String, val password: String)
data class AuthData(val token: String, val user: UserInfo)
data class UserInfo(val id: Int, val username: String, val email: String)

// --- Modelo para Perfil de Usuario ---
data class ProfileData(val profile: UserProfile)
data class UserProfile(val id: Int, val username: String, val email: String, val monedas: Int)

// --- Modelo para Guardar Puntuación ---
data class ScorePostRequest(val puntuacion: Int, val tiempo_jugado: Int)
data class ScorePostResponse(val puntuacionGuardada: Int, val monedasGanadas: Int)

// --- Modelo para el Leaderboard ---
data class LeaderboardEntry(val puntuacion: Int, val usuario: LeaderboardUser)
data class LeaderboardUser(val id: Int, val username: String)

// Modelo para la petición de gastar monedas
data class SpendCoinsRequest(val amount: Int)

// Modelo para la respuesta al gastar monedas
data class SpendCoinsResponse(val monedasRestantes: Int)