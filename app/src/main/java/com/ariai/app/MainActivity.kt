package com.ariai.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // یک رابط کاربری ساده با کد برای تست اینکه برنامه باز می‌شود
        val textView = TextView(this)
        textView.text = "سلام! اپلیکیشن خالص AriAi با موفقیت اجرا شد! 🎉"
        textView.textSize = 20f
        textView.setPadding(50, 100, 50, 50)
        
        setContentView(textView)
    }
}
