package com.example.mas_montaa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

// Datos que se mostrarán en pantalla al seleccionar puntos de interés
data class PuntoInfo(
    val iconoMapa: Int,
    val fotoInfo: Int?,
    val descripcion: String,
    val nombreVisible: String
)

class MainActivity : AppCompatActivity() {

    // Vistas y componentes del layout
    private lateinit var mapView: MapView
    private lateinit var polyline: Polyline
    private lateinit var infoPanel: LinearLayout
    private lateinit var faunaScroll: ScrollView
    private lateinit var toggleFaunaButton: Button
    private lateinit var changeMapButton: Button
    private lateinit var bottomControls: RelativeLayout
    private var bottomOriginalY: Float = 0f

    // Constantes
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val circulosMostrados = mutableListOf<Polygon>()

    // Diccionario de información por punto
    private val infoPuntos = mapOf(
        "🦌 Ciervo" to PuntoInfo(R.drawable.ciervo, R.drawable.ciervo, "El ciervo común...", "Ciervo"),
        "🦊 Zorro" to PuntoInfo(R.drawable.zorro, R.drawable.zorro, "El zorro rojo...", "Zorro rojo"),
        "🦅 Águila real" to PuntoInfo(R.drawable.aguila_real, R.drawable.aguila_real, "El águila real...", "Águila real"),
        "🐍 Víbora hocicuda" to PuntoInfo(R.drawable.vibora, R.drawable.vibora, "La víbora hocicuda...", "Víbora hocicuda"),
        "🌲 Pino silvestre" to PuntoInfo(R.drawable.pino, R.drawable.pino, "El pino silvestre...", "Pino silvestre"),
        "🌿 Helecho común" to PuntoInfo(R.drawable.helecho, R.drawable.helecho, "El helecho común...", "Helecho común"),
        "🌸 Jara pringosa" to PuntoInfo(R.drawable.jara, R.drawable.jara, "La jara pringosa...", "Jara pringosa"),

        "Inicio: Puerto de Cotos" to PuntoInfo(R.drawable.ic_inicio, R.drawable.puerto_cotos, "Punto de partida...", "Puerto de Cotos"),
        "Final: Mirador de la Gitana" to PuntoInfo(R.drawable.ic_final, R.drawable.mirador_gitana, "Punto final...", "Mirador de la Gitana"),
        "Mirador de Zabala" to PuntoInfo(R.drawable.ic_mirador, R.drawable.mirador_de_los_picos, "Un excelente punto...", "Mirador de Zabala"),
        "Mirador del Cielo" to PuntoInfo(R.drawable.ic_mirador, R.drawable.mirador_de_los_picos, "Este mirador ofrece...", "Mirador del Cielo"),
        "Mirador de los Picos" to PuntoInfo(R.drawable.ic_mirador, R.drawable.mirador_de_los_picos, "Permite una observación...", "Mirador de los Picos")
    )

    // Colores para cada especie
    private val coloresEspecie = mapOf(
        "🦌 Ciervo" to 0xFFD32F2F.toInt(),
        "🦊 Zorro" to 0xFFF57C00.toInt(),
        "🦅 Águila real" to 0xFFFBC02D.toInt(),
        "🐍 Víbora hocicuda" to 0xFF388E3C.toInt(),
        "🌲 Pino silvestre" to 0xFF1976D2.toInt(),
        "🌿 Helecho común" to 0xFF7B1FA2.toInt(),
        "🌸 Jara pringosa" to 0xFF6D4C41.toInt()
    )

    // Ruta de puntos del sendero
    private val routePoints = listOf(
        GeoPoint(40.7603, -4.0076), GeoPoint(40.7608, -4.0082), GeoPoint(40.7612, -4.0090),
        GeoPoint(40.7616, -4.0097), GeoPoint(40.7620, -4.0105), GeoPoint(40.7624, -4.0112),
        GeoPoint(40.7627, -4.0120), GeoPoint(40.7631, -4.0128), GeoPoint(40.7635, -4.0135),
        GeoPoint(40.7640, -4.0142), GeoPoint(40.7645, -4.0150), GeoPoint(40.7650, -4.0157),
        GeoPoint(40.7654, -4.0163), GeoPoint(40.7659, -4.0171), GeoPoint(40.7663, -4.0180),
        GeoPoint(40.7668, -4.0188), GeoPoint(40.7670, -4.0192), GeoPoint(40.7672, -4.0196),
        GeoPoint(40.7674, -4.0200)
    )

    // Miradores a mostrar con nombre
    private val miradores = listOf(
        Pair(GeoPoint(40.7660, -4.0160), "Mirador de Zabala"),
        Pair(GeoPoint(40.7650, -4.0145), "Mirador del Cielo"),
        Pair(GeoPoint(40.7640, -4.0130), "Mirador de los Picos")
    )

