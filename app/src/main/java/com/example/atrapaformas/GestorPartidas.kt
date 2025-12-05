package com.example.atrapaformas

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class Partida(
    val id: String,
    val jugador: Jugador,
    val estado: String = "en_curso", // "en_curso", "completada", "abandonada"
    val fecha: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val horaFin: String = "" // Añadido para guardar cuándo acabó la partida globalmente
                  )

class GestorPartidas(private val directorio: String = "partidas") {

    // El bloque init se puede simplificar con mkdirs() que es seguro llamar aunque exista
    init { File(directorio).mkdirs() }

    fun crearPartida(jugador: Jugador): String {
        val id = "partida_${System.currentTimeMillis()}"
        val partida = Partida(id = id, jugador = jugador)
        guardarPartida(partida)
        return id
    }

    fun guardarPartida(partida: Partida) {
        try {
            File("$directorio/${partida.id}.json").writeText(Json.encodeToString(partida))
            println("✅ Partida guardada: ${partida.id}")
        } catch (e: Exception) {
            println("❌ Error al guardar: ${e.message}")
        }
    }

    fun finalizarPartida(idPartida: String, puntuacion: Int, tiempoJuego: Int) {
        val archivo = File("$directorio/$idPartida.json")
        if (!archivo.exists()) return

        try {
            // 1. Leemos la partida original
            val partida = Json.decodeFromString<Partida>(archivo.readText())
            val horaActual = TimeUtils.getHora()

            // 2. Actualizamos jugador y partida usando copy (más limpio e inmutable)
            val jugadorFinal = partida.jugador.copy(
                puntuacion = puntuacion,
                tiempoJuego = tiempoJuego,
                )

            val partidaFinal = partida.copy(
                jugador = jugadorFinal,
                estado = "completada",
                horaFin = horaActual
                )

            // 3. Guardamos
            guardarPartida(partidaFinal)
            println("✅ Partida finalizada correctamente.")

        } catch (e: Exception) {
            println("❌ Error al finalizar: ${e.message}")
        }
    }
}