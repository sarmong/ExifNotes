/*
 * Exif Notes
 * Copyright (C) 2024  Tommi Hirvonen
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

package com.tommihirvonen.exifnotes.core.entities

import android.os.Parcelable
import kotlinx.serialization.Serializable

/**
 * Abstract super class for different types of gear.
 * Defines all common member variables and methods as well as mandatory interfaces to implement.
 *
 * @property id database id of object
 * @property make make of manufacturer of the piece of gear
 * @property model model name of the piece of gear
 */

@Serializable
sealed class Gear : Parcelable, Comparable<Gear> {
    abstract val id: Long
    abstract val make: String?
    abstract val model: String?

    val name: String get() = "$make $model"

    override fun compareTo(other: Gear): Int {
        return name.compareTo(other.name, ignoreCase = true)
    }
}
