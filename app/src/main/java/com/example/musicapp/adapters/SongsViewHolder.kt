package com.example.musicapp.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.databinding.SongItemBinding
import com.example.musicapp.model.Audio

class SongsViewHolder(view:View):RecyclerView.ViewHolder(view) {
    val binding = SongItemBinding.bind(view)

    fun render(audio:Audio){
        binding.songTitle.text = audio.name

        var artist = audio.artist
        if (artist == "<unknown>") artist = "Artista Desconocido"
        binding.songArtist.text = artist

        binding.songDuration.text = audio.duration.toString()
    }
}