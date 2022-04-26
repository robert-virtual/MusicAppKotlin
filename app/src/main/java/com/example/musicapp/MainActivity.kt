package com.example.musicapp

import android.app.Activity
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapters.SongsAdapter
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.model.Audio

class MainActivity : AppCompatActivity() {
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            title = it.resultCode.toString()
            if (it.resultCode == Activity.RESULT_OK) {
                getAudioFiles()
            }
        }
    private lateinit var binding:ActivityMainBinding
    private val audioList = mutableListOf<Audio>()
    private val adapter = SongsAdapter(audioList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askPermission()
        initRecycler()

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
        binding.songsList.adapter = adapter
        binding.songsList.layoutManager = LinearLayoutManager(this)
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
        val selection = ""
        applicationContext.contentResolver.query(
            audioCollection,
            projection,
            null,
            null,
           null
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
                adapter.notifyItemInserted(audioList.size-1)
                //audioList += Audio(contentUri,name, artist,duration,size)

            }
            binding.loader.visibility = View.GONE

        }
    }
}