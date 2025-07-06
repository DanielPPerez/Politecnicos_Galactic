package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.example.politecnicosgalactic.R
import kotlin.random.Random

class Enemy(context: Context, private val screenWidth: Int, screenHeight: Int) {

    var bitmap: Bitmap
    var x: Int
    var y: Int
    var width: Int
    var height: Int
    private val speedY: Int
    val collisionRect: Rect

    // <<< NUEVO: Variables para el disparo del enemigo >>>
    private var shootTimer = 0L
    // El enemigo disparará en un intervalo aleatorio entre 2 y 5 segundos
    private val shootInterval = Random.nextLong(2000, 5000)
    // Bandera para saber si el enemigo está listo para disparar
    var canShoot = false

    init {
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.enemy_3a_medium)
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        width = screenWidth / 12
        height = (width * originalHeight) / originalWidth
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        speedY = Random.nextInt(5, 12)
        x = Random.nextInt(0, screenWidth - width)
        y = -height

        collisionRect = Rect(x, y, x + width, y + height)
    }

    // <<< MODIFICADO: El método update ahora también gestiona el temporizador de disparo >>>
    fun update(deltaTime: Long) {
        y += speedY
        collisionRect.left = x
        collisionRect.top = y
        collisionRect.right = x + width
        collisionRect.bottom = y + height

        // Actualizar el temporizador de disparo
        shootTimer += deltaTime
        if (shootTimer >= shootInterval) {
            // Cuando el temporizador se cumple, activamos la bandera
            canShoot = true
            // Reiniciamos el temporizador para el próximo disparo
            shootTimer = 0L
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
    }
}