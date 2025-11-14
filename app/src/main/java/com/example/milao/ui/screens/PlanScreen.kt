package com.example.milao.ui.screens

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun PlanScreen(event: com.example.milao.ui.data.Event) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(1.35, 103.87), 10f)
    }
    GoogleMap(
        cameraPositionState = cameraPositionState
    ) {
        event.places.forEach { place ->
            place.location?.coordinate?.let {
                Marker(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = place.name,
                    snippet = place.descriptionAI
                )
            }
        }
    }
}
