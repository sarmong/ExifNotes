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

package com.tommihirvonen.exifnotes.utilities

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.View
import android.view.animation.Interpolator
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.transition.TransitionSet
import com.google.android.material.snackbar.Snackbar
import com.tommihirvonen.exifnotes.datastructures.Gear
import java.io.*

fun <T> T.validate(vararg validations: (T) -> (Pair<Boolean, String>)): Pair<Boolean, String> =
    validations.map { it(this) }.firstOrNull { !it.first } ?: (true to "")


val Context.packageInfo: PackageInfo? get() {
    try {
        return packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return null
}

/**
 * Removes potential illegal characters from a string to make it a valid file name.
 */
fun String.illegalCharsRemoved(): String = replace("[|\\\\?*<\":>/]".toRegex(), "_")

/**
 * Remove all files in a directory. Subdirectories are skipped.
 */
fun File.purgeDirectory() = this.listFiles()?.filterNot { it.isDirectory }?.forEach { it.delete() }

fun File.makeDirsIfNotExists() { if (!isDirectory) mkdirs() }

fun List<Gear>.toStringList(): String =
    if (this.isEmpty()) "" else this.joinToString(separator = "\n-", prefix = "\n-") { it.name }

fun TransitionSet.setCommonInterpolator(interpolator: Interpolator): TransitionSet = apply {
    (0 until transitionCount).map { index -> getTransitionAt(index) }
        .forEach { transition -> transition?.interpolator = interpolator }
}

fun View.snackbar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(this, resId, duration).show()

fun View.snackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(this, text, duration).show()

fun View.snackbar(@StringRes resId: Int, anchorView: View, duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(this, resId, duration).setAnchorView(anchorView).show()

fun View.snackbar(text: CharSequence, anchorView: View, duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(this, text, duration).setAnchorView(anchorView).show()

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, resId, duration).show()