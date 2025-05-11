package com.example.mas_montaa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RouteSelectionActivity : AppCompatActivity() {

    private var isRouteVisible = false

    private val coloresEspecie = mapOf(
        "🦌 Ciervo" to 0xFFD32F2F.toInt(),
        "🦊 Zorro" to 0xFFF57C00.toInt(),
        "🦅 Águila real" to 0xFFFBC02D.toInt(),
        "🐍 Víbora hocicuda" to 0xFF388E3C.toInt(),
        "🌲 Pino silvestre" to 0xFF1976D2.toInt(),
        "🌿 Helecho común" to 0xFF7B1FA2.toInt(),
        "🌸 Jara pringosa" to 0xFF6D4C41.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_selection)

        val btnRuta1 = findViewById<Button>(R.id.btnRuta1)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val scrollView = findViewById<ScrollView>(R.id.scrollRuta)

        btnRuta1.setBackgroundResource(R.drawable.button_blue) // Azul

        btnRuta1.setOnClickListener {
            if (!isRouteVisible) {
                scrollView.visibility = ScrollView.VISIBLE
                linearLayout.removeAllViews()

                val contenedorHorizontal = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2f
                }

                val columnaRuta = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val columnaFauna = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val datosRuta = listOf(
                    "Distancia" to "7.2 km",
                    "Dificultad" to "Media",
                    "Inclinación" to "Moderada"
                )

                for ((titulo, valor) in datosRuta) {
                    val textView = TextView(this).apply {
                        text = "$titulo: $valor"
                        textSize = 18f
                        setPadding(0, 20, 0, 0)
                    }
                    columnaRuta.addView(textView)
                }

                for ((nombre, color) in coloresEspecie) {
                    val especie = TextView(this).apply {
                        text = nombre
                        textSize = 16f
                        setBackgroundColor(color)
                        setTextColor(Color.WHITE)
                        setPadding(16, 16, 16, 16)
                    }

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 10, 0, 10)
                    }

                    especie.layoutParams = params
                    columnaFauna.addView(especie)
                }

                contenedorHorizontal.addView(columnaRuta)
                contenedorHorizontal.addView(columnaFauna)
                linearLayout.addView(contenedorHorizontal)

                val button = Button(this).apply {
                    text = "VER EN EL MAPA"
                    setBackgroundResource(R.drawable.button_dark)
                    setTextColor(Color.WHITE)
                    setPadding(20, 16, 20, 16)
                    setOnClickListener {
                        val intent = Intent(this@RouteSelectionActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }

                linearLayout.addView(button)
            } else {
                scrollView.visibility = ScrollView.GONE
                linearLayout.removeAllViews()
            }

            isRouteVisible = !isRouteVisible
        }
    }
}