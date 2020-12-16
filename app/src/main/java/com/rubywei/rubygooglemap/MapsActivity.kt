package com.rubywei.rubygooglemap
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rubywei.rubygooglemap.databinding.ActivityMapsBinding
import java.util.*


class MapsActivity : AppCompatActivity() ,OnMapReadyCallback{

    lateinit var activityMainBinding: ActivityMapsBinding
    lateinit var focusLocationProviderClient : FusedLocationProviderClient
    lateinit var map : GoogleMap
    lateinit var location : Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        focusLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        activityMainBinding.btn.setOnClickListener {
            getLastLocation()
        }



    }

    override fun onStart() {
        super.onStart()
        if(!checkLocationPermission())
            permissionRequest()
    }

    private fun permissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        ),100)
    }

    private fun checkLocationPermission() : Boolean{
        return ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun isLocationEnabled() : Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun getLastLocation(){
        if(checkLocationPermission()){
            if(isLocationEnabled()){
                focusLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    val location: Location? = task.result
                    if(location == null){
                        newLocationData()
                    }else{
                        Log.d("Debug:" ,"Your Location:"+ location.longitude)
                        this.location = location
                        val mapFragment = (supportFragmentManager
                                .findFragmentById(R.id.map) as SupportMapFragment).also {
                            it.getMapAsync(this@MapsActivity)
                        }
                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }else{
            permissionRequest()
        }
    }
    @SuppressLint("MissingPermission")
    fun newLocationData(){
        val locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        focusLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        focusLocationProviderClient.requestLocationUpdates(
                locationRequest,locationCallback, Looper.myLooper()
        )
    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            this@MapsActivity.location = lastLocation
            Log.d("Debug:","your last last location: "+ lastLocation.longitude.toString())
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this@MapsActivity)
        }
    }
    private fun getCityName(lat: Double,long: Double):String{
        var cityName = ""
        var countryName = ""
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,long,3)
        if(address.size>0) {
            cityName = address[0].countryName
            countryName = address[0].toString()
            Log.d("testingAndroid",countryName)
        }
        return  address.toString()
    }

    override fun onMapReady(location: GoogleMap) {
        map = location

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(this.location.latitude, this.location.longitude)
        map.addMarker(
                MarkerOptions()
                        .position(sydney)
                        .title(getCityName(this.location.latitude,this.location.longitude)))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


}