package com.ifpr.androidapptemplate

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ifpr.androidapptemplate.databinding.ActivityMainBinding
import com.ifpr.androidapptemplate.ui.firebase.MyFirebaseMessagingService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,
                R.id.navigation_profile
            )
        )

        navView.setupWithNavController(navController)

        // Monitora materiais pendentes e dispara notificação para aprovadores
        MyFirebaseMessagingService.monitorarPendentes(this)

        // Navega para notificações se aberto via notificação
        intent.getStringExtra("nav_to")?.let { destino ->
            if (destino == "notifications") {
                navController.navigate(R.id.navigation_notifications)
            }
        }
    }
}