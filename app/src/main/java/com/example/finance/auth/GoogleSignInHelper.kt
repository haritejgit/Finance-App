package com.example.finance.auth

import android.content.Context
import android.content.IntentSender
import com.example.finance.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var oneTapClient: SignInClient = Identity.getSignInClient(context)
    
    // Pulling the Web Client ID from resources (automatically generated from google-services.json)
    private val webClientId by lazy {
        context.getString(R.string.default_web_client_id)
    }
    
    suspend fun beginSignIn(): SignInResult {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        return try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            SignInResult.Success(result.pendingIntent.intentSender)
        } catch (e: Exception) {
            e.printStackTrace()
            SignInResult.Error(e)
        }
    }
    
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

sealed class SignInResult {
    data class Success(val intentSender: IntentSender) : SignInResult()
    data class Error(val exception: Exception) : SignInResult()
}
