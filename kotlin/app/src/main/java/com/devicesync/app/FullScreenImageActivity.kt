package com.devicesync.app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var photoView: PhotoView

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)
        
        setupViews()
        setupToolbar()
        loadImage()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        photoView = findViewById(R.id.photoView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Image"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadImage() {
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(photoView)
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