package com.example.atrapaformas

import java.text.SimpleDateFormat
import java.util.*

// Funciones de extensión o de nivel superior para simplificar fechas
object TimeUtils {
    private fun ahora() = Calendar.getInstance().time

    fun getFecha(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ahora())
    fun getHora(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ahora())
}