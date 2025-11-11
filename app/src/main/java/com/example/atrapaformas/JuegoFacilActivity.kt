package com.example.atrapaformas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Random

class JuegoFacilActivity : AppCompatActivity() {

    // ===========================================
    // 1. VARIABLES DEL JUEGO
    // ===========================================
    private var vidas = 3
    private var puntos = 0
    private var record = 0
    private var isJuegoActivo = true

    // Views
    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout

    // Forma objetivo actual
    private var currentTargetDrawableId: Int = 0

    // Handlers y utilidades
    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()

    // Overlay de instrucciones
    private var instructionsOverlay: View? = null

    // Lista de imágenes del juego
    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    // ===========================================
    // 2. CICLO DE VIDA - onCreate
    // ===========================================
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_facil)

        // Configurar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        inicializarVistas()

        // Cargar récord (TODO: implementar SharedPreferences)
        record = 112

        // Mostrar overlay de instrucciones
        showInstructionsOverlay()

        // Iniciar el juego
        iniciarJuego()
    }

    // ===========================================
    // 3. INICIALIZACIÓN
    // ===========================================
    private fun inicializarVistas() {
        tvVidas = findViewById(R.id.tv_vidas)
        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)

        // Inicializar textos
        tvVidas.text = "VIDAS: $vidas"
        tvPuntos.text = "PUNTOS: $puntos"
    }

    // ===========================================
    // 4. OVERLAY DE INSTRUCCIONES
    // ===========================================
    private fun showInstructionsOverlay() {
        val rootView = findViewById<View>(android.R.id.content)

        val overlay = LayoutInflater.from(this)
            .inflate(R.layout.instructions_overlay, rootView as? android.view.ViewGroup, false)

        overlay.setOnClickListener {
            hideInstructionsOverlay()
        }

        (rootView as? android.view.ViewGroup)?.addView(overlay)
        instructionsOverlay = overlay
    }

    private fun hideInstructionsOverlay() {
        instructionsOverlay?.let { overlay ->
            val rootView = findViewById<android.view.ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
            instructionsOverlay = null
        }
    }

    // ===========================================
    // 5. LÓGICA DEL JUEGO
    // ===========================================
    private fun iniciarJuego() {
        // Establecer forma objetivo inicial
        establecerNuevaFormaObjetivo()

        // Game loop: crear objetos que caen
        val gameLoop = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun run() {
                if (!isJuegoActivo) return
                crearObjetoQueCae()
                gameHandler.postDelayed(this, 1500) // Cada 1.5 segundos
            }
        }
        gameHandler.post(gameLoop)
    }

    private fun establecerNuevaFormaObjetivo() {
        val nuevaFormaId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        ivTargetShape.setImageResource(nuevaFormaId)
        currentTargetDrawableId = nuevaFormaId
    }

    // ===========================================
    // 6. CREACIÓN Y ANIMACIÓN DE OBJETOS
    // ===========================================
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)

        val imagenParaCaerId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId

        val tamanoEnPx = (150 * resources.displayMetrics.density).toInt()
        val params = ConstraintLayout.LayoutParams(tamanoEnPx, tamanoEnPx)
        objeto.layoutParams = params

        cieloContainer.post {
            // --- INICIO DE LA SOLUCIÓN ---

            // 1. Calcula el rango máximo de forma segura
            val maxWidth = cieloContainer.width - tamanoEnPx

            // 2. Comprueba si el rango es válido
            // Si el contenedor es demasiado pequeño o aún no se ha medido (width=0),
            // simplemente no añadas este objeto y espera al siguiente ciclo.
            if (maxWidth <= 0) {
                return@post // Salir del bloque 'post' de forma segura
            }

            // 3. Ahora SÍ es seguro generar el número aleatorio
            val startX = random.nextInt(0, maxWidth)

            // --- FIN DE LA SOLUCIÓN ---

            objeto.x = startX.toFloat()
            objeto.y = 0f

            objeto.setOnClickListener { view ->
                // ... (tu código de click listener)
            }

            cieloContainer.addView(objeto)
            animarCaida(objeto)
        }
    }

    private fun animarCaida(objeto: ImageView) {
        val alturaSuelo = cieloContainer.height.toFloat()
        val animator = ObjectAnimator.ofFloat(objeto, "translationY", 0f, alturaSuelo)
        animator.duration = 6000

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Si el objeto aún está en pantalla (no fue atrapado)
                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto)

                    // Si era el objetivo y llegó al suelo, restar vida
                    if (objeto.tag as Int == currentTargetDrawableId) {
                        restarVida()
                    }
                    // Si era otro objeto, no pasa nada
                }
            }
        })

        animator.start()
    }

    // ===========================================
    // 7. PUNTUACIÓN Y VIDAS
    // ===========================================
    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        tvPuntos.text = "PUNTOS: $puntos"

        // Actualizar récord si se supera
        if (puntos > record) {
            record = puntos
            // TODO: Guardar en SharedPreferences
        }
    }

    private fun restarVida() {
        vidas--
        tvVidas.text = "VIDAS: $vidas"

        if (vidas <= 0) {
            tvVidas.text = "¡FIN!"
            terminarJuego()
        }
    }

    // ===========================================
    // 8. FIN DEL JUEGO
    // ===========================================
    private fun terminarJuego() {
        isJuegoActivo = false
        gameHandler.removeCallbacksAndMessages(null)

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos)
                                                        intent.putExtra("RECORD_ACTUAL", record)
                                                        startActivity(intent)
                                                        finish()
                                                    }, 2000)
    }
}