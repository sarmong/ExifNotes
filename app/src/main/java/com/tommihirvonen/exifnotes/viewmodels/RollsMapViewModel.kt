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
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.tommihirvonen.exifnotes.core.entities.Frame
import com.tommihirvonen.exifnotes.core.entities.Roll
import com.tommihirvonen.exifnotes.core.entities.RollFilterMode
import com.tommihirvonen.exifnotes.data.repositories.FrameRepository
import com.tommihirvonen.exifnotes.data.repositories.RollRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RollsMapViewModel(application: Application,
                        private val rollRepository: RollRepository,
                        private val frameRepository: FrameRepository,
                        private val filterMode: RollFilterMode
)
    : AndroidViewModel(application) {

    private val markerBitmaps = ViewModelUtility.getMarkerBitmaps(application)

    val rolls: StateFlow<List<RollData>> get() = mRolls

    private val mRolls = MutableStateFlow(emptyList<RollData>())

    init {
        loadData()
    }

    fun setSelections(selections: List<Roll>) {
        val current = mRolls.value
        val updated = current.map {
            val selected = selections.contains(it.roll)
            it.copy(selected = selected)
        }
        updated.filter(RollData::selected).forEachIndexed { index, data ->
           val i = index % markerBitmaps.size
           data.marker = markerBitmaps[i]
        }
        mRolls.value = updated
    }

    private fun loadData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val rolls = rollRepository.getRolls(filterMode)
                val data = rolls.mapIndexed { index, roll ->
                    val i = index % markerBitmaps.size
                    RollData(roll, true, markerBitmaps[i], frameRepository.getFrames(roll))
                }
                mRolls.value = data
            }
        }
    }
}

data class RollData(
    val roll: Roll,
    val selected: Boolean,
    var marker: Bitmap?,
    val frames: List<Frame>)

class RollsMapViewModelFactory(
    private val application: Application,
    private val rollRepository: RollRepository,
    private val frameRepository: FrameRepository,
    private val filterMode: RollFilterMode
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(RollsMapViewModel::class.java)) {
            return RollsMapViewModel(application, rollRepository, frameRepository, filterMode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}