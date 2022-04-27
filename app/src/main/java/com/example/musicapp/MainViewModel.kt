package com.example.musicapp

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import com.example.musicapp.model.Audio

class MainViewModel:ViewModel() {
    var audio:Audio? = null
    var length:Int? = null
    var idx:Int? = null
    var mMediaPlayer: MediaPlayer? = null

    fun playSong(_audio:Audio,_idx:Int,applicationContext:Context){
        idx = _idx
        audio = _audio
        if (audio != null){
            mMediaPlayer?.release()
            mMediaPlayer = null
            mMediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext,audio!!.uri)
                prepare()
                start()
            }
        }
    }

    fun pause(_length:Int){
        length = _length
    }
}