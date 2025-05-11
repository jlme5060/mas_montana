package com.example.mas_montaa

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var dbHelper: Helper
    private lateinit var db: SQLiteDatabase
    private lateinit var polyline: Polyline
    private lateinit var infoPanel: LinearLayout
    private lateinit var faunaScroll: ScrollView
    private lateinit var toggleFaunaButton: Button
    private lateinit var changeMapButton: Button
    private lateinit var bottomControls: RelativeLayout
    private var bottomOriginalY: Float = 0f
    private var rutaId: Int = -1

    private val circulosMostrados = mutableListOf<Polygon>()
    private val coloresEspecie = mutableMapOf<String, Int>()

    private val tileSources = listOf(
        TileSourceFactory.MAPNIK,
        TileSourceFactory.USGS_TOPO,
        object : OnlineTileSourceBase(
            "OpenTopoMap", 0, 17, 256, ".png",
            arrayOf("https://a.tile.opentopomap.org/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val zoom = (pMapTileIndex shr 58).toInt()
                val x = (pMapTileIndex shr 29 and 0x1FFFFFFF).toInt()
                val y = (pMapTileIndex and 0x1FFFFFFF).toInt()
                return "$baseUrl$zoom/$x/$y.png"
            }
        }
    )
    private var currentTileIndex = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        rutaId = intent.getIntExtra("rutaId", -1)
        if (rutaId == -1) {
            Toast.makeText(this, "Error: rutaId no recibido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mapView = findViewById(R.id.map)
        dbHelper = Helper(this)
        db = dbHelper.openDatabase()

        changeMapButton = findViewById(R.id.change_map_button)
        toggleFaunaButton = findViewById(R.id.toggle_fauna_button)
        bottomControls = findViewById(R.id.bottom_controls)
        infoPanel = findViewById(R.id.info_panel)
        faunaScroll = findViewById(R.id.fauna_scroll)
        bottomOriginalY = bottomControls.y

        mapView.setTileSource(tileSources[currentTileIndex])
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        mostrarRuta(rutaId)
        mostrarPuntosDeInteres(rutaId)

        changeMapButton.setOnClickListener {
            currentTileIndex = (currentTileIndex + 1) % tileSources.size
            mapView.setTileSource(tileSources[currentTileIndex])
            mapView.invalidate()
        }

        toggleFaunaButton.setOnClickListener {
            val mostrar = faunaScroll.visibility == View.GONE
            faunaScroll.visibility = if (mostrar) View.VISIBLE else View.GONE
            toggleFaunaButton.text = if (mostrar) "▲ Fauna y Flora" else "▼ Fauna y Flora"
            bottomControls.animate()
                .translationY(if (mostrar) bottomOriginalY - 300f else bottomOriginalY)
                .setDuration(300).start()

            if (mostrar) {
                cargarEspeciesDesdeBD()
            }
        }
    }

    private fun mostrarRuta(rutaId: Int) {
        val puntosRuta = mutableListOf<GeoPoint>()
        val cursor = db.rawQuery(
            "SELECT latitud, longitud FROM puntos_ruta WHERE id_ruta = ?",
            arrayOf(rutaId.toString())
        )

        while (cursor.moveToNext()) {
            val lat = cursor.getDouble(cursor.getColumnIndex("latitud"))
            val lon = cursor.getDouble(cursor.getColumnIndex("longitud"))
            puntosRuta.add(GeoPoint(lat, lon))
        }
        cursor.close()

        if (puntosRuta.isNotEmpty()) {
            polyline = Polyline().apply { setPoints(puntosRuta) }
            mapView.overlays.add(polyline)
            mapView.controller.setCenter(puntosRuta.first())
        }

        mapView.invalidate()
    }

    private fun mostrarPuntosDeInteres(rutaId: Int) {
        val cursor = db.rawQuery(
            "SELECT nombre, descripcion, latitud, longitud, tipo FROM puntos_interes WHERE rutaId = ? AND tipo NOT IN ('fauna', 'flora')",
            arrayOf(rutaId.toString())
        )

        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val lat = cursor.getDouble(2)
            val lon = cursor.getDouble(3)
            val iconoNombre = "ic_" + cursor.getString(4)
            Log.d("MiTag", "El icono es: " + iconoNombre)
            val iconoResId = resources.getIdentifier(iconoNombre, "drawable", packageName)
            val drawable = if (iconoResId != 0) {
                ContextCompat.getDrawable(this, iconoResId)
            } else {
                ContextCompat.getDrawable(this, R.drawable.icon_default)
            }

            val marker = Marker(mapView).apply {
                position = GeoPoint(lat, lon)
                title = nombre
                icon = resizeDrawable(drawable, 100, 100) // Con el método corregido
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    enviarInfo(nombre)
                    true
                }
            }


            mapView.overlays.add(marker)

            if (nombre.equals("inicio", ignoreCase = true)) {
                mapView.controller.setCenter(marker.position)
                mapView.controller.setZoom(18.0)
            }
        }

        cursor.close()
        mapView.invalidate()
    }

    private fun cargarEspeciesDesdeBD() {
        infoPanel.removeAllViews()
        val especiesConPuntos = mutableMapOf<String, MutableList<GeoPoint>>()

        val cursor = db.rawQuery(
            "SELECT nombre, latitud, longitud FROM puntos_interes WHERE tipo IN ('fauna', 'flora') AND rutaId = ?",
            arrayOf(rutaId.toString())
        )

        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val lat = cursor.getDouble(1)
            val lon = cursor.getDouble(2)
            val punto = GeoPoint(lat, lon)

            especiesConPuntos.getOrPut(nombre) { mutableListOf() }.add(punto)

            if (!coloresEspecie.containsKey(nombre)) {
                coloresEspecie[nombre] = generarColorAleatorio()
            }
        }
        cursor.close()

        especiesConPuntos.forEach { (especie, puntos) ->
            val label = TextView(this).apply {
                text = especie
                textSize = 18f
                setPadding(16, 8, 16, 8)
                setOnClickListener {
                    circulosMostrados.forEach { mapView.overlays.remove(it) }
                    circulosMostrados.clear()

                    val color = coloresEspecie[especie] ?: Color.RED
                    puntos.forEach { punto ->
                        val circulo = Polygon().apply {
                            points = Polygon.pointsAsCircle(punto, 50.0)
                            fillPaint.color = (color and 0x00FFFFFF) or 0x55000000
                            strokeColor = color
                            strokeWidth = 2f
                            isClickable = true
                            setOnClickListener { _, _, _ ->
                                enviarInfo(especie)
                                true
                            }
                        }
                        mapView.overlays.add(circulo)
                        circulosMostrados.add(circulo)
                    }

                    mapView.controller.animateTo(puntos.first())
                    mapView.invalidate()
                }
            }

            infoPanel.addView(label)
        }
    }

    private fun generarColorAleatorio(): Int {
        val r = (80..200).random()
        val g = (80..200).random()
        val b = (80..200).random()
        return Color.rgb(r, g, b)
    }

    private fun resizeDrawable(drawable: Drawable?, width: Int, height: Int): Drawable? {
        if (drawable == null) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return BitmapDrawable(resources, bitmap)
    }



    private fun enviarInfo(nombre: String) {
        val cursor = db.rawQuery(
            "SELECT descripcion, foto FROM puntos_interes WHERE nombre = ? LIMIT 1",
            arrayOf(nombre)
        )

        if (cursor.moveToFirst()) {
            val descripcion = cursor.getString(cursor.getColumnIndex("descripcion"))
            val fotoNombre = cursor.getString(cursor.getColumnIndex("foto")) ?: "ic_mirador"
            val imagenId = resources.getIdentifier(fotoNombre, "drawable", packageName).takeIf { it != 0 } ?: R.drawable.ic_mirador

            val intent = Intent(this@MainActivity, PuntoDeInteres::class.java).apply {
                putExtra("nombre", nombre)
                putExtra("descripcion", descripcion)
                putExtra("foto", imagenId)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se encontró información de $nombre", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }
}
