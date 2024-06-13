package com.example.rayzi

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.rayzi.databinding.ActivityHomeScreenBinding

class Home_ScreenActivity : AppCompatActivity() {

private lateinit var binding: ActivityHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityHomeScreenBinding.inflate(layoutInflater)
     setContentView(binding.root)

    }

}