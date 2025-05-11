package com.example.mas_montaa

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class Helper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        private const val DB_NAME = "rutas.db"
    }

    private var database: SQLiteDatabase? = null

    override fun onCreate(db: SQLiteDatabase?) {
        // No se usa: base de datos ya creada en assets
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Si hicieras migraciones, irían aquí
    }

    fun openDatabase(forceUpdate: Boolean = false): SQLiteDatabase {
        val dbPath: File = context.getDatabasePath(DB_NAME)
        if (dbPath.exists()) {
            dbPath.delete()  // Elimina la base de datos antigua
        }
        // Copiar la base de datos si no existe o si se fuerza la actualización
        if (forceUpdate || !dbPath.exists()) {
            if (dbPath.exists()) {
                dbPath.delete()
            }

            // Asegúrate de que el directorio exista
            dbPath.parentFile?.mkdirs()

            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }

        database = SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
        return database!!
    }

    override fun close() {
        database?.close()
        super.close()
    }
}
