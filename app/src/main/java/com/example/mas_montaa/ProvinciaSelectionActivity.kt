package com.example.mas_montaa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProvinciaSelectionActivity: AppCompatActivity() {

    private lateinit var tvComunidad: TextView
    private lateinit var ivImagen: ImageView
    private lateinit var btnIzquierda: Button
    private lateinit var btnDerecha: Button

    private val comunidades = listOf(
        "huesca" to R.drawable.huesca,
        "Cantabria" to R.drawable.cantabria,
        "Madrid" to R.drawable.madrid,
        // Agrega más comunidades e imágenes aquí
    )

    private var indiceActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provincia_selection)

        tvComunidad = findViewById(R.id.tvComunidad)
        ivImagen = findViewById(R.id.ivImagen)
        btnIzquierda = findViewById(R.id.btnIzquierda)
        btnDerecha = findViewById(R.id.btnDerecha)

        mostrarComunidad()

        btnIzquierda.setOnClickListener {
            indiceActual = if (indiceActual == 0) comunidades.size - 1 else indiceActual - 1
            mostrarComunidad()
        }

        btnDerecha.setOnClickListener {
            indiceActual = (indiceActual + 1) % comunidades.size
            mostrarComunidad()
        }
        ivImagen.setOnClickListener {
            val intent = Intent(this, RouteSelectionActivity::class.java)
            intent.putExtra("comunidad", comunidades[indiceActual].first)
            startActivity(intent)
        }
    }

    private fun mostrarComunidad() {
        val (nombre, imagenResId) = comunidades[indiceActual]
        tvComunidad.text = nombre
        ivImagen.setImageResource(imagenResId)
    }
}
