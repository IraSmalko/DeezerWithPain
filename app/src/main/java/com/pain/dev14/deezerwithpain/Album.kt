package com.pain.dev14.deezerwithpain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import paperparcel.PaperParcel
import paperparcel.PaperParcelable


/**
 * Created by dev14 on 15.08.17.
 */
data class Album(val id: Long,
                 val title: String,
                 val tracks: Tracks)

@PaperParcel
data class Data(val id: Long,
                val title: String,
                val artist: Artist,
                val preview: String) : PaperParcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelData.CREATOR
    }
}

@PaperParcel
data class Artist(val id: Long,
                  val name: String) : PaperParcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelArtist.CREATOR
    }
}

@PaperParcel
data class Tracks(val data: List<Data>) : PaperParcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelTracks.CREATOR
    }
}