package com.example.atrapaformas

import kotlinx.serialization.Serializable

@Serializable
data class Jugador(
    // Datos que pides al usuario (obligatorios)
    val nombre: String,
    val edad: Int,
    val dificultad: String,

    // Datos automáticos (tienen valor por defecto, no hace falta pasarlos)
    val puntuacion: Int = 0,
    val tiempoJuego: Int = 0,
    val fechaJugada: String = TimeUtils.getFecha(),
    val horaApertura: String = TimeUtils.getHora(),
    val horaFin: String = ""
                  )