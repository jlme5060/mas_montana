package com.example.mas_montaa

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PuntoDeInteres : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_punto_de_interes)

        val nombre = intent.getStringExtra("nombre") ?: "Nombre desconocido"
        val descripcion = intent.getStringExtra("descripcion") ?: "Sin descripción disponible"
        val imagenId = intent.getIntExtra("foto", R.drawable.ic_mirador)

        findViewById<TextView>(R.id.miradorTitle).text = nombre
        findViewById<TextView>(R.id.miradorText).text = descripcion
        findViewById<ImageView>(R.id.miradorImage).setImageResource(imagenId)
    }
}
