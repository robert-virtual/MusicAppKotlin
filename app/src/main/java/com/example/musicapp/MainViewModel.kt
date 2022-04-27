package com.example.musicapp

import androidx.lifecycle.ViewModel
import com.example.musicapp.model.Audio

class MainViewModel:ViewModel() {
    var audio:Audio? = null
    var length:Int? = null
    var idx:Int? = null

    fun playSong(_audio:Audio,_idx:Int){
        idx = _idx
        audio = _audio
    }

    fun pause(_length:Int){
        length = _length
    }
}