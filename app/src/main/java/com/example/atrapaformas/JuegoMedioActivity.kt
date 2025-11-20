package com.example.atrapaformas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class JuegoMedioActivity : AppCompatActivity() {

    // --- Variables del Juego ---
    private var vidas = 3
    private var puntos = 0
    private var record = 0
    private var isJuegoActivo = true
    private var isPausado = false // --- NUEVO: Controla si está en pausa

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout

    // --- NUEVO: Referencia al botón de pausa (asegúrate de tenerlo en el XML)
    private lateinit var btnPause: View

    private var currentTargetDrawableId: Int = 0

    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()

    private var mediaPlayer: MediaPlayer? = null

    // --- NUEVO: Lista para guardar las animaciones activas y poder pausarlas ---
    private val animadoresActivos = mutableListOf<ObjectAnimator>()

    // --- NUEVO: Sacamos el Runnable a una variable de clase para poder reutilizarlo
    private lateinit var gameLoop: Runnable

    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_facil)

        mediaPlayer = MediaPlayer.create(this, R.raw.fondomusica1)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        val nombreRecibido = intent.getStringExtra("NOMBRE_JUGADOR")
        val textViewNombre = findViewById<TextView>(R.id.tv_usuario)
        textViewNombre.text = nombreRecibido ?: "Jugador"

        tvVidas = findViewById(R.id.tv_vidas)
        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)

        // --- NUEVO: Inicializar botón de pausa ---
        // Asegúrate de que en tu XML tengas un botón/imagen con id: button_pause
        btnPause = findViewById(R.id.button_pause)
        btnPause.setOnClickListener {
            mostrarDialogoPausa()
        }

        tvVidas.text = "VIDAS: $vidas"
        tvPuntos.text = "PUNTOS: $puntos"
        record = 112

        // Definimos el bucle del juego aquí para poder iniciarlo y pararlo
        definirGameLoop()

        iniciarJuego()
    }

    // --- NUEVO: Lógica del botón de Pausa ---
    private fun mostrarDialogoPausa() {
        if (!isJuegoActivo) return // Si ya perdiste, no permite pausar

        pausarLogicaJuego()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pausa")
        builder.setMessage("¿Qué quieres hacer?")
        builder.setCancelable(false) // Evita que se cierre tocando fuera

        builder.setPositiveButton("Reanudar") { dialog, _ ->
            reanudarLogicaJuego()
            dialog.dismiss()
        }

        builder.setNegativeButton("Salir") { dialog, _ ->
            // Cierra la actividad y vuelve atrás (o al menú)
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun pausarLogicaJuego() {
        isPausado = true
        // 1. Parar la música
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        // 2. Detener la generación de nuevos objetos
        gameHandler.removeCallbacks(gameLoop)
        gameHandler.removeCallbacksAndMessages(null) // Limpia todo

        // 3. Pausar las animaciones de caída actuales
        for (anim in animadoresActivos) {
            if (anim.isRunning) { // Solo en API 19+, si usas una muy vieja usa if(anim.isStarted)
                anim.pause()
            }
        }
    }

    private fun reanudarLogicaJuego() {
        isPausado = false
        // 1. Reanudar música
        mediaPlayer?.start()

        // 2. Reanudar animaciones
        for (anim in animadoresActivos) {
            if (anim.isPaused) {
                anim.resume()
            }
        }

        // 3. Volver a arrancar el bucle de generación
        gameHandler.post(gameLoop)
    }


    override fun onPause() {
        super.onPause()
        // Si el usuario sale de la app, pausamos automáticamente
        if (isJuegoActivo && !isPausado) {
            pausarLogicaJuego()
            // Nota: Aquí no mostramos el diálogo, porque el usuario ya salió de la app.
            // Podrías mostrar el diálogo en onResume si quieres forzar la pausa al volver.
        }
    }

    override fun onResume() {
        super.onResume()
        // Si volvemos y estaba pausado por el sistema (no por el botón), reanudamos o mostramos diálogo
        // Para simplificar, si minimizó la app, al volver mostramos el diálogo de pausa
        if (isJuegoActivo && isPausado) {
            mostrarDialogoPausa()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // --- Lógica del Juego ---

    private fun definirGameLoop() {
        gameLoop = object : Runnable {
            override fun run() {
                if (!isJuegoActivo || isPausado) return

                val numeroDeObjetos = random.nextInt(2) + 1

                for (i in 1..numeroDeObjetos) {
                    val retrasoSpawn = (i - 1) * 200L
                    gameHandler.postDelayed({
                                                if (isJuegoActivo && !isPausado) {
                                                    crearObjetoQueCae()
                                                }
                                            }, retrasoSpawn)
                }

                val proximoDelay = (2500).toLong()
                gameHandler.postDelayed(this, proximoDelay)
            }
        }
    }

    private fun iniciarJuego() {
        establecerNuevaFormaObjetivo()
        gameHandler.post(gameLoop)
    }

    private fun establecerNuevaFormaObjetivo() {
        val nuevaFormaId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        ivTargetShape.setImageResource(nuevaFormaId)
        currentTargetDrawableId = nuevaFormaId
    }

    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)
        val imagenParaCaerId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId

        val tamanoEnPx = (150 * resources.displayMetrics.density).toInt()
        objeto.layoutParams = ConstraintLayout.LayoutParams(tamanoEnPx, tamanoEnPx)

        cieloContainer.post {
            // Verificación extra por seguridad
            if (cieloContainer.width <= 0) return@post

            val maxWidth = (cieloContainer.width - tamanoEnPx).coerceAtLeast(1)
            val startX = random.nextInt(maxWidth)
            objeto.x = startX.toFloat()
            objeto.y = 0f

            objeto.setOnClickListener { view ->
                if (!isJuegoActivo || isPausado) return@setOnClickListener // Bloqueo click en pausa

                cieloContainer.removeView(view)
                // Al hacer click y eliminarlo, cancelamos su animación para que no reste vida al final
                // Esto requiere buscar la animación asociada, pero para simplificar,
                // la lógica actual de onAnimationEnd maneja si la vista tiene padre.

                if (view.tag as Int == currentTargetDrawableId) {
                    sumarPuntos(10)
                } else {
                    restarVida()
                }
            }

            cieloContainer.addView(objeto)
            animarCaida(objeto)
        }
    }

    private fun animarCaida(objeto: ImageView) {
        val alturaSuelo = cieloContainer.height.toFloat()
        val animator = ObjectAnimator.ofFloat(objeto, "translationY", 0f, alturaSuelo)
        animator.duration = 4000

        // --- NUEVO: Agregar a la lista de activos ---
        animadoresActivos.add(animator)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // --- NUEVO: Remover de la lista de activos ---
                animadoresActivos.remove(animation)

                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto)
                    if (objeto.tag as Int == currentTargetDrawableId) {
                        // Solo resta vida si el juego sigue activo y no se ha clicado
                        if(isJuegoActivo) restarVida()
                    }
                }
            }
        })

        animator.start()
    }

    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        tvPuntos.text = "PUNTOS: $puntos"
        if (puntos > record) record = puntos
    }

    private fun restarVida() {
        vidas--
        tvVidas.text = "VIDAS: $vidas"

        if (vidas <= 0) {
            tvVidas.text = "¡FIN!"
            terminarJuego()
        }
    }

    private fun terminarJuego() {
        isJuegoActivo = false
        // Cancelamos todo
        gameHandler.removeCallbacksAndMessages(null)
        for(anim in animadoresActivos) {
            anim.cancel()
        }
        animadoresActivos.clear()

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos)
                                                        intent.putExtra("RECORD_ACTUAL", record)
                                                        startActivity(intent)
                                                        finish()
                                                    }, 500) // Pequeño delay para que se vea el fin
    }
}