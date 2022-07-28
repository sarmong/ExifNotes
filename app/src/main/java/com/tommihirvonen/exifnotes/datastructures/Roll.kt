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

package com.tommihirvonen.exifnotes.datastructures

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Roll(var id: Long = 0,
           var name: String? = null,
           var date: DateTime? = null,
           var unloaded: DateTime? = null,
           var developed: DateTime? = null,
           var note: String? = null,
           var camera: Camera? = null,
           var iso: Int = 0,
           var pushPull: String? = null,
           private var format_: Int = 0,
           var archived: Boolean = false,
           var filmStock: FilmStock? = null
           ) : Parcelable {

    init {
        if (format_ !in 0..3) format_ = 0
    }

    var format: Int
        get() = if (format_ in 0..3) format_ else 0
        set(value) { if (value in 0..3) format_ = value }

    override fun equals(other: Any?) = other is Roll && other.id == id

    override fun hashCode() = id.hashCode()
}

fun List<Roll>.sorted(sortMode: RollSortMode): List<Roll> = sortedWith(sortMode.comparator)