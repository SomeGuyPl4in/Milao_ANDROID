package com.example.milao.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var locationInfo by remember { mutableStateOf("Location not updated yet.") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            // Permission Granted: Fetch and update location
            fetchAndAndUpdateLocation(context, fusedLocationClient, db, auth) { message ->
                locationInfo = message
            }
        } else {
            // Permission Denied
            val message = "Location permission is required to update your profile."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            locationInfo = message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Section")
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = locationInfo)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    fetchAndAndUpdateLocation(context, fusedLocationClient, db, auth) { message ->
                        locationInfo = message
                    }
                }
                else -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }) {
            Text("Update My Location")
        }
    }
}

private fun fetchAndAndUpdateLocation(
    context: Context,
    locationClient: FusedLocationProviderClient,
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    onResult: (String) -> Unit
) {
    try {
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                val user = auth.currentUser

                if (user != null) {
                    val userId = user.uid
                    val userName = user.displayName ?: "Anonymous"
                    val locationId = "loc_${lat.toString().replace('.', '_')}_${lon.toString().replace('.', '_')}"

                    val userData = hashMapOf(
                        "name" to userName,
                        "locationId" to locationId,
                        "latitude" to lat,
                        "longitude" to lon
                    )

                    db.collection("users").document(userId)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            val message = "Location updated: ($lat, $lon)"
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            onResult(message)
                        }
                        .addOnFailureListener { e ->
                            val message = "Failed to update location: ${e.message}"
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            onResult(message)
                        }
                } else {
                    onResult("User not signed in.")
                }
            } else {
                onResult("Could not retrieve location. Please try again.")
            }
        }.addOnFailureListener {
            onResult("Failed to get location: ${it.message}")
        }
    } catch (e: SecurityException) {
        onResult("Location permission error: ${e.message}")
    }
}
