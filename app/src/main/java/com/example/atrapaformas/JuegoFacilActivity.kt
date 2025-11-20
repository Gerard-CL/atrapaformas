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
    private var isPausado = false

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout
    private lateinit var btnPause: View

    private var currentTargetDrawableId: Int = 0
    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()

    // Lista para controlar las animaciones activas
    private val animadoresActivos = mutableListOf<ObjectAnimator>()
    private lateinit var gameLoop: Runnable

    private var mediaPlayer: MediaPlayer? = null

    // --- VARIABLES DE GESTIÓN DE PARTIDA (Fusionadas) ---
    private lateinit var gestorPartidas: GestorPartidas
    private var idPartida: String = ""
    private var tiempoInicioPartida: Long = 0

    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    // ===============================
    // 2. CICLO DE VIDA - onCreate
    // ===============================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_facil)

        // 1. Inicializar Gestor
        gestorPartidas = GestorPartidas("${filesDir.path}/partidas")
        idPartida = intent.getStringExtra("ID_PARTIDA") ?: ""
        tiempoInicioPartida = System.currentTimeMillis()

        // 2. Música
        mediaPlayer = MediaPlayer.create(this, R.raw.fondomusica1)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // 3. UI y Datos recibidos
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

        // 4. Configurar botón de Pause
        btnPause = findViewById(R.id.button_pause)
        btnPause.setOnClickListener {
            mostrarDialogoPausa()
        }

        record = 112 // Aquí podrías cargar el récord real si lo tuvieras guardado

        definirGameLoop()

        // Esperar a que el layout esté listo antes de iniciar
        cieloContainer.post {
            iniciarJuego()
        }
    }

    // --- Lógica del botón de Pausa ---
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
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun pausarLogicaJuego() {
        isPausado = true
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        gameHandler.removeCallbacks(gameLoop)
        gameHandler.removeCallbacksAndMessages(null)

        for (anim in animadoresActivos) {
            if (anim.isRunning) {
                anim.pause()
            }
        }
    }

    private fun reanudarLogicaJuego() {
        isPausado = false
        mediaPlayer?.start()

        for (anim in animadoresActivos) {
            if (anim.isPaused) {
                anim.resume()
            }
        }
        gameHandler.post(gameLoop)
    }

    override fun onPause() {
        super.onPause()
        if (isJuegoActivo && !isPausado) {
            pausarLogicaJuego()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isJuegoActivo && isPausado) {
            mostrarDialogoPausa()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        gameHandler.removeCallbacksAndMessages(null)
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

                val proximoDelay = (3000).toLong() // 3 segundos en fácil
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
            if (cieloContainer.width <= 0) return@post

            val maxWidth = (cieloContainer.width - tamanoEnPx).coerceAtLeast(1)
            val startX = random.nextInt(maxWidth)
            objeto.x = startX.toFloat()
            objeto.y = 0f

            objeto.setOnClickListener { view ->
                if (!isJuegoActivo || isPausado) return@setOnClickListener

                cieloContainer.removeView(view)

                // Nota: Al remover la vista, la animación eventualmente termina o se cancela.
                // Dependemos de la lógica de puntos aquí y de onAnimationEnd para limpieza.

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
        animator.duration = 6000 // Lento para fácil

        animadoresActivos.add(animator)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // IMPORTANTE: Remover de la lista
                animadoresActivos.remove(animation)

                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto)
                    if (objeto.tag as Int == currentTargetDrawableId) {
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
    // 8. FIN DEL JUEGO (CORREGIDO)
    // ===============================
    private fun terminarJuego() {
        isJuegoActivo = false

        // Cancelar handlers
        gameHandler.removeCallbacksAndMessages(null)

        // --- CORRECCIÓN CRASH: Usar copia de la lista ---
        val copiaAnimadores = animadoresActivos.toList()
        for(anim in copiaAnimadores) {
            anim.cancel()
        }
        animadoresActivos.clear()

        // --- LÓGICA FUSIONADA DEL GESTOR DE PARTIDAS ---
        val tiempoJugadoSegundos = ((System.currentTimeMillis() - tiempoInicioPartida) / 1000).toInt()
        gestorPartidas.finalizarPartida(idPartida, puntos, tiempoJugadoSegundos)

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos) // Clave unificada
                                                        intent.putExtra("ID_PARTIDA", idPartida)
                                                        intent.putExtra("DIFICULTAD", "Fácil")
                                                        startActivity(intent)
                                                        finish()
                                                    }, 500)
    }
}