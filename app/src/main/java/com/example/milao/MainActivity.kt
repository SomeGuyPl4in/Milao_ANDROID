package com.example.milao

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.milao.ui.screens.EventDetailsScreen
import com.example.milao.ui.screens.HomeScreen
import com.example.milao.ui.screens.HomeViewModel
import com.example.milao.ui.screens.ProfileScreen
import com.example.milao.ui.screens.WelcomeScreen
import com.example.milao.ui.theme.MilaoTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            FirebaseDatabase.getInstance(
                "https://milao-default-rtdb.asia-southeast1.firebasedatabase.app/"
            )
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Initialization failed: ${e.message}")
        }

        setContent {
            MilaoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MilaoApp()
                }
            }
        }
    }
}

@Composable
fun MilaoApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    var firebaseUser by remember { mutableStateOf(auth.currentUser) }

    // FirebaseAuth state
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { authState ->
            firebaseUser = authState.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val isSignedIn by remember { derivedStateOf { firebaseUser != null } }

    LaunchedEffect(isSignedIn) {
        val destination = if (isSignedIn) "home" else "welcome"
        val current = navController.currentBackStackEntry?.destination?.route
        if (current != destination) {
            navController.navigate(destination) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            val route = navController.currentBackStackEntryAsState().value?.destination?.route
            if (route in listOf("home", "profile", "settings")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                WelcomeScreen(onSignInSuccess = {})
            }

            composable("home") {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    onEventJoined = { eventId ->
                        if (eventId.isNotBlank()) navController.navigate("event/$eventId")
                    },
                    viewModel = homeViewModel
                )
            }

            composable("event/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailsScreen(
                    eventId = eventId,
                    onNavigateUp = { navController.popBackStack() }
                )
            }

            composable("profile") { ProfileScreen() }
            composable("settings") { SettingsScreenPlaceholder() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        "home" to Icons.Default.Home,
        "profile" to Icons.Default.Person,
        "settings" to Icons.Default.Settings
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { navController.navigate(route) { popUpTo("home"); launchSingleTop = true } },
                icon = { Icon(icon, contentDescription = route) },
                label = { Text(route.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@Composable
fun SettingsScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings screen placeholder")
    }
}

@Preview(showBackground = true)
@Composable
fun MilaoAppPreview() {
    MilaoApp()
}
