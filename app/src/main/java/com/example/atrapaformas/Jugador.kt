package com.example.atrapaformas

import kotlinx.serialization.Serializable // Para el JSON

@Serializable
data class Jugador(
    // Datos que pides al usuario (obligatorios)
    val nombre: String,
    val edad: Int,
    val dificultad: String,

    // Datos automáticos (tienen valor por defecto, no hace falta pasarlos)
    var puntuacion: Int = 0,
    var tiempoJuego: Int = 0,
    val fechaJugada: String = TimeUtils.getFecha(),
    val horaApertura: String = TimeUtils.getHora(),
                  )
    : java.io.Serializable