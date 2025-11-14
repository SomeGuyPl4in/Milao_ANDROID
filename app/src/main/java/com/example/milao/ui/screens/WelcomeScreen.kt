package com.example.milao.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.milao.ui.theme.MilaoTheme
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun WelcomeScreen(onSignInSuccess: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val oneTapClient = remember { Identity.getSignInClient(context) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onSignInSuccess()
                            } else {
                                errorMessage = task.exception?.message ?: "Firebase sign-in failed."
                            }
                        }
                } else {
                    errorMessage = "No ID token received from Google."
                }
            } catch (e: ApiException) {
                errorMessage = "Sign-in failed. Please ensure Google Play Services is up to date. Error: ${e.localizedMessage}"
            }
        } else {
            errorMessage = "Sign-in was cancelled."
        }
    }

    fun launchSignIn() {
        errorMessage = null
        val signInRequest = GetSignInIntentRequest.builder()
            .setServerClientId("838988061822-t1p8ntnn3a5883524i5su9409suremas.apps.googleusercontent.com")
            .build()

        oneTapClient.getSignInIntent(signInRequest)
            .addOnSuccessListener { pendingIntent ->
                signInLauncher.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            }
            .addOnFailureListener { e ->
                errorMessage = "Could not start sign-in flow: ${e.localizedMessage}"
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Milao",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { launchSignIn() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Sign in with Google")
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MilaoTheme {
        WelcomeScreen(onSignInSuccess = {})
    }
}
