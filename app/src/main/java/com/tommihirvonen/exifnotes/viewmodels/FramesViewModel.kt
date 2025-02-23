/*
 * Exif Notes
 * Copyright (C) 2022  Tommi Hirvonen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tommihirvonen.exifnotes.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.tommihirvonen.exifnotes.datastructures.Frame
import com.tommihirvonen.exifnotes.datastructures.FrameSortMode
import com.tommihirvonen.exifnotes.datastructures.Roll
import com.tommihirvonen.exifnotes.datastructures.sorted
import com.tommihirvonen.exifnotes.preferences.PreferenceConstants
import com.tommihirvonen.exifnotes.utilities.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FramesViewModel(application: Application, roll: Roll) : AndroidViewModel(application) {
    private val database = application.database
    private val sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(application.baseContext)

    val roll get() = mRoll as LiveData<Roll>
    val frames get() = mFrames as LiveData<List<Frame>>
    val frameSortMode get() = mFrameSortMode as LiveData<FrameSortMode>

    val selectedFrames = HashSet<Frame>()

    private val mRoll = MutableLiveData<Roll>().apply { value = roll }

    private val mFrames by lazy {
        MutableLiveData<List<Frame>>().also { loadFrames(roll) }
    }

    private val mFrameSortMode by lazy {
        MutableLiveData<FrameSortMode>().apply {
            value = FrameSortMode.fromValue(
                sharedPreferences.getInt(
                    PreferenceConstants.KEY_FRAME_SORT_ORDER, FrameSortMode.FRAME_COUNT.value))
        }
    }

    fun setRoll(roll: Roll) {
        mRoll.value = roll
    }

    fun setFrameSortMode(mode: FrameSortMode) {
        val editor = sharedPreferences.edit()
        editor.putInt(PreferenceConstants.KEY_FRAME_SORT_ORDER, mode.value)
        editor.apply()
        mFrameSortMode.value = mode
        mFrames.value = mFrames.value?.sorted(getApplication(), mode)
    }

    fun submitFrame(frame: Frame) {
        if (database.updateFrame(frame) == 0) {
            database.addFrame(frame)
        }
        val sortMode = mFrameSortMode.value ?: FrameSortMode.FRAME_COUNT
        mFrames.value = mFrames.value
            ?.filterNot { it.id == frame.id }
            ?.plus(frame)
            ?.sorted(getApplication(), sortMode)
    }

    fun deleteFrame(frame: Frame) {
        database.deleteFrame(frame)
        mFrames.value = mFrames.value?.minus(frame)
    }

    private fun loadFrames(roll: Roll) {
        val sortMode = mFrameSortMode.value ?: FrameSortMode.FRAME_COUNT
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mFrames.postValue(database.getFrames(roll).sorted(getApplication(), sortMode))
            }
        }
    }
}

class FramesViewModelFactory(private val application: Application, private val roll: Roll)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(FramesViewModel::class.java)) {
            return FramesViewModel(application, roll) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}