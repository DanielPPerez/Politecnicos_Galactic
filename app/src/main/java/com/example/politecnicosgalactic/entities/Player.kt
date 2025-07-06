package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.example.politecnicosgalactic.R

class Player(context: Context, screenWidth: Int, screenHeight: Int) {

    // Imagen de la nave
    var bitmap: Bitmap

    // Posición y dimensiones
    var x: Int
    var y: Int
    var width: Int
    var height: Int

    // Rectángulo de colisión (lo usaremos más adelante)
    val collisionRect: Rect

    init {
        // Cargar la imagen de la nave desde los recursos
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ship_1a_medium)

        // Escalar la nave a un tamaño razonable (ej. 1/10 del ancho de la pantalla)
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        width = screenWidth / 10
        height = (width * originalHeight) / originalWidth // Mantener proporción
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // Posicionar la nave inicialmente
        // Centrada horizontalmente y en la parte inferior de la pantalla
        x = (screenWidth / 2) - (width / 2)
        y = screenHeight - height - 50 // Un pequeño margen desde abajo

        // Inicializar el rectángulo de colisión
        collisionRect = Rect(x, y, x + width, y + height)
    }

    // Actualiza el estado del jugador (por ahora, solo el rectángulo de colisión)
    fun update() {
        collisionRect.left = x
        collisionRect.top = y
        collisionRect.right = x + width
        collisionRect.bottom = y + height
    }

    // Dibuja la nave en el lienzo
    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
    }
}