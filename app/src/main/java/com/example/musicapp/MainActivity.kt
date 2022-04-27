package com.example.musicapp

import android.app.Activity
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapters.SongsAdapter
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.model.Audio
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var mMediaPlayer: MediaPlayer? = null
    private val viewModel:MainViewModel by viewModels()
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            getAudioFiles()
        }
    private lateinit var binding:ActivityMainBinding
    private val audioListCopy = mutableListOf<Audio>()
    private val audioList = mutableListOf<Audio>()
    private val adapter = SongsAdapter(audioList){it,idx->
        onSelectAudioItem(it,idx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askPermission()
        initRecycler()
        setCurrentSong()
        initButtonListeners()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)
        val search  = menu.findItem(R.id.search_icon)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Buscar cancion"
        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchSongs(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                searchSongs(query)
                return false
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
    fun searchSongs(query:String?){

        if (query != null){
            val results = audioListCopy.filter { it.name.contains(query) }
            audioList.clear()
            audioList.addAll(results)
        }else{
            audioList.clear()
            audioList.addAll(audioListCopy)
        }
        adapter.notifyDataSetChanged()
    }
    fun initButtonListeners(){
        binding.btnPlay.setOnClickListener {
            if (mMediaPlayer != null){
                if (mMediaPlayer!!.isPlaying){
                    mMediaPlayer?.pause()
                    viewModel.pause(mMediaPlayer!!.currentPosition)
                    binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow)
                }else{
                    mMediaPlayer?.seekTo(viewModel.length!!)
                    mMediaPlayer?.start()
                    binding.btnPlay.setBackgroundResource(R.drawable.ic_pause)
                }
            }

        }
        binding.btnNext.setOnClickListener {
            if (viewModel.idx != null){
                var idx = viewModel.idx!!+1
                if (idx == audioList.size){
                    idx = 0
                    onSelectAudioItem(audioList[idx],idx)
                }else{
                    onSelectAudioItem(audioList[idx],idx)
                }
            }
        }
    }
    fun setCurrentSong(){
        if (viewModel.audio != null){
            binding.playing.visibility = View.VISIBLE
            binding.songTitle.text = viewModel.audio!!.name
            binding.songArtist.text = viewModel.audio!!.artist
        }

    }
    fun onSelectAudioItem(audio:Audio,idx:Int){
        viewModel.playSong(audio,idx)
        if (viewModel.audio != null){
            mMediaPlayer?.release()
            mMediaPlayer = null
            mMediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext,viewModel.audio!!.uri)
                prepare()
                start()
            }
        }
        setCurrentSong()
    }
    fun askPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                val builder = AlertDialog.Builder(this)
        builder.setTitle("Music App")
        builder.setMessage("Necesita otorgar permisos para acceder a los archivos del dispositivo")
        builder.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                    val i = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    getResult.launch(i)
        }
        builder.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, i: Int ->
            getAudioFiles()
        }
        builder.show()
            }else{
                getAudioFiles()
            }
        }
    }
    fun initRecycler(){
        val manager = LinearLayoutManager(this)
        binding.songsList.adapter = adapter
        binding.songsList.layoutManager = manager
        //val decoration = DividerItemDecoration(this,manager.orientation)
        //binding.songsList.addItemDecoration(decoration)

    }
    fun getAudioFiles(){
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.SIZE,
        )
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(1,TimeUnit.MINUTES).toString()
        )
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        applicationContext.contentResolver.query(
            audioCollection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (cursor.moveToNext()){
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val artist = cursor.getString(artistColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                audioList.add(Audio(contentUri,name, artist,duration,size))
                audioListCopy.add(Audio(contentUri,name, artist,duration,size))
                adapter.notifyItemInserted(audioList.size-1)
                //audioList += Audio(contentUri,name, artist,duration,size)

            }
            binding.loader.visibility = View.GONE

        }

    }
}