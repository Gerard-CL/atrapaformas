package com.example.atrapaformas

import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Jugador(
    // Valores que se piden al usuario
    val nombre: String,
    val edad: Int,

    // Valores que se determinan automáticamente
    var puntuacion: Int = 0,
    var tiempoJuego: Int = 0, // en segundos
    val dificultad: String = "",
    val fechaJugada: String = "",
    val horaApertura: String = ""
                  ) {
    companion object {
        fun crearJugador(nombre: String, edad: Int, dificultadPersonalizada: String? = null): Jugador {
            val ahora = Calendar.getInstance().time
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            return Jugador(
                nombre = nombre,
                edad = edad,
                dificultad = dificultadPersonalizada ?: determinarDificultad(edad),
                fechaJugada = formatoFecha.format(ahora),
                horaApertura = formatoHora.format(ahora)
                          )
        }

        private fun determinarDificultad(edad: Int): String {
            return when (edad) {
                in 0..5 -> "Fácil"
                in 6..8 -> "Medio"
                else -> "Difícil"
            }
        }
    }

    fun actualizarPuntuacion(nuevaPuntuacion: Int) {
        this.puntuacion = nuevaPuntuacion
    }

    fun actualizarTiempo(segundos: Int) {
        this.tiempoJuego = segundos
    }
}