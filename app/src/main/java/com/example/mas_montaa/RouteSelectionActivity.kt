package com.example.mas_montaa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class RouteSelectionActivity : AppCompatActivity() {

    private lateinit var dbHelper: Helper
    private lateinit var linearLayout: LinearLayout
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_selection)

        linearLayout = findViewById(R.id.linearLayout)
        scrollView = findViewById(R.id.scrollRuta)
        dbHelper = Helper(this)

        val provinciaSeleccionada = intent.getStringExtra("provincia") ?: ""
        mostrarRutas(provinciaSeleccionada)
    }

    private fun mostrarRutas(provincia: String) {
        linearLayout.removeAllViews()

        val db = dbHelper.openDatabase()
        val cursorRutas = db.rawQuery(
            "SELECT * FROM rutas WHERE provincia = ?",
            arrayOf(provincia)
        )

        while (cursorRutas.moveToNext()) {
            val idRuta = cursorRutas.getInt(cursorRutas.getColumnIndexOrThrow("id"))
            val nombreRuta = cursorRutas.getString(cursorRutas.getColumnIndexOrThrow("nombre"))
            val longitud = cursorRutas.getDouble(cursorRutas.getColumnIndexOrThrow("longitudKm"))
            val altitud = cursorRutas.getDouble(cursorRutas.getColumnIndexOrThrow("altitudMax"))
            val desnivel = cursorRutas.getDouble(cursorRutas.getColumnIndexOrThrow("desnivel"))
            val estrellas = cursorRutas.getInt(cursorRutas.getColumnIndexOrThrow("estrellas"))

            // --- CARD CON SOMBRA Y BORDES REDONDEADOS ---
            val cardView = CardView(this).apply {
                radius = 18f
                cardElevation = 7f
                useCompatPadding = true
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 28)
                }
                setCardBackgroundColor(Color.parseColor("#E0F2F1"))
            }

            val contenedorRuta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, 0, 0)
            }

            // Botón con el nombre de la ruta
            val botonRuta = TextView(this).apply {
                text = nombreRuta
                setBackgroundColor(Color.parseColor("#BBDEFB"))
                setTextColor(Color.parseColor("#000000"))
                textSize = 20f
                setPadding(12, 20, 12, 20)
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 0)
                }
            }

            // Layout vertical que se despliega (contiene horizontal y botón)
            val layoutDetalles = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setPadding(0, 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Layout horizontal para datos y fauna
            val layoutHorizontal = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(Color.WHITE)
            }

            // Columna izquierda: datos de la ruta
            val datosColumna = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val detalles = listOf(
                "Distancia: $longitud km",
                "Altitud Máx: $altitud m",
                "Desnivel: $desnivel m",
                "Estrellas: $estrellas / 5"
            )
            for (dato in detalles) {
                datosColumna.addView(TextView(this).apply {
                    text = dato
                    textSize = 15f
                    setPadding(0, 6, 0, 6)
                    setTextColor(Color.BLACK)
                })
            }

            // Columna derecha: fauna
            val faunaColumna = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(18, 0, 0, 0)
            }

            val cursorFauna = db.rawQuery(
                "SELECT nombre FROM puntos_interes WHERE rutaId = ? AND tipo = 'fauna'",
                arrayOf(idRuta.toString())
            )

            if (cursorFauna.count > 0) {
                faunaColumna.addView(TextView(this).apply {
                    text = "Fauna:"
                    textSize = 15f
                    setPadding(0, 0, 0, 8)
                })

                while (cursorFauna.moveToNext()) {
                    val especie = cursorFauna.getString(0)
                    faunaColumna.addView(TextView(this).apply {
                        text = especie
                        textSize = 15f
                        setTextColor(Color.BLACK)
                        setPadding(14, 6, 14, 6)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 4, 0, 4)
                        }
                    })
                }
            }
            cursorFauna.close()

            // Botón para ver en el mapa (dentro del desplegable)
            val botonMapa = Button(this).apply {
                text = "VER EN MAPA"
                setBackgroundColor(Color.BLACK)
                setTextColor(Color.WHITE)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 18, 0, 0)
                }
                setOnClickListener {
                    val intent = Intent(this@RouteSelectionActivity, MainActivity::class.java)
                    intent.putExtra("rutaId", idRuta)
                    startActivity(intent)
                }
            }

            // Añade columnas al layout horizontal
            layoutHorizontal.addView(datosColumna)
            layoutHorizontal.addView(faunaColumna)

            // Añade horizontal y botón al vertical desplegable
            layoutDetalles.addView(layoutHorizontal)
            layoutDetalles.addView(botonMapa)

            // Toggle del desplegable
            botonRuta.setOnClickListener {
                layoutDetalles.visibility =
                    if (layoutDetalles.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            // Añade todo al contenedor y a la card
            contenedorRuta.addView(botonRuta)
            contenedorRuta.addView(layoutDetalles)
            cardView.addView(contenedorRuta)
            linearLayout.addView(cardView)
        }

        cursorRutas.close()
        db.close()
        scrollView.visibility = View.VISIBLE
    }
}

