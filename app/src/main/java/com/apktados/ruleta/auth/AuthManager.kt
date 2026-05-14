package com.apktados.ruleta.auth

import android.content.Context
import android.content.Intent
import com.apktados.ruleta.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthManager(context: Context) {

    private val appContext = context.applicationContext
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient = GoogleSignIn.getClient(
        appContext,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(appContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

    fun currentState(): AuthState = firebaseAuth.currentUser.toAuthState()

    fun addAuthStateListener(onStateChanged: (AuthState) -> Unit): FirebaseAuth.AuthStateListener {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            onStateChanged(auth.currentUser.toAuthState())
        }
        firebaseAuth.addAuthStateListener(listener)
        return listener
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.removeAuthStateListener(listener)
    }

    fun signInIntent(): Intent = googleSignInClient.signInIntent

    fun signInWithGoogleResult(data: Intent?): Task<AuthState> {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            .getResult(ApiException::class.java)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        return firebaseAuth.signInWithCredential(credential)
            .continueWith { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: IllegalStateException("Firebase sign-in failed")
                }
                firebaseAuth.currentUser.toAuthState()
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }

    private fun FirebaseUser?.toAuthState(): AuthState {
        return if (this == null) {
            AuthState.NotAuthenticated
        } else {
            AuthState.Authenticated(
                AuthUser(
                    uid = uid,
                    name = displayName,
                    email = email,
                    photoUrl = photoUrl?.toString()
                )
            )
        }
    }
}
