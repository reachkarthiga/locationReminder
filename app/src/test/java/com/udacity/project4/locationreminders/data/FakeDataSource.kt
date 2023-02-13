package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource (val list : MutableList<ReminderDTO>? = mutableListOf()): ReminderDataSource {

//    Done: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
       list?.let {
           return Result.Success(ArrayList(it))
       }
        return Result.Error("NO Reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        list?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO()
    }

    override suspend fun deleteAllReminders() {
        list?.clear()
    }


}