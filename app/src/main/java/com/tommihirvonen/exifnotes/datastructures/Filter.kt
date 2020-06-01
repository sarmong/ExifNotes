package com.tommihirvonen.exifnotes.datastructures

import android.os.Parcel
import android.os.Parcelable

/**
 * Filter class holds the information of a photographic filter.
 */
class Filter : Gear, Parcelable, Comparable<Gear> {

    constructor() : super()

    private constructor(pc: Parcel) : super(pc)

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Filter> {
        override fun createFromParcel(parcel: Parcel): Filter {
            return Filter(parcel)
        }

        override fun newArray(size: Int): Array<Filter?> {
            return arrayOfNulls(size)
        }
    }

}
