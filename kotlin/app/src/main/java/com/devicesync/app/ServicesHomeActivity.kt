package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.ServiceCardAdapter
import com.devicesync.app.data.Service
import com.devicesync.app.utils.LanguageManager
import com.devicesync.app.viewmodels.ServicesViewModel

class ServicesHomeActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ServicesViewModel
    private lateinit var adapter: ServiceCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var viewAllButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_home)
        
        // Apply current language
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        // No toolbar in this layout, so we skip toolbar setup
        
        recyclerView = findViewById(R.id.servicesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        viewAllButton = findViewById(R.id.viewAllButton)
        
        // Setup RecyclerView
        adapter = ServiceCardAdapter(
            onServiceClick = { _ ->
                // Navigate to services list
                val intent = Intent(this, ServicesListActivity::class.java)
                startActivity(intent)
            },
            onFavoriteClick = { service ->
                viewModel.toggleFavorite(service)
            }
        )
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        
        // Setup button click
        viewAllButton.setOnClickListener {
            val intent = Intent(this, ServicesListActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ServicesViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.services.observe(this) { services ->
            // Show first 4 services on home screen
            val featuredServices = services.take(4)
            adapter.updateServices(featuredServices)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            if (error != null) {
                errorText.text = error
                errorText.visibility = View.VISIBLE
            } else {
                errorText.visibility = View.GONE
            }
        }
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Монгол", "Русский", "中文", "Қазақша")
        val languageCodes = arrayOf("en", "mn", "ru", "zh", "kk")
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                setAppLanguage(languageCodes[which])
            }
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
            listView.post {
                for (i in 0 until listView.count) {
                    val child = listView.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(resources.getColor(R.color.text_dark, theme))
                        child.textSize = 16f
                    }
                }
            }
        }
    }
    
    private fun setAppLanguage(languageCode: String) {
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 