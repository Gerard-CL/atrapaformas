package com.example.atrapaformas

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class DatosJugadores(
    val fechaActualizacion: String = "",
    val totalJugadores: Int = 0,
    val jugadores: List<Jugador> = emptyList()
                         )

class GestorJugadores(private val archivoJson: String = "jugadores.json") {
    private val jugadores = mutableListOf<Jugador>()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        try {
            val archivo = File(archivoJson)
            if (archivo.exists()) {
                val jsonString = archivo.readText()
                val datos = Json.decodeFromString<DatosJugadores>(jsonString)
                jugadores.clear()
                jugadores.addAll(datos.jugadores)
                println("✅ Datos cargados: ${jugadores.size} jugadores")
            }
        } catch (e: Exception) {
            println("❌ Error al cargar datos: ${e.message}")
        }
    }

    fun agregarJugador(jugador: Jugador) {
        jugadores.add(jugador)
        guardarDatos()
    }

    fun crearYAgregarJugador(nombre: String, edad: Int, dificultad: String? = null) {
        val jugador = Jugador.crearJugador(nombre, edad, dificultad)
        agregarJugador(jugador)
    }

    fun guardarDatos() {
        try {
            val ahora = Calendar.getInstance().time
            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val datos = DatosJugadores(
                fechaActualizacion = formato.format(ahora),
                totalJugadores = jugadores.size,
                jugadores = jugadores.toList()
                                      )

            val jsonString = Json.encodeToString(datos)
            File(archivoJson).writeText(jsonString)

            println("✅ JSON guardado en: $archivoJson")
            println("📊 Total jugadores: ${jugadores.size}")
        } catch (e: Exception) {
            println("❌ Error al guardar JSON: ${e.message}")
        }
    }

    fun obtenerJugadores(): List<Jugador> = jugadores.toList()

    fun obtenerJugadoresPorDificultad(dificultad: String): List<Jugador> {
        return jugadores.filter { it.dificultad.equals(dificultad, ignoreCase = true) }
    }
}