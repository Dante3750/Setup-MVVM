package com.life.finalversioncalander

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.life.finalversioncalander.databinding.ActivityCalendarBinding

class CalendarActivity : AppCompatActivity() {

    internal lateinit var binding: ActivityCalendarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.homeToolbar)
    }
}