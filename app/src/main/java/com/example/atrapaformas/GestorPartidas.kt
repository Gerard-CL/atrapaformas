package com.example.atrapaformas

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class GestorPartidas(private val directorio: String = "partidas") {

    // El bloque init se puede simplificar con mkdirs() que es seguro llamar aunque exista
    init { File(directorio).mkdirs() }

    fun crearPartida(jugador: Jugador): Partida {
        val id = "partida_${System.currentTimeMillis()}"
        val partida = Partida(id = id, jugador = jugador)
       // guardarPartida(partida)



        return partida
    }

    fun guardarPartida(partida: Partida) {
        try {
            File("$directorio/${partida.id}.json").writeText(Json.encodeToString(partida))
            println("✅ Partida guardada: ${partida.id}")
        } catch (e: Exception) {
            println("❌ Error al guardar: ${e.message}")
        }
    }

    fun finalizarPartida(partida: Partida, puntuacion: Int, tiempoJuego: Int) {
        val partidaid = partida.id
        val archivo = File("$directorio/$partidaid.json")

        if (archivo.exists()){
            return
        }

        try {
            val horaActual = TimeUtils.getHora()

            partida.jugador.puntuacion = puntuacion
            partida.jugador.tiempoJuego = tiempoJuego

            partida.estado = "completada"
            partida.horaFin = horaActual
            partida.fecha = TimeUtils.getFecha()



            guardarPartida(partida)
            println("✅ Partida finalizada correctamente.")

        } catch (e: Exception) {
            println("❌ Error al finalizar: ${e.message}")
        }
    }
}