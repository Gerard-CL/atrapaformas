package com.example.atrapaformas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FinReinicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fin_reinicio)
        val botonRanking: ImageButton = findViewById(R.id.btn_ver_ranking)
        val botonJugar: ImageButton = findViewById(R.id.btn_play_again)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        botonRanking.setOnClickListener {
            // Le dices qué hacer:
            // (Asegúrate de que tu pantalla de ranking se llame "RankingActivity")
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        botonJugar.setOnClickListener {
            // Aquí pones la lógica para jugar de nuevo.
            // Por ejemplo, cerrar esta pantalla y volver al inicio
            // o reiniciar la actividad del juego.

            // Ejemplo: Cierra esta pantalla (FinReinicioActivity)
            finish()
        }
    }
}