package com.example.atrapaformas

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Partida(
    val id: String = "",
    val jugador: Jugador,
    val fechaPartida: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val estado: String = "en_curso" // "en_curso", "completada", "abandonada"
                  )

class GestorPartidas(private val directorio: String = "partidas") {

    init {
        // Crear directorio si no existe
        val dir = File(directorio)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    fun crearPartida(jugador: Jugador): String {
        val id = "partida_${System.currentTimeMillis()}"
        val ahora = Calendar.getInstance().time
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val partida = Partida(
            id = id,
            jugador = jugador,
            fechaPartida = formatoFecha.format(ahora),
            horaInicio = formatoHora.format(ahora)
                             )

        guardarPartida(partida)
        return id
    }

    fun guardarPartida(partida: Partida) {
        try {
            val archivo = File("${directorio}/${partida.id}.json")
            val jsonString = Json.encodeToString(partida)
            archivo.writeText(jsonString)
            println("✅ Partida guardada: ${partida.id}.json")
        } catch (e: Exception) {
            println("❌ Error al guardar partida: ${e.message}")
        }
    }

    fun finalizarPartida(idPartida: String, puntuacion: Int, tiempoJuego: Int) {
        try {
            val archivo = File("${directorio}/${idPartida}.json")
            if (archivo.exists()) {
                val jsonString = archivo.readText()
                val partida = Json.decodeFromString<Partida>(jsonString)

                // Actualizar datos usando copy() para crear nueva instancia
                val jugadorActualizado = partida.jugador.copy(
                    puntuacion = puntuacion,
                    tiempoJuego = tiempoJuego
                                                             )

                val partidaActualizada = partida.copy(
                    jugador = jugadorActualizado,
                    estado = "completada",
                    horaFin = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Calendar.getInstance().time)
                                                     )

                // Guardar la partida actualizada
                guardarPartida(partidaActualizada)
                println("✅ Partida finalizada: $idPartida")
            }
        } catch (e: Exception) {
            println("❌ Error al finalizar partida: ${e.message}")
        }
    }

    fun obtenerTodasLasPartidas(): List<Partida> {
        val partidas = mutableListOf<Partida>()
        try {
            val directorio = File(directorio)
            if (directorio.exists()) {
                directorio.listFiles()?.forEach { archivo ->
                    if (archivo.name.endsWith(".json")) {
                        val jsonString = archivo.readText()
                        val partida = Json.decodeFromString<Partida>(jsonString)
                        partidas.add(partida)
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error al cargar partidas: ${e.message}")
        }
        return partidas
    }
}