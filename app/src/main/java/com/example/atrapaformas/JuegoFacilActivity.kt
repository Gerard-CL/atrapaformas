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

    private var nombreJugador: String? = null



    private lateinit var hearts: List<ImageView>
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
    private lateinit var idPartida: String
    private lateinit var Partida: Partida
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
        Partida = intent.getSerializableExtra("ID_PARTIDA") as Partida
        idPartida = Partida.id

        nombreJugador = intent.getStringExtra("NOMBRE_JUGADOR")
        tiempoInicioPartida = System.currentTimeMillis()

        // 2. Música
        mediaPlayer = MediaPlayer.create(this, R.raw.fondomusica1)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // 3. UI y Datos recibidos
        val nombreRecibido = intent.getStringExtra("NOMBRE_JUGADOR")
        val textViewNombre = findViewById<TextView>(R.id.tv_usuario)
        textViewNombre.text = nombreRecibido ?: "Jugador"

        hearts = listOf(
            findViewById(R.id.heart1),
            findViewById(R.id.heart2),
            findViewById(R.id.heart3)
                       )

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
        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)
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

        // --- INICIO DE LA MODIFICACIÓN ---

        // 1. Definimos el porcentaje de probabilidad (Ej: 60%)
        // Puedes subir este número si quieres que salga aún más veces.
        val probabilidadDeTarget = 40

        // 2. Tiramos un "dado" de 0 a 99
        val dado = random.nextInt(100)

        val imagenParaCaerId = if (dado < probabilidadDeTarget) {
            // CASO A: Forzamos que salga la forma correcta
            currentTargetDrawableId
        } else {
            // CASO B: Sale una forma totalmente aleatoria (ruido)
            imagenesJuego[random.nextInt(imagenesJuego.size)]
        }

        // --- FIN DE LA MODIFICACIÓN ---

        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId

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


        val duracionCaida: Long = when (puntos) {
            in 0..50 -> 6000L
            in 51..100 -> 5500L
            in 101..150 -> 400L
            else -> 4000L
        }

        animator.duration = duracionCaida

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

        // Si la vida actual está dentro del rango (ej: baja de 3 a 2, ocultamos el índice 2)
        if (vidas >= 0 && vidas < hearts.size) {
            // Hacemos el corazón invisible
            hearts[vidas].visibility = View.INVISIBLE

            // Opcional: Si prefieres que cambie a corazón vacío en vez de desaparecer, usa:
            // hearts[vidas].setImageResource(R.drawable.heart_empty)
        }

        if (vidas <= 0) {
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
        gestorPartidas.finalizarPartida(Partida, puntos, tiempoJugadoSegundos)

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos) // Clave unificada
                                                        intent.putExtra("ID_PARTIDA", idPartida)
                                                        intent.putExtra("DIFICULTAD", "Fácil")
                                                        intent.putExtra("NOMBRE_JUGADOR", nombreJugador)
                                                        startActivity(intent)
                                                        finish()
                                                    }, 0)
    }
}