package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.utils.LanguageManager

class LanguageSelectionActivity : AppCompatActivity() {
    
    private lateinit var languageSpinner: Spinner
    private lateinit var continueButton: Button
    private lateinit var welcomeTitle: TextView
    private lateinit var welcomeSubtitle: TextView
    private lateinit var languageTitle: TextView
    private lateinit var languageSubtitle: TextView
    private lateinit var feature1Title: TextView
    private lateinit var feature2Title: TextView
    private lateinit var feature3Title: TextView
    
    private var selectedLanguageCode = "en"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if this is the first launch
        val isFirstLaunch = !getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("first_launch_completed", false)
        
        if (!isFirstLaunch) {
            // Not first launch, go directly to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_language_selection)
        
        initializeViews()
        setupLanguageSpinner()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        languageSpinner = findViewById(R.id.languageSpinner)
        continueButton = findViewById(R.id.continueButton)
        welcomeTitle = findViewById(R.id.welcomeTitle)
        welcomeSubtitle = findViewById(R.id.welcomeSubtitle)
        languageTitle = findViewById(R.id.languageTitle)
        languageSubtitle = findViewById(R.id.languageSubtitle)
        feature1Title = findViewById(R.id.feature1Title)
        feature2Title = findViewById(R.id.feature2Title)
        feature3Title = findViewById(R.id.feature3Title)
    }
    
    private fun setupLanguageSpinner() {
        val languages = listOf(
            LanguageOption("English", "en"),
            LanguageOption("Монгол", "mn"),
            LanguageOption("Русский", "ru"),
            LanguageOption("中文", "zh")
        )
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages.map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
        
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedLanguageCode = languages[position].code
                updateContentLanguage(selectedLanguageCode)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default to English
                selectedLanguageCode = "en"
            }
        }
    }
    
    private fun setupClickListeners() {
        continueButton.setOnClickListener {
            // Save the selected language
            LanguageManager.setAppLanguage(this, selectedLanguageCode)
            
            // Mark that the app has been launched before
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("first_launch_completed", true)
                .apply()
            
            // Navigate to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateContentLanguage(languageCode: String) {
        val content = getLocalizedContent(languageCode)
        
        welcomeTitle.text = content.welcomeTitle
        welcomeSubtitle.text = content.welcomeSubtitle
        languageTitle.text = content.languageTitle
        languageSubtitle.text = content.languageSubtitle
        continueButton.text = content.continueButton
        feature1Title.text = content.feature1Title
        feature2Title.text = content.feature2Title
        feature3Title.text = content.feature3Title
    }
    
    private fun getLocalizedContent(languageCode: String): LocalizedContent {
        return when (languageCode) {
            "mn" -> LocalizedContent(
                welcomeTitle = "Дубай нээлтийн тавтай морил",
                welcomeSubtitle = "Дубайн ид шидийн аяллын таны эцсийн заавар",
                languageTitle = "Хэлээ сонгоно уу",
                languageSubtitle = "Эхлэхийн тулд хүссэн хэлээ сонгоно уу",
                continueButton = "Үргэлжлүүлэх",
                feature1Title = "Мэргэжлийн зааварчид",
                feature2Title = "Ухаалаг төлөвлөлт",
                feature3Title = "Орон нутгийн туршлага"
            )
            "ru" -> LocalizedContent(
                welcomeTitle = "Добро пожаловать в Dubai Discoveries",
                welcomeSubtitle = "Ваш путеводитель по магии Дубая",
                languageTitle = "Выберите язык",
                languageSubtitle = "Выберите предпочитаемый язык для начала",
                continueButton = "Продолжить",
                feature1Title = "Экспертные гиды",
                feature2Title = "Умное планирование",
                feature3Title = "Местный опыт"
            )
            "zh" -> LocalizedContent(
                welcomeTitle = "欢迎来到迪拜探索",
                welcomeSubtitle = "探索迪拜魔法的终极指南",
                languageTitle = "选择您的语言",
                languageSubtitle = "选择您偏好的语言开始使用",
                continueButton = "继续",
                feature1Title = "专业导游",
                feature2Title = "智能规划",
                feature3Title = "当地体验"
            )
            else -> LocalizedContent(
                welcomeTitle = "Welcome to Dubai Discoveries",
                welcomeSubtitle = "Your ultimate guide to exploring the magic of Dubai",
                languageTitle = "Choose Your Language",
                languageSubtitle = "Select your preferred language to get started",
                continueButton = "Continue",
                feature1Title = "Expert Guides",
                feature2Title = "Smart Planning",
                feature3Title = "Local Experiences"
            )
        }
    }
    
    data class LanguageOption(val displayName: String, val code: String)
    
    data class LocalizedContent(
        val welcomeTitle: String,
        val welcomeSubtitle: String,
        val languageTitle: String,
        val languageSubtitle: String,
        val continueButton: String,
        val feature1Title: String,
        val feature2Title: String,
        val feature3Title: String
    )
} 