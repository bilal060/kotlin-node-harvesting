package com.devicesync.app

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.devicesync.app.adapters.GalleryAdapter
import android.content.Intent

class GalleryActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var galleryAdapter: GalleryAdapter

    companion object {
        const val EXTRA_IMAGES = "extra_images"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        
        setupViews()
        setupToolbar()
        setupRecyclerView()
        loadImages()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Gallery"
        supportActionBar?.title = title
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        galleryAdapter = GalleryAdapter { imageUrl ->
            // Handle image click - could open full screen viewer
            openFullScreenImage(imageUrl)
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@GalleryActivity, 2)
            adapter = galleryAdapter
        }
    }

    private fun loadImages() {
        val images = intent.getStringArrayListExtra(EXTRA_IMAGES) ?: arrayListOf()
        galleryAdapter.updateImages(images)
    }

    private fun openFullScreenImage(imageUrl: String) {
        val intent = Intent(this, FullScreenImageActivity::class.java).apply {
            putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, imageUrl)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 