package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.example.politecnicosgalactic.R

class Player(context: Context, private val screenWidth: Int, screenHeight: Int) {

    // --- MODIFICADO: Enum TiltState eliminado ya que no hay animación de inclinación ---

    enum class MovementState {
        IDLE,
        MOVING_LEFT,
        MOVING_RIGHT
    }
    var movementState: MovementState = MovementState.IDLE

    var isInvincible = false
    private var invincibilityTimer = 0f
    private val invincibilityDuration = 2.0f // 2 segundos de invencibilidad

    // --- MODIFICADO: Simplificado a un solo bitmap ---
    private val bitmap: Bitmap
    var x: Float
    var y: Float
    var width: Int
    var height: Int

    // --- ELIMINADO: Propiedades de animación ya no son necesarias ---
    // private val frameWidth: Int
    // private val frameHeight: Int
    // private var currentThrusterFrame = 0
    // private var tiltState = TiltState.STRAIGHT
    // private var lastFrameChangeTime = 0L
    // private val animationDelay = 50L
    // companion object { ... }

    private val speed = 800f
    val collisionRect: Rect

    init {
        // --- MODIFICADO: Carga y escalado de una sola imagen ---
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ship1blue)

        // Escalar la nave a un tamaño razonable
        width = screenWidth / 10
        // Calcular la altura manteniendo la proporción de la imagen original
        height = (width * originalBitmap.height) / originalBitmap.width

        // Crear el bitmap escalado final
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        // Posición inicial
        x = (screenWidth / 2f) - (width / 2f)
        y = screenHeight - height - (screenHeight * 0.20f)

        collisionRect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }

    fun update(deltaTime: Float) {
        // Manejo del temporizador de invencibilidad
        if (isInvincible) {
            invincibilityTimer -= deltaTime
            if (invincibilityTimer <= 0) {
                isInvincible = false
            }
        }

        // --- MODIFICADO: La lógica de movimiento ya no actualiza el estado de inclinación ---
        when (movementState) {
            MovementState.MOVING_LEFT -> {
                x -= speed * deltaTime
            }
            MovementState.MOVING_RIGHT -> {
                x += speed * deltaTime
            }
            MovementState.IDLE -> {
                // No hace nada cuando está quieto
            }
        }

        // Limitar la nave a los bordes de la pantalla
        x = x.coerceIn(0f, (screenWidth - width).toFloat())

        // --- ELIMINADO: Lógica de animación de propulsores ---

        // Actualizar el rectángulo de colisión
        collisionRect.left = x.toInt()
        collisionRect.top = y.toInt()
        collisionRect.right = (x + width).toInt()
        collisionRect.bottom = (y + height).toInt()
    }

    fun draw(canvas: Canvas) {
        // Efecto de parpadeo si es invencible (se mantiene)
        if (isInvincible) {
            if ((System.currentTimeMillis() / 100) % 2 == 0L) {
                return // No dibuja la nave en este frame para crear el parpadeo
            }
        }

        // --- MODIFICADO: Dibuja la imagen estática directamente ---
        canvas.drawBitmap(bitmap, x, y, null)
    }

    // Método para cuando el jugador es golpeado (se mantiene)
    fun takeHit() {
        if (!isInvincible) {
            isInvincible = true
            invincibilityTimer = invincibilityDuration
        }
    }
}