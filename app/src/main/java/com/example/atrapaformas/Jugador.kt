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
    val puntuacion: Int = 0,
    val tiempoJuego: Int = 0,
    val dificultad: String = "",
    val fechaJugada: String = "",
    val horaApertura: String = "",
    var horaFin: String = "",

    ) {
    companion object {
        fun crearJugador(nombre: String, edad: Int, dificultad: String): Jugador {
            val ahora = Calendar.getInstance().time
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())


            return Jugador(
                nombre = nombre,
                edad = edad,
                dificultad = dificultad,
                fechaJugada = formatoFecha.format(ahora),
                horaApertura = formatoHora.format(ahora),
                horaFin = ""
                          )
        }
    }
    // Función extra dentro de la clase Jugador (fuera del companion object)
    fun terminarPartida() {
        val ahora = Calendar.getInstance().time
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Aquí reutilizamos el formatoHora, no hace falta crear uno nuevo llamado "formatoFin"
        this.horaFin = formatoHora.format(ahora)
    }
}