package com.apktados.ruleta.auth

data class AuthUser(
    val uid: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?
)

sealed interface AuthState {
    data object NotAuthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}
