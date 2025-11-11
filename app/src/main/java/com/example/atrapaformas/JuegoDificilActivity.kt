package com.example.atrapaformas

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class JuegoDificilActivity : AppCompatActivity() {

    // --- 1. Variables del Juego ---
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

    // --- 2. Método Principal ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_facil)

        // Conectar vistas
        tvVidas = findViewById(R.id.tv_vidas)
        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)

        tvVidas.text = "VIDAS: $vidas"
        tvPuntos.text = "PUNTOS: $puntos"

        record = 112 // Ejemplo de récord

        iniciarJuego()
    }

    // --- 3. Game Loop ---
    private fun iniciarJuego() {
        establecerNuevaFormaObjetivo()

        val gameLoop = object : Runnable {
            override fun run() {
                if (!isJuegoActivo) return
                crearObjetoQueCae()
                gameHandler.postDelayed(this, 1500)
            }
        }
        gameHandler.post(gameLoop)
    }

    private fun establecerNuevaFormaObjetivo() {
        val nuevaFormaId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        ivTargetShape.setImageResource(nuevaFormaId)
        currentTargetDrawableId = nuevaFormaId
    }

    // --- 4. Crear objeto que cae ---
    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)
        val imagenParaCaerId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId

        val tamanoEnPx = (100 * resources.displayMetrics.density).toInt()
        objeto.layoutParams = ConstraintLayout.LayoutParams(tamanoEnPx, tamanoEnPx)

        cieloContainer.post {
            // --- CORRECCIÓN PRINCIPAL ---
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

    // --- 5. Animación de caída ---
    private fun animarCaida(objeto: ImageView) {
        val alturaSuelo = cieloContainer.height.toFloat()
        val animator = ObjectAnimator.ofFloat(objeto, "translationY", 0f, alturaSuelo)
        animator.duration = 2500

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
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

    // --- 6. Lógica de puntos y vidas ---
    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        tvPuntos.text = "PUNTOS: $puntos"

        if (puntos > record) {
            record = puntos
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
