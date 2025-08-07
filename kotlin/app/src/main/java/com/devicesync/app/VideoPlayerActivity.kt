package com.devicesync.app

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var videoView: VideoView
    private lateinit var mediaController: MediaController

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        
        setupViews()
        setupToolbar()
        setupVideoPlayer()
        loadVideo()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        videoView = findViewById(R.id.videoView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Video Player"
        supportActionBar?.title = title
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupVideoPlayer() {
        mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
    }

    private fun loadVideo() {
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        if (videoUrl != null) {
            try {
                val uri = Uri.parse(videoUrl)
                videoView.setVideoURI(uri)
                videoView.requestFocus()
                videoView.start()
            } catch (e: Exception) {
                // Handle video loading error
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 