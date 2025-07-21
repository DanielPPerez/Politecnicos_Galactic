package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R

class Player(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    enum class MovementState { IDLE, MOVING_LEFT, MOVING_RIGHT }
    var movementState: MovementState = MovementState.IDLE

    var isInvincible = false
    private var invincibilityTimer = 0f
    private val invincibilityDuration = 2.0f

    var isShielded = false
    private val shieldBitmap: Bitmap

    var isLaserActive = false
    private val laserHeadBitmap: Bitmap
    private val laserBodyBitmap: Bitmap
    private val laserWidth: Int

    var bitmap: Bitmap
    var x: Float
    var y: Float
    var width: Int
    var height: Int

    private val speed = 800f
    val collisionRect: Rect
    private val context: Context

    init {
        this.context = context
        val defaultSkinRes = R.drawable.ship1blue
        val originalBitmap = BitmapFactory.decodeResource(context.resources, defaultSkinRes)

        width = screenWidth / 10
        height = (width * originalBitmap.height) / originalBitmap.width
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        x = (screenWidth / 2f) - (width / 2f)
        y = screenHeight - height - (screenHeight * 0.20f)

        collisionRect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())

        val shieldOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.shield_effect)
        val shieldWidth = (width * 1.2f).toInt()
        val shieldHeight = (height * 1.2f).toInt()
        shieldBitmap = Bitmap.createScaledBitmap(shieldOriginal, shieldWidth, shieldHeight, false)

        // --- CORRECCIÓN DEFINITIVA ---
        laserWidth = width / 2 // Definir el ancho deseado

        val laserHeadOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.laser_head)
        // Calcular la altura manteniendo la proporción BASADA en el nuevo ancho
        val laserHeadHeight = (laserWidth * laserHeadOriginal.height) / laserHeadOriginal.width
        laserHeadBitmap = Bitmap.createScaledBitmap(laserHeadOriginal, laserWidth, laserHeadHeight, false)

        val laserBodyOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.laser_body)
        // Escalar el cuerpo al MISMO ANCHO
        laserBodyBitmap = Bitmap.createScaledBitmap(laserBodyOriginal, laserWidth, laserBodyOriginal.height, true)
    }

    fun setSkin(skinResId: Int) {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, skinResId)
        width = screenWidth / 10
        height = (width * originalBitmap.height) / originalBitmap.width
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }

    fun update(deltaTime: Float) {
        if (isInvincible) {
            invincibilityTimer -= deltaTime
            if (invincibilityTimer <= 0) {
                isInvincible = false
            }
        }
        when (movementState) {
            MovementState.MOVING_LEFT -> x -= speed * deltaTime
            MovementState.MOVING_RIGHT -> x += speed * deltaTime
            MovementState.IDLE -> {}
        }
        x = x.coerceIn(0f, (screenWidth - width).toFloat())
        collisionRect.set(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun draw(canvas: Canvas) {
        if (isInvincible) {
            if ((System.currentTimeMillis() / 100) % 2 == 0L) return
        }
        canvas.drawBitmap(bitmap, x, y, null)

        if (isShielded) {
            val shieldX = x - (shieldBitmap.width - width) / 2f
            val shieldY = y - (shieldBitmap.height - height) / 2f
            canvas.drawBitmap(shieldBitmap, shieldX, shieldY, null)
        }

        if (isLaserActive) {
            drawLaser(canvas)
        }
    }

    private fun drawLaser(canvas: Canvas) {
        val laserX = x + (width / 2f) - (laserWidth / 2f)

        val laserLength = screenHeight * 0.6f
        val laserTopY = y - laserLength

        // El cuerpo del láser se dibuja desde la punta hasta la nave
        val bodyStartY = laserTopY
        val bodyEndY = y
        if (bodyStartY < bodyEndY) {
            val bodyDestRect = Rect(laserX.toInt(), bodyStartY.toInt(), (laserX + laserWidth).toInt(), bodyEndY.toInt())
            canvas.drawBitmap(laserBodyBitmap, null, bodyDestRect, null)
        }

        // La cabeza se dibuja al final, encima de todo, en la punta
        canvas.drawBitmap(laserHeadBitmap, laserX, laserTopY, null)
    }

    fun takeHit() {
        if (!isInvincible && !isShielded) {
            isInvincible = true
            invincibilityTimer = invincibilityDuration
        }
    }
}