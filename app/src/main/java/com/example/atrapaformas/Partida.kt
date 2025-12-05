package com.example.atrapaformas

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class Partida(
    val id: String,
    val jugador: Jugador,
    var estado: String = "en_curso", // "en_curso", "completada", "abandonada"
    var fecha: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    var horaFin: String = "" // Añadido para guardar cuándo acabó la partida globalment
                  )
    : java.io.Serializable