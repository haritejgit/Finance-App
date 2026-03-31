package com.example.finance.auth

import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    val userFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        // Emit current user immediately upon subscription
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // --- Google Sign In ---
    suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            saveUserToFirestore(result.user)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Email / Password Auth ---
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            
            // Set display name
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            user?.updateProfile(profileUpdates)?.await()
            
            // Send verification email
            user?.sendEmailVerification()?.await()
            
            saveUserToFirestore(user)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null && !user.isEmailVerified) {
                auth.signOut()
                return Result.failure(Exception("Please verify your email before signing in. Check your inbox."))
            }
            saveUserToFirestore(user)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Helpers ---
    private suspend fun saveUserToFirestore(user: FirebaseUser?) {
        user?.let { u ->
            val userMap = hashMapOf(
                "uid" to u.uid,
                "name" to (u.displayName ?: ""),
                "email" to (u.email ?: ""),
                "photoUrl" to (u.photoUrl?.toString() ?: ""),
                "lastLogin" to System.currentTimeMillis()
            )
            // Storing user data under their unique UID ensures data isolation at the user level
            db.collection("users").document(u.uid).set(userMap).await()
        }
    }
    
    fun getCurrentUser() = auth.currentUser
    fun signOut() = auth.signOut()
    
    fun isUserSignedIn(): Boolean {
        val user = auth.currentUser
        if (user == null) return false
        
        // If it's Google login, we consider it verified
        val isGoogleUser = user.providerData.any { it.providerId == "google.com" }
        return isGoogleUser || user.isEmailVerified
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUserName(): String? = auth.currentUser?.displayName
}
