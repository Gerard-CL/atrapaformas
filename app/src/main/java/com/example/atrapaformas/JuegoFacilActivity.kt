package com.example.atrapaformas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout

    private var currentTargetDrawableId: Int = 0
    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()


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

        // Ajuste de window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cielo_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        inicializarVistas()

        // Cargar récord (placeholder)
        record = 112


        // Esperar a que el layout esté listo antes de iniciar el juego
        cieloContainer.post {
            iniciarJuego()
        }
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
    private fun iniciarJuego() {
        establecerNuevaFormaObjetivo()

        val gameLoop = object : Runnable {
            override fun run() {
                if (!isJuegoActivo) return
                crearObjetoQueCae()
                gameHandler.postDelayed(this, 1500) // Cada 1.5s
            }
        }
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
            val maxWidth = (cieloContainer.width - tamanoEnPx).coerceAtLeast(1)
            val startX = random.nextInt(maxWidth)
            objeto.x = startX.toFloat()
            objeto.y = 0f

            objeto.setOnClickListener { view ->
                if (!isJuegoActivo) return@setOnClickListener
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
        animator.duration = 6000

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto)
                    if (objeto.tag as Int == currentTargetDrawableId) {
                        restarVida()
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
