package com.example.mas_montaa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelecionadorProvincia : AppCompatActivity() {

    private lateinit var tvComunidad: TextView
    private lateinit var ivImagen: ImageView
    private lateinit var btnIzquierda: Button
    private lateinit var btnDerecha: Button
    private lateinit var dbHelper: Helper

    private var provincias = mutableListOf<String>()
    private var indiceActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slecionador_provincia)

        tvComunidad = findViewById(R.id.tvComunidad)
        ivImagen = findViewById(R.id.ivImagen)
        btnIzquierda = findViewById(R.id.btnIzquierda)
        btnDerecha = findViewById(R.id.btnDerecha)

        dbHelper = Helper(this)

        cargarProvincias()
        mostrarProvincia()

        btnIzquierda.setOnClickListener {
            indiceActual = if (indiceActual == 0) provincias.size - 1 else indiceActual - 1
            mostrarProvincia()
        }

        btnDerecha.setOnClickListener {
            indiceActual = (indiceActual + 1) % provincias.size
            mostrarProvincia()
        }

        ivImagen.setOnClickListener {
            val intent = Intent(this, SelecionadorRuta::class.java)
            intent.putExtra("provincia", provincias[indiceActual])
            startActivity(intent)
        }
    }

    private fun cargarProvincias() {
        val db = dbHelper.openDatabase()
        val cursor = db.rawQuery("SELECT DISTINCT provincia FROM rutas", null)
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            provincias.add(nombre)
        }
        cursor.close()
        db.close()
    }

    private fun mostrarProvincia() {
        if (provincias.isNotEmpty()) {
            val nombre = provincias[indiceActual]
            tvComunidad.text = nombre

            // Convierte nombre a lowercase y sin espacios para buscar el drawable
            val drawableName = nombre.lowercase().replace(" ", "")
            val imagenId = resources.getIdentifier(drawableName, "drawable", packageName)
            if (imagenId != 0) {
                ivImagen.setImageResource(imagenId)
            } else {
                ivImagen.setImageResource(R.drawable.madrid) // Una imagen por defecto si no existe
            }
        }
    }
}
