package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Result
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.security.Permissions

const val PERMISSION_REQUEST_CODE = 1
const val CURRENT_LOCATION_CODE = 2


class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        if (!checkPermissions()) {
            askPermissions()
        }

        getLocationAccess()
//
//        Done: add the map setup implementation
//        Done: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
//        TODO: call this function after the user confirms on the selected location

        onLocationSelected()

        return binding.root
    }

    private fun getLocationAccess() {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val locationSettingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        val locationClient = LocationServices.getSettingsClient(requireActivity())

        locationClient.checkLocationSettings(locationSettingsRequest).run {
            addOnFailureListener {
                if (it is ResolvableApiException) {
                    try {
                        it.startResolutionForResult(requireActivity(), CURRENT_LOCATION_CODE)
                    } catch (e:Exception) {
                        Log.d("Location", "Error getting location settings")
                    }
                } else {
                    Snackbar.make(binding.selectedFragment, "Current Location is needed for this app to work", Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, null).show()
                }

            }
        }
    }

    private fun askPermissions() {
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }

        requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        getLocationAccess()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ( grantResults.any {
            it ==  PackageManager.PERMISSION_DENIED
            } || grantResults.isEmpty()) {

            Snackbar.make(binding.selectedFragment, "Location Access Needed to run this app", Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings" , View.OnClickListener {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity?.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }).show()

        } else {

           if (!checkPermissions() ){
               askPermissions()
           }

        }

    }

    private fun checkPermissions(): Boolean {

      val foreGroundPermission =
          ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val backGroundPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        return foreGroundPermission && backGroundPermission

    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (!checkPermissions()) {
            askPermissions()
        } else {
            map.isMyLocationEnabled = true
            map.uiSettings?.isMyLocationButtonEnabled = true
        }
       val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.result.latitude, it.result.longitude), 15f))
            }
        }

    }



}
