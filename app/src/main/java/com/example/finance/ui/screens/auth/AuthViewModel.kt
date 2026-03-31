package com.example.finance.ui.screens.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.auth.AuthManager
import com.example.finance.auth.GoogleSignInHelper
import com.example.finance.auth.SignInResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val googleSignInHelper: GoogleSignInHelper
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        // Check if user is already signed in
        if (authManager.isUserSignedIn()) {
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    // --- Google Sign In ---
    suspend fun beginGoogleSignIn(): SignInResult {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            googleSignInHelper.beginSignIn()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start sign in: ${e.message}"
            SignInResult.Error(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun signInWithGoogle(idToken: String, onComplete: () -> Unit) {
        try {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authManager.signInWithGoogle(idToken)
            if (result.isSuccess) {
                onComplete()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        } catch (e: Exception) {
            _errorMessage.value = "Sign in failed: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    // --- Email / Password Auth ---
    fun signInWithEmail(email: String, password: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authManager.signInWithEmail(email, password)
            if (result.isSuccess) {
                onComplete()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            val result = authManager.signUpWithEmail(name, email, password)
            if (result.isSuccess) {
                _successMessage.value = "Verification email sent! Please check your inbox and verify your account before logging in."
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _successMessage.value = "Password reset email sent!"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }
    
    fun handleSignInResult(context: Context, data: Intent): com.google.android.gms.auth.api.identity.SignInCredential? {
        return try {
            val oneTapClient = Identity.getSignInClient(context)
            oneTapClient.getSignInCredentialFromIntent(data)
        } catch (e: ApiException) {
            _errorMessage.value = "Sign in failed: ${e.message}"
            null
        }
    }
    
    suspend fun signOut() {
        try {
            _isLoading.value = true
            authManager.signOut()
            googleSignInHelper.signOut()
        } catch (e: Exception) {
            _errorMessage.value = "Sign out failed: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    fun isUserSignedIn(): Boolean = authManager.isUserSignedIn()
    fun getCurrentUserId(): String? = authManager.getCurrentUserId()
    fun getCurrentUserName(): String? = authManager.getCurrentUserName()
}
