package com.example.mentalhealthemotion.ui.theme

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.mentalhealthemotion.API.Map
import com.example.mentalhealthemotion.Data.Place
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(apiKey: String) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var nearbyPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    var showNearby by remember { mutableStateOf(false) }  // ✅ Controls button state

    val placeTypes = listOf("park", "gym", "cafe")
    val coroutineScope = rememberCoroutineScope()

    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted.value = isGranted
        if (isGranted) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted.value = true
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    // Move camera to user location only once
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 14f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Button(onClick = {
                if (!showNearby) {  // ✅ Fetch places only when enabling
                    userLocation?.let { loc ->
                        coroutineScope.launch {
                            try {
                                val allPlaces = mutableListOf<Place>()
                                for (type in placeTypes) {
                                    Log.d("PlacesAPI", "Fetching places of type: $type")
                                    val response = Map.api.getNearbyPlaces(
                                        location = "${loc.latitude},${loc.longitude}",
                                        type = type,
                                        apiKey = apiKey
                                    )
                                    allPlaces.addAll(response.results)
                                }
                                nearbyPlaces = allPlaces
                            } catch (e: Exception) {
                                Log.e("PlacesAPI", "Failed to fetch places: ${e.message}")
                                Toast.makeText(context, "Failed to fetch places", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    nearbyPlaces = emptyList()  // ✅ Clear places when hiding
                }
                showNearby = !showNearby  // ✅ Toggle button state
            }) {
                Text(if (showNearby) "Hide Nearby Places" else "Show Nearby Places")  // ✅ Change button text
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = MapProperties(isMyLocationEnabled = locationPermissionGranted.value),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                cameraPositionState = cameraPositionState
            ) {
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "You are here"
                    )
                }

                nearbyPlaces.forEach { place ->
                    val placeLocation = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                    Marker(
                        state = MarkerState(position = placeLocation),
                        title = place.name
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getUserLocation(fusedLocationClient: FusedLocationProviderClient, onLocationFound: (LatLng) -> Unit) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            onLocationFound(LatLng(location.latitude, location.longitude))
        } else {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let {
                        onLocationFound(LatLng(it.latitude, it.longitude))
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }.addOnFailureListener {
        Log.e("Location", "Failed to get location", it)
    }

}