    // Puntos geográficos para cada especie
    private val especiesConPuntos = mapOf(
        "🦌 Ciervo" to listOf(GeoPoint(40.7632, -4.0140), GeoPoint(40.7660, -4.0165)),
        "🦊 Zorro" to listOf(GeoPoint(40.7655, -4.0135), GeoPoint(40.7640, -4.0120)),
        "🦅 Águila real" to listOf(GeoPoint(40.7668, -4.0183), GeoPoint(40.7672, -4.0200)),
        "🐍 Víbora hocicuda" to listOf(GeoPoint(40.7650, -4.0142)),
        "🌲 Pino silvestre" to listOf(GeoPoint(40.7636, -4.0122), GeoPoint(40.7643, -4.0131)),
        "🌿 Helecho común" to listOf(GeoPoint(40.7608, -4.0082)),
        "🌸 Jara pringosa" to listOf(GeoPoint(40.7672, -4.0200))
    )

    // Fuentes de mapas disponibles
    private val tileSources = listOf(
        TileSourceFactory.MAPNIK,
        TileSourceFactory.USGS_TOPO,
        object : OnlineTileSourceBase("OpenTopoMap", 0, 17, 256, ".png", arrayOf("https://a.tile.opentopomap.org/")) {
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

        // Inicialización de vistas
        mapView = findViewById(R.id.map)
        changeMapButton = findViewById(R.id.change_map_button)
        toggleFaunaButton = findViewById(R.id.toggle_fauna_button)
        bottomControls = findViewById(R.id.bottom_controls)
        infoPanel = findViewById(R.id.info_panel)
        faunaScroll = findViewById(R.id.fauna_scroll)
        bottomOriginalY = bottomControls.y

        // Configuración inicial del mapa
        mapView.setTileSource(tileSources[currentTileIndex])
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(40.7625, -4.0133))

        // Cambio de fuente de mapa al pulsar botón
        changeMapButton.setOnClickListener {
            currentTileIndex = (currentTileIndex + 1) % tileSources.size
            mapView.setTileSource(tileSources[currentTileIndex])
            mapView.invalidate()
        }

        // Solicitud de permisos de localización si no están concedidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }

        // Añadir la ruta al mapa
        polyline = Polyline().apply { setPoints(routePoints) }
        mapView.overlays.add(polyline)

        // Marcadores de inicio, fin y miradores
        addMarkerConInfo(routePoints.first(), "Inicio: Puerto de Cotos")
        addMarkerConInfo(routePoints.last(), "Final: Mirador de la Gitana")
        miradores.forEach { (pt, title) -> addMarkerConInfo(pt, title) }

        // Botón de mostrar/ocultar fauna y flora
        toggleFaunaButton.setOnClickListener {
            val mostrar = faunaScroll.visibility == View.GONE
            faunaScroll.visibility = if (mostrar) View.VISIBLE else View.GONE
            toggleFaunaButton.text = if (mostrar) "▲ Fauna y Flora" else "▼ Fauna y Flora"
            bottomControls.animate().translationY(if (mostrar) bottomOriginalY - 300f else bottomOriginalY).setDuration(300).start()
        }

        // Crear etiquetas de especies y mostrar puntos en el mapa al hacer clic
        especiesConPuntos.forEach { (especie, puntos) ->
            val label = TextView(this).apply {
                text = especie
                textSize = 18f
                setPadding(16, 8, 16, 8)
                setOnClickListener {
                    // Limpiar círculos anteriores
                    circulosMostrados.forEach { mapView.overlays.remove(it) }
                    circulosMostrados.clear()

                    // Crear nuevos círculos
                    val color = coloresEspecie[especie] ?: 0x55000000
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
                }
            }
            infoPanel.addView(label)
        }
    }

    // Agrega un marcador personalizado con evento de clic
    private fun addMarkerConInfo(pt: GeoPoint, title: String) {
        val marker = Marker(mapView)
        val info = infoPuntos[title] ?: return
        marker.position = pt
        marker.icon = resizeDrawable(info.iconoMapa, 100, 100)
        marker.infoWindow = null
        marker.setOnMarkerClickListener { _, _ ->
            enviarInfo(title)
            true
        }
        mapView.overlays.add(marker)
    }

    // Inicia la actividad de detalle de punto de interés
    private fun enviarInfo(key: String) {
        val info = infoPuntos[key] ?: return
        startActivity(Intent(this, PuntoDeInteres::class.java).apply {
            putExtra("nombre", info.nombreVisible)
            putExtra("foto", info.fotoInfo ?: 0)
            putExtra("descripcion", info.descripcion)
        })
    }

    // Redimensiona un drawable a las medidas especificadas
    private fun resizeDrawable(drawableId: Int, width: Int, height: Int): Drawable {
        val drawable = resources.getDrawable(drawableId, null) as BitmapDrawable
        val bmp = Bitmap.createScaledBitmap(drawable.bitmap, width, height, false)
        return BitmapDrawable(resources, bmp)
    }
}
