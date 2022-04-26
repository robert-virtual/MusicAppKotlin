package com.example.musicapp.model

import android.net.Uri

data class Audio (
val uri: Uri,
val name:String,
val artist:String,
val duration:Int,
val size:Int
)
