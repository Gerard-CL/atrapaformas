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
import androidx.appcompat.app.AlertDialog // Importante para el cuadro de diálogo
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Random

class JuegoFacilActivity : AppCompatActivity() {

    // ===============================
    // 1. VARIABLES DEL JUEGO
    // ===============================
    private var vidas = 3
    private var puntos = 0
    private var record = 0
    private var isJuegoActivo = true

    // --- NUEVO: Variable para saber si estamos en pausa
    private var isPausado = false

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout

    // --- NUEVO: Referencia al botón de pausa
    private lateinit var btnPause: View

    private var currentTargetDrawableId: Int = 0
    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()

    // --- NUEVO: Lista para controlar las animaciones activas (congelarlas al pausar)
    private val animadoresActivos = mutableListOf<ObjectAnimator>()

    // --- NUEVO: Declaramos el Runnable aquí para poder pararlo y reiniciarlo
    private lateinit var gameLoop: Runnable

    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    private var mediaPlayer: MediaPlayer? = null

    // ===============================
    // 2. CICLO DE VIDA - onCreate
    // ===============================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_facil)

        // Música
        mediaPlayer = MediaPlayer.create(this, R.raw.fondomusica1)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        val nombreRecibido = intent.getStringExtra("NOMBRE_JUGADOR")
        val textViewNombre = findViewById<TextView>(R.id.tv_usuario)
        textViewNombre.text = nombreRecibido ?: "Jugador"

        // Ajuste de window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cielo_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()

        // --- NUEVO: Configurar el botón de Pause ---
        btnPause = findViewById(R.id.button_pause)
        btnPause.setOnClickListener {
            mostrarDialogoPausa()
        }

        record = 112

        // Definimos cómo funciona el bucle del juego
        definirGameLoop()

        // Esperar a que el layout esté listo antes de iniciar
        cieloContainer.post {
            iniciarJuego()
        }
    }

    // --- NUEVO: Lógica del botón de Pausa ---
    private fun mostrarDialogoPausa() {
        if (!isJuegoActivo) return

        pausarLogicaJuego()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pausa")
        builder.setMessage("¿Qué quieres hacer?")
        builder.setCancelable(false)

        builder.setPositiveButton("Reanudar") { dialog, _ ->
            reanudarLogicaJuego()
            dialog.dismiss()
        }

        builder.setNegativeButton("Salir") { dialog, _ ->
            finish() // Cierra la actividad
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun pausarLogicaJuego() {
        isPausado = true
        // 1. Pausar música
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        // 2. Detener generación de figuras
        gameHandler.removeCallbacks(gameLoop)
        gameHandler.removeCallbacksAndMessages(null)

        // 3. Pausar figuras que están cayendo
        for (anim in animadoresActivos) {
            if (anim.isRunning) {
                anim.pause()
            }
        }
    }

    private fun reanudarLogicaJuego() {
        isPausado = false
        // 1. Reanudar música
        mediaPlayer?.start()

        // 2. Reanudar caída de figuras
        for (anim in animadoresActivos) {
            if (anim.isPaused) {
                anim.resume()
            }
        }
        // 3. Reactivar generación de figuras
        gameHandler.post(gameLoop)
    }

    override fun onPause() {
        super.onPause()
        // Si el usuario sale de la app, pausamos internamente
        if (isJuegoActivo && !isPausado) {
            pausarLogicaJuego()
        }
    }

    override fun onResume() {
        super.onResume()
        // Si vuelve y estaba pausado por el sistema, mostramos el menú
        if (isJuegoActivo && isPausado) {
            mostrarDialogoPausa()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        animadoresActivos.clear()
    }

    // ===============================
    // 3. INICIALIZACIÓN DE VISTAS
    // ===============================
    private fun inicializarVistas() {
        tvVidas = findViewById(R.id.tv_vidas)
        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)

        tvVidas.text = "VIDAS: $vidas"
        tvPuntos.text = "PUNTOS: $puntos"
    }

    // ===============================
    // 5. INICIAR EL JUEGO
    // ===============================

    // --- NUEVO: Definimos el Runnable fuera para poder controlarlo
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

                val proximoDelay = (3000).toLong()
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

    // ===============================
    // 6. CREAR Y ANIMAR OBJETOS
    // ===============================
    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)
        val imagenId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        objeto.setImageResource(imagenId)
        objeto.tag = imagenId

        val tamanoEnPx = (150 * resources.displayMetrics.density).toInt()
        objeto.layoutParams = ConstraintLayout.LayoutParams(tamanoEnPx, tamanoEnPx)

        cieloContainer.post {
            // Verificación de seguridad
            if (cieloContainer.width <= 0) return@post

            val maxWidth = (cieloContainer.width - tamanoEnPx).coerceAtLeast(1)
            val startX = random.nextInt(maxWidth)
            objeto.x = startX.toFloat()
            objeto.y = 0f

            objeto.setOnClickListener { view ->
                // --- NUEVO: No permitir clic si está pausado
                if (!isJuegoActivo || isPausado) return@setOnClickListener

                cieloContainer.removeView(view)
                // IMPORTANTE: Si hacemos clic, debemos cancelar la animación asociada para que no cuente fallo
                // (Implementación simplificada confiando en que onAnimationEnd maneja la vista removida)

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
        animator.duration = 6000

        // --- NUEVO: Añadimos a la lista para control
        animadoresActivos.add(animator)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // --- NUEVO: Quitamos de la lista al terminar
                animadoresActivos.remove(animation)

                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto)
                    if (objeto.tag as Int == currentTargetDrawableId) {
                        // Solo restamos vida si el juego sigue activo
                        if(isJuegoActivo) restarVida()
                    }
                }
            }
        })

        animator.start()
    }

    // ===============================
    // 7. PUNTUACIÓN Y VIDAS
    // ===============================
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

    // ===============================
    // 8. FIN DEL JUEGO
    // ===============================
    private fun terminarJuego() {
        isJuegoActivo = false

        // Limpiamos todo
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
                                                    }, 0)
    }
}