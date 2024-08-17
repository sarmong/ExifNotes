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

enum class RollSortMode {
    Date,
    Name,
    Camera;

    val comparator: Comparator<Roll> get() = when (this) {
        Date -> compareByDescending { roll ->
            roll.date
        }
        Name -> compareByDescending<Roll> { roll ->
            val numberPrefixRegex = "^(\\d+).*".toRegex(RegexOption.DOT_MATCHES_ALL)
            val result = numberPrefixRegex.matchEntire(roll.name ?: "")
            val numberPrefix = result?.groups?.get(1)?.value?.toIntOrNull()
            // Use descending order and reverse values
            // to make nulls (roll names with no number prefix) appear last in the list.
            numberPrefix?.let { -it }
        }.thenBy { roll ->
            roll.name
        }.thenByDescending { roll ->
            roll.developed ?: roll.unloaded ?: roll.date
        }
        Camera -> compareBy<Roll> { roll ->
            roll.camera
        }.thenByDescending { roll ->
            roll.developed ?: roll.unloaded ?: roll.date
        }
    }
}
