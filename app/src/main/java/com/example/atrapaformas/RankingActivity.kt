package com.example.atrapaformas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.File
import java.util.Collections

// 1. Clase de datos simple para manejar la información
data class Puntuacion(val nombre: String, val puntos: Int) : Comparable<Puntuacion> {
    // Esto permite ordenar la lista automáticamente de mayor a menor
    override fun compareTo(other: Puntuacion): Int = other.puntos.compareTo(this.puntos)
}

class RankingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ranking)

        // Ajuste de padding para barras del sistema (Edge to Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBotones()
        cargarRanking()
    }

    private fun setupBotones() {
        val buttonJugarOtraVez = findViewById<ImageButton>(R.id.btn_play_again)
        buttonJugarOtraVez.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // Limpiamos la pila para que no pueda volver atrás al ranking
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun cargarRanking() {
        // Ejecutamos la lectura de archivos en un hilo secundario para no bloquear la UI
        Thread {
            val listaPuntuaciones = leerArchivosJson()

            // Volvemos al hilo principal para pintar la pantalla
            runOnUiThread {
                actualizarVistas(listaPuntuaciones)
            }
        }.start()
    }

    private fun leerArchivosJson(): List<Puntuacion> {
        val lista = ArrayList<Puntuacion>()

        // Ruta: /data/data/com.example.atrapaformas/files/partidas
        val directorioPartidas = File(filesDir, "partidas")

        if (directorioPartidas.exists() && directorioPartidas.isDirectory) {
            val archivos = directorioPartidas.listFiles()

            archivos?.forEach { archivo ->
                try {
                    if (archivo.isFile && archivo.name.endsWith(".json")) {
                        val contenido = archivo.readText()
                        val jsonRaiz = JSONObject(contenido)

                        // 1. Verificamos si existe el objeto "jugador" antes de leer
                        if (jsonRaiz.has("jugador")) {

                            // 2. Entramos al objeto "jugador"
                            val jsonJugador = jsonRaiz.getJSONObject("jugador")

                            // 3. Ahora sí extraemos nombre y puntuacion de dentro de 'jugador'
                            val nombre = jsonJugador.optString("nombre", "Desconocido")

                            // Nota: Tu JSON tiene "puntuacion" como número (40), así que usamos optInt
                            val puntos = jsonJugador.optInt("puntuacion", 0)

                            lista.add(Puntuacion(nombre, puntos))
                        }
                    }
                } catch (e: Exception) {
                    // Logueamos el error para verlo en el Logcat si algo falla
                    Log.e("RankingActivity", "Error al procesar el archivo ${archivo.name}", e)
                }
            }
        }

        // Ordenamos de mayor a menor y cogemos el Top 5
        Collections.sort(lista)
        return lista.take(5)
    }

    private fun actualizarVistas(topScores: List<Puntuacion>) {
        // Mapeo de IDs de tu XML para iterar fácilmente
        val textViewsIds = listOf(
            Pair(R.id.usuario1name, R.id.usuario1puntos),
            Pair(R.id.usuario2name, R.id.usuario2puntos),
            Pair(R.id.usuario3name, R.id.usuario3puntos),
            Pair(R.id.usuario4name, R.id.usuario4puntos),
            Pair(R.id.usuario5name, R.id.usuario5puntos)
                                 )

        // Iteramos por los 5 huecos disponibles
        for (i in textViewsIds.indices) {
            val (idNombre, idPuntos) = textViewsIds[i]
            val tvNombre = findViewById<TextView>(idNombre)
            val tvPuntos = findViewById<TextView>(idPuntos)

            if (i < topScores.size) {
                // Si hay datos para esta posición, los mostramos
                val score = topScores[i]
                tvNombre.text = score.nombre
                tvPuntos.text = score.puntos.toString()
            } else {
                // Si no hay datos (ej: solo hay 3 partidas jugadas), vaciamos los campos 4 y 5
                tvNombre.text = "---"
                tvPuntos.text = "0"
            }
        }
    }
}