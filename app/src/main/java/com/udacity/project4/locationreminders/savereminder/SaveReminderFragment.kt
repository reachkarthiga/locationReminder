package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.PERMISSION_REQUEST_CODE
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

const val GEOFENCE_REQUEST_CODE = 1
const val ACTION_GEOFENCE = "Location_Reminder_Geofence"

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private val pendingIntent: PendingIntent by lazy {

        val intent = Intent(this.requireContext(), GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE
        }

        PendingIntent.getBroadcast(requireContext(),
            GEOFENCE_REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(this.requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminderDataItem =
                ReminderDataItem(title, description, location, latitude, longitude)

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                _viewModel.validateAndSaveReminder(
                    reminderDataItem
                )
                addGeoFence(reminderDataItem)
            }

//            Done: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
        }

    }

    private fun checkPermission() {

        val foreGroundPermission =
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val backGroundPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        if (!(foreGroundPermission && backGroundPermission)) {
            var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }

            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                PERMISSION_REQUEST_CODE
            )

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.any { it == PackageManager.PERMISSION_DENIED } || grantResults.isEmpty()) {
            Toast.makeText(
                this@SaveReminderFragment.requireContext(),
                "Adding GeoFence Failed!",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    @SuppressLint("MissingPermission")
    private fun addGeoFence(reminderDataItem: ReminderDataItem) {

        checkPermission()

        val geoFence = Geofence.Builder()
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(_viewModel.latitude.value!!, _viewModel.longitude.value!!, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geoFenceRequest = GeofencingRequest.Builder()
            .addGeofence(geoFence)
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()


        geofencingClient.addGeofences(geoFenceRequest, pendingIntent).run {
            addOnFailureListener {
                Toast.makeText(
                    this@SaveReminderFragment.requireContext(),
                    "Adding GeoFence Failed!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            addOnSuccessListener {
                Log.i("GeoFence", "Geofence added")
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
