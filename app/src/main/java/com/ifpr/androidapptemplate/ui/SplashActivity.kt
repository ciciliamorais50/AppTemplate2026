package com.ifpr.androidapptemplate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val triangle = findViewById<ImageView>(R.id.splash_triangle)
        val name = findViewById<TextView>(R.id.splash_name)
        val subtitle = findViewById<TextView>(R.id.splash_subtitle)

        // Começa com triângulo sem olho
        triangle.setImageResource(R.drawable.ic_triangle)

        val fadeTriangle = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeEye = AnimationUtils.loadAnimation(this, R.anim.fadein_eye)
        val fadeName = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow)

        triangle.visibility = View.VISIBLE
        name.visibility = View.VISIBLE
        subtitle.visibility = View.VISIBLE

        // Triângulo aparece
        triangle.startAnimation(fadeTriangle)

        // Olho fechado aparece
        triangle.postDelayed({
            triangle.setImageResource(R.drawable.ic_eye_closed)
        }, 1200)

        // Olho abre
        triangle.postDelayed({
            triangle.setImageResource(R.drawable.ic_spectra)
        }, 2000)

        // Nome aparece
        name.startAnimation(fadeName)
        subtitle.startAnimation(fadeName)

        // Vai para login
        triangle.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 4000)
    }
}