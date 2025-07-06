package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import kotlin.random.Random

class ParallaxLayer(
    context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int,
    drawableId: Int,
    val speed: Float, // La velocidad de esta capa en particular
    private val fullScreen: Boolean = false, // Si debe cubrir toda la pantalla
    private val initialX: Float = 0f // Posición X inicial para capas móviles
) {

    private var bitmap: Bitmap
    private val originalBitmap: Bitmap
    private var y: Float = 0f
    private var x: Float = initialX
    private var height: Int
    private var width: Int

    companion object {
        // Permite modificar la frecuencia, tamaño y cantidad de nebulosas
        var nebulaFrequency: Float = 1.0f // 1.0 = normal, <1.0 menos frecuente, >1.0 más frecuente
        var nebulaMinScale: Float = 0.1f
        var nebulaMaxScale: Float = 1.0f
    }

    init {
        // Cargar la imagen desde los recursos
        originalBitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height

        if (fullScreen) {
            // Escalado tipo "center crop" para cubrir toda la pantalla
            val scaleX = screenWidth.toFloat() / originalWidth
            val scaleY = screenHeight.toFloat() / originalHeight
            val scale = maxOf(scaleX, scaleY) // Elige el mayor para cubrir todo
            val scaledWidth = (originalWidth * scale).toInt()
            val scaledHeight = (originalHeight * scale).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

            // Recortar el bitmap al tamaño exacto de la pantalla (centrado)
            val offsetX = (scaledWidth - screenWidth) / 2
            val offsetY = (scaledHeight - screenHeight) / 2
            bitmap = Bitmap.createBitmap(scaledBitmap, offsetX, offsetY, screenWidth, screenHeight)
            height = screenHeight
            width = screenWidth
        } else {
            // Escalar solo al ancho de pantalla, mantener proporción y aplicar escala random para nebulosas
            var scale = 1.0f
            if (isNebulaResource(drawableId)) {
                scale = Random.nextFloat() * (nebulaMaxScale - nebulaMinScale) + nebulaMinScale
            }
            width = (screenWidth * scale).toInt()
            height = (originalBitmap.height * width) / originalBitmap.width
            bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        }

        // Posicionar la capa inicial justo encima de la pantalla
        y = -height.toFloat()
    }

    fun update() {
        // Si la velocidad es 0, no mover la capa
        if (speed == 0f) return
        // Mover la capa hacia abajo según su velocidad
        y += speed

        // Si la capa se ha salido completamente por abajo, la reposicionamos arriba
        if (y > screenHeight) {
            y = -height.toFloat()
            // Si es una nebulosa, reaparecer en una posición X aleatoria y con nuevo tamaño
            if (isNebula()) {
                val scale = Random.nextFloat() * (nebulaMaxScale - nebulaMinScale) + nebulaMinScale
                width = (screenWidth * scale).toInt()
                height = (originalBitmap.height * width) / originalBitmap.width
                bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                x = Random.nextInt(0, screenWidth - width).toFloat()
            }
        }
    }

    private fun isNebula(): Boolean {
        // Puedes ajustar el criterio según tus nombres de recursos
        return this.toString().contains("nebula", ignoreCase = true) ||
               width < screenWidth // Heurística: solo las capas móviles y no fullScreen
    }

    private fun isNebulaResource(drawableId: Int): Boolean {
        // Puedes ajustar esto según tus nombres de recursos
        return drawableId.toString().contains("nebula")
    }

    fun draw(canvas: Canvas) {
        // Si la velocidad es 0, siempre dibujar en y = 0
        if (speed == 0f) {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(bitmap, x, y, null)
        }
    }

    // Un método especial para la primera capa, para que no empiece fuera de la pantalla
    fun setInitialPosition(y: Float, x: Float? = null) {
        this.y = y
        if (x != null) this.x = x
    }

    fun updateWithDelta(deltaTime: Float) {
        if (speed == 0f) return
        y += speed * deltaTime * 60f // Ajuste para mantener velocidad similar a antes
        if (y > screenHeight) {
            y = -height.toFloat()
            if (isNebula()) {
                val scale = Random.nextFloat() * (nebulaMaxScale - nebulaMinScale) + nebulaMinScale
                width = (screenWidth * scale).toInt()
                height = (originalBitmap.height * width) / originalBitmap.width
                bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                x = Random.nextInt(0, screenWidth - width).toFloat()
            }
        }
    }
}
