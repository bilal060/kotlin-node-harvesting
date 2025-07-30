package com.devicesync.app.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.devicesync.app.utils.DynamicStringManager

class TranslatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    
    private var stringKey: String? = null
    
    init {
        // Get the string key from attributes if available
        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.text))
        try {
            val textResId = typedArray.getResourceId(0, 0)
            if (textResId != 0) {
                // Extract the string key from the resource name
                val resourceName = resources.getResourceEntryName(textResId)
                stringKey = resourceName
            }
        } finally {
            typedArray.recycle()
        }
    }
    
    /**
     * Set text using a string key that will be automatically translated
     */
    fun setTranslatedText(key: String) {
        stringKey = key
        val translatedText = DynamicStringManager.getStringSync(key, context)
        text = translatedText
    }
    
    /**
     * Set text directly (will not be translated)
     */
    fun setDirectText(text: String) {
        stringKey = null
        this.text = text
    }
    
    /**
     * Refresh the text with current language
     */
    fun refreshText() {
        stringKey?.let { key ->
            val translatedText = DynamicStringManager.getStringSync(key, context)
            text = translatedText
        }
    }
} 