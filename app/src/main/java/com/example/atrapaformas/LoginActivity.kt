package com.example.atrapaformas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val button_facil = findViewById<ImageButton>(R.id.button_facil)
        val button_medio = findViewById<ImageButton>(R.id.button_medio)
        val button_dificil = findViewById<ImageButton>(R.id.button_dificil)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        button_facil.setOnClickListener {
            val intent = Intent(this, JuegoFacilActivity::class.java)
            startActivity(intent)
        }
        button_medio.setOnClickListener {
            val intent = Intent(this, JuegoMedioActivity::class.java)
            startActivity(intent)
        }
        button_dificil.setOnClickListener {
            val intent = Intent(this, JuegoDificilActivity::class.java)
            startActivity(intent)
        }
    }
}