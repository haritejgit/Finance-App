package com.example.finance.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance.R
import com.example.finance.auth.SignInResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    
    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                coroutineScope.launch {
                    val credential = viewModel.handleSignInResult(context, it)
                    if (credential != null) {
                        viewModel.signInWithGoogle(credential.googleIdToken ?: "", onLoginSuccess)
                    }
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0), Color(0xFF0D47A1))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Card(
                modifier = Modifier.size(90.dp).shadow(16.dp, CircleShape),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(45.dp), tint = Color(0xFF1E88E5))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Finance Manager", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(if (isSignUp) "Create your account" else "Login to your account", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
            
            Spacer(modifier = Modifier.height(32.dp))

            // Messages
            if (errorMessage != null || successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (errorMessage != null) Color.Red.copy(0.1f) else Color.Green.copy(0.1f)
                    )
                ) {
                    Text(
                        text = errorMessage ?: successMessage ?: "",
                        color = if (errorMessage != null) Color.Red else Color.Green,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Form
            Column {
                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(0.5f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                if (!showForgotPassword) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(0.5f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                if (!isSignUp && !showForgotPassword) {
                    TextButton(
                        onClick = { showForgotPassword = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Forgot Password?", color = Color.White.copy(0.8f), fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(
                    onClick = {
                        if (showForgotPassword) {
                            viewModel.sendPasswordReset(email)
                        } else if (isSignUp) {
                            viewModel.signUpWithEmail(name, email, password)
                        } else {
                            viewModel.signInWithEmail(email, password, onLoginSuccess)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLoading && email.isNotEmpty()
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1E88E5))
                    else Text(
                        text = when {
                            showForgotPassword -> "Reset Password"
                            isSignUp -> "Create Account"
                            else -> "Login"
                        },
                        color = Color(0xFF1E88E5),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showForgotPassword) {
                    TextButton(onClick = { showForgotPassword = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Back to Login", color = Color.White)
                    }
                } else {
                    TextButton(onClick = { isSignUp = !isSignUp; viewModel.clearMessages() }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isSignUp) "Already have an account? Login" else "New User? Create Account", color = Color.White)
                    }
                }
            }

            if (!isSignUp && !showForgotPassword) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.3f))
                    Text(" OR ", color = Color.White.copy(0.6f), modifier = Modifier.padding(horizontal = 8.dp), fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.3f))
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.beginGoogleSignIn()
                            if (result is SignInResult.Success) {
                                launcher.launch(IntentSenderRequest.Builder(result.intentSender).build())
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(Color.White, Color.White)))
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
