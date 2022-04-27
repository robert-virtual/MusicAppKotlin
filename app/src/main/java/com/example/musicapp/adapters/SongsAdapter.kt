package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.model.Audio

class SongsAdapter(private val songsList:List<Audio>,private val onClickListener:(Audio)->Unit):RecyclerView.Adapter<SongsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SongsViewHolder(layoutInflater .inflate(R.layout.song_item,parent,false))
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        holder.render(songsList[position],onClickListener)
    }

    override fun getItemCount(): Int {
        return songsList.size
    }
}