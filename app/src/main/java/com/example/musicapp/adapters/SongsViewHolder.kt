package com.example.musicapp.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.databinding.SongItemBinding
import com.example.musicapp.model.Audio

class SongsViewHolder(view:View):RecyclerView.ViewHolder(view) {
    val binding = SongItemBinding.bind(view)

    fun render(audio:Audio,onClickListener:(Audio)->Unit){
        binding.songTitle.text = audio.name.trim().substring(0,audio.name.lastIndexOf("."))
        binding.songItem.setOnClickListener {
            onClickListener(audio)
        }
        var artist = audio.artist
        if (artist == "<unknown>") artist = "Artista Desconocido"
        artist += "-${audio.duration}"
        binding.songArtist.text = artist
        var seconds = audio.duration/1000
        val minutes = seconds/60
        seconds = seconds-(minutes*60)
        var secondsString = seconds.toString()

        if (secondsString.length == 1){
            secondsString = "0${seconds}"
        }
        binding.songDuration.text = "${minutes}:${secondsString}"
    }
}