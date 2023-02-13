package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initialSetUp() {
        reminderDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource )
    }

    @Test
    fun onClear_clearsLiveDataObjects() {
        saveReminderViewModel.reminderTitle.value = "Place 1"
        saveReminderViewModel.latitude.value = 13.23
        saveReminderViewModel.longitude.value = 12.2
        saveReminderViewModel.selectedPOI.value = PointOfInterest(LatLng(2.0,3.0), null, null)
        saveReminderViewModel.reminderSelectedLocationStr.value = "Selected Place 1"
        saveReminderViewModel.reminderDescription.value = "Place 1 Description"

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.value , `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value , `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.value , `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.value , `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value , `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.value , `is`(nullValue()))

    }



    //TODO: provide testing to the SaveReminderView and its live data objects


}
