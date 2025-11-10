package com.example.atrapaformas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class JuegoFacilActivity : AppCompatActivity() {

    // --- 1. Variables del Juego ---
    private var vidas = 3
    private var puntos = 0
    private var record = 0 // Necesitarás guardar y cargar el récord real
    private var isJuegoActivo = true

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView // Nuevo TextView para puntos
    private lateinit var ivTargetShape: ImageView // Nuevo ImageView para la forma objetivo
    private lateinit var cieloContainer: ConstraintLayout

    // La imagen objetivo actual que debe ser CAPTURADA
    private var currentTargetDrawableId: Int = 0

    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()

    // Lista de tus imágenes (asegúrate de que estas existan en res/drawable)
    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas, // Ejemplo de nombres de tus drawables
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    // --- 2. Método Principal (onCreate) ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_facil)

        // Conectamos las vistas del XML
        tvVidas = findViewById(R.id.tv_vidas)
        tvPuntos = findViewById(R.id.tv_puntos) // Conectar el TextView de puntos
        ivTargetShape = findViewById(R.id.iv_target_shape) // Conectar el ImageView del objetivo
        cieloContainer = findViewById(R.id.cielo_container)

        // Inicializar texto
        tvVidas.text = "VIDAS: $vidas"
        tvPuntos.text = "PUNTOS: $puntos"

        // Cargar el récord (aquí deberías cargar de SharedPreferences o similar)
        record = 112 // Ejemplo, reemplaza con tu lógica de carga
        // Actualizar el TextView del récord si lo tienes

        // ¡Empezar el juego!
        iniciarJuego()
    }

    // --- 3. El Game Loop ---
    private fun iniciarJuego() {
        // Establece una forma objetivo al inicio del juego
        establecerNuevaFormaObjetivo()

        val gameLoop = object : Runnable {
            override fun run() {
                if (!isJuegoActivo) return

                crearObjetoQueCae()
                gameHandler.postDelayed(this, 1500) // Crear una forma cada 1.5 segundos
            }
        }
        gameHandler.post(gameLoop)
    }

    /**
     * Elige una forma aleatoria de la lista y la muestra como objetivo.
     */
    private fun establecerNuevaFormaObjetivo() {
        val nuevaFormaId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        ivTargetShape.setImageResource(nuevaFormaId)
        currentTargetDrawableId = nuevaFormaId
    }

    // --- 4. Crear el Objeto ---
    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)

        // Selecciona una imagen aleatoria para el objeto que va a caer
        val imagenParaCaerId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId // Guardamos el ID de la imagen en el tag para identificarla

        val tamanoEnPx = (100 * resources.displayMetrics.density).toInt()
        val params = ConstraintLayout.LayoutParams(tamanoEnPx, tamanoEnPx)
        objeto.layoutParams = params

        cieloContainer.post {
            val startX = random.nextInt(0, cieloContainer.width - tamanoEnPx)
            objeto.x = startX.toFloat()
            objeto.y = 0f

            // Añadir el Click Listener al objeto
            objeto.setOnClickListener { view ->
                if (!isJuegoActivo) return@setOnClickListener

                cieloContainer.removeView(view) // Quitar la imagen al tocarla

                // Comprobar si es el objeto objetivo
                if (view.tag as Int == currentTargetDrawableId) {
                    sumarPuntos(10) // Sumar puntos por atrapar el objetivo correcto
                    // Opcional: cambiar el objetivo cada X puntos o cada X objetos capturados
                    // if (puntos % 50 == 0) establecerNuevaFormaObjetivo()
                } else {
                    restarVida() // Restar vida por tocar un objeto incorrecto
                }
            }

            cieloContainer.addView(objeto)
            animarCaida(objeto)
        }
    }

    // --- 5. Animar la Caída ---
    private fun animarCaida(objeto: ImageView) {
        val alturaSuelo = cieloContainer.height.toFloat()

        val animator = ObjectAnimator.ofFloat(objeto, "translationY", 0f, alturaSuelo)
        animator.duration = 6000

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Si la animación terminó y la imagen aún está en pantalla (no fue atrapada)
                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto) // Limpiar la imagen

                    // Lógica para cuando un objeto llega al suelo
                    // Si el objeto que llegó al suelo era el objetivo, RESTA VIDA
                    if (objeto.tag as Int == currentTargetDrawableId) {
                        restarVida() // El objetivo se escapó, ¡pierdes vida!
                    }
                    // Si era otro objeto, no pasa nada (no hay que tocarlo ni restas vida)
                }
            }
        })
        animator.start()
    }

    // --- 6. Lógica de Puntos, Vidas y Game Over ---
    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        tvPuntos.text = "PUNTOS: $puntos"
        // Opcional: Actualizar el récord si se supera
        if (puntos > record) {
            record = puntos
            // tvRecord.text = "RECORD: $record" // Si tienes un TextView para el récord
            // Guardar nuevo récord
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
                                                        // Aquí puedes pasar puntos y récord a la pantalla de fin
                                                        intent.putExtra("PUNTUACION_FINAL", puntos)
                                                        intent.putExtra("RECORD_ACTUAL", record)
                                                        startActivity(intent)
                                                        finish()
                                                    }, 2000)
    }
}
