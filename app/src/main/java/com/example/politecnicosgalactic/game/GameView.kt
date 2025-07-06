package com.example.politecnicosgalactic.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.politecnicosgalactic.R
import com.example.politecnicosgalactic.entities.Bullet
import com.example.politecnicosgalactic.entities.BulletOwner
import com.example.politecnicosgalactic.entities.Enemy
import com.example.politecnicosgalactic.entities.ParallaxLayer
import com.example.politecnicosgalactic.entities.Player
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private lateinit var player: Player
    private val parallaxLayers = mutableListOf<ParallaxLayer>()
    private val enemies = mutableListOf<Enemy>()
    // <<< MODIFICADO: Esta lista ahora contendrá balas de AMBOS tipos >>>
    private val bullets = mutableListOf<Bullet>()

    // Variables para controlar la aparición de enemigos
    private var enemySpawnTimer = 0L
    private val enemySpawnInterval = 1200L // 1.2 segundos

    // <<< NUEVO: Variables para controlar el disparo automático del jugador >>>
    private var playerShootTimer = 0L
    private val playerShootInterval = 300L // 0.3 segundos (cadencia de fuego)

    // <<< NUEVO: Variable para el estado del juego y la pantalla de Game Over >>>
    private var isGameOver = false
    private val gameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
    }

    private var lastFrameTime = 0L
    private var isSetup = false

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!isSetup) {
            setupGame()
            isSetup = true
        }
        lastFrameTime = System.currentTimeMillis()
        resume()
    }

    private fun setupGame() {
        val screenWidth = width
        val screenHeight = height

        player = Player(context, screenWidth, screenHeight)

        parallaxLayers.clear()
        enemies.clear()
        bullets.clear()
        isGameOver = false

        parallaxLayers.add(ParallaxLayer(context, screenWidth, screenHeight, R.drawable.background, 0f, fullScreen = true))
        parallaxLayers.add(ParallaxLayer(context, screenWidth, screenHeight, R.drawable.stars_1, 0f, fullScreen = true))
        parallaxLayers.add(ParallaxLayer(context, screenWidth, screenHeight, R.drawable.stars_2, 1f))
        val cantidadNebulosas = (ParallaxLayer.nebulaFrequency + Random.nextInt(2, 5)).toInt()
        for (i in 0 until cantidadNebulosas) {
            val maxNebulaWidth = (screenWidth * ParallaxLayer.nebulaMaxScale).toInt()
            val rangoX = screenWidth - maxNebulaWidth
            val xAleatorio = if (rangoX > 0) Random.nextInt(0, rangoX).toFloat() else 0f
            val yAleatorio = -Random.nextInt((screenHeight * 0.2f).toInt(), (screenHeight * 0.9f).toInt()).toFloat()
            val drawableId = if (i % 2 == 0) R.drawable.nebula_1 else R.drawable.nebula_2
            parallaxLayers.add(
                ParallaxLayer(context, screenWidth, screenHeight, drawableId, 0.5f + i, fullScreen = false, initialX = xAleatorio)
            )
            parallaxLayers.last().setInitialPosition(yAleatorio, xAleatorio)
        }
        parallaxLayers.add(ParallaxLayer(context, screenWidth, screenHeight, R.drawable.stars_1, 2f))
        parallaxLayers[0].setInitialPosition(0f)
        parallaxLayers[1].setInitialPosition(0f)
        parallaxLayers[2].setInitialPosition(-screenHeight / 2f)
        parallaxLayers[6].setInitialPosition(0f)
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) { pause() }

    private fun spawnEnemy() {
        enemies.add(Enemy(context, width, height))
    }

    private fun spawnBullet(owner: BulletOwner, startX: Int, startY: Int) {
        bullets.add(Bullet(context, startX, startY, owner))
    }

    private fun checkCollisions() {
        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()

        // Comprobar colisiones
        for (bullet in bullets) {
            if (bullet.owner == BulletOwner.PLAYER) {
                // Si la bala es del jugador, comprobar contra enemigos
                for (enemy in enemies) {
                    if (Rect.intersects(bullet.collisionRect, enemy.collisionRect)) {
                        bulletsToRemove.add(bullet)
                        enemiesToRemove.add(enemy)
                    }
                }
            } else { // Si la bala es del ENEMIGO
                // Comprobar contra el jugador
                if (Rect.intersects(bullet.collisionRect, player.collisionRect)) {
                    isGameOver = true
                    return // Fin del juego, no hay necesidad de seguir comprobando
                }
            }
        }

        // Comprobar colisiones de naves (jugador vs enemigo)
        for (enemy in enemies) {
            if (Rect.intersects(enemy.collisionRect, player.collisionRect)) {
                isGameOver = true
                return
            }
        }

        bullets.removeAll(bulletsToRemove)
        enemies.removeAll(enemiesToRemove)
    }

    fun update() {
        if (!isSetup || isGameOver) return

        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        // Temporizador de enemigos
        enemySpawnTimer += deltaTime
        if (enemySpawnTimer >= enemySpawnInterval) {
            spawnEnemy()
            enemySpawnTimer = 0L
        }

        // Temporizador de disparo del jugador
        playerShootTimer += deltaTime
        if (playerShootTimer >= playerShootInterval) {
            val bulletX = player.x + player.width / 2
            val bulletY = player.y
            spawnBullet(BulletOwner.PLAYER, bulletX, bulletY)
            playerShootTimer = 0L
        }

        // Actualizar fondos
        parallaxLayers.forEach { it.update() }

        // Actualizar enemigos
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            enemy.update(deltaTime) // <<< MODIFICADO: Pasar deltaTime al enemigo

            // <<< NUEVO: Comprobar si el enemigo quiere disparar >>>
            if (enemy.canShoot) {
                val bulletX = enemy.x + enemy.width / 2
                val bulletY = enemy.y + enemy.height
                spawnBullet(BulletOwner.ENEMY, bulletX, bulletY)
                enemy.canShoot = false // Apagar la bandera para que no dispare en cada frame
            }

            if (enemy.y > height) {
                enemyIterator.remove()
            }
        }

        // Actualizar balas
        val bulletIterator = bullets.iterator()
        while (bulletIterator.hasNext()) {
            val bullet = bulletIterator.next()
            bullet.update()
            // Eliminar si salen por arriba O por abajo
            if (bullet.y < -bullet.height || bullet.y > height) {
                bulletIterator.remove()
            }
        }

        // Actualizar jugador
        player.update()

        // Comprobar colisiones
        checkCollisions()
    }

    fun updateWithDelta(deltaTime: Float) {
        if (!isSetup || isGameOver) return

        // Convertimos deltaTime (segundos) a milisegundos para mantener la lógica existente
        val deltaMs = (deltaTime * 1000).toLong()
        lastFrameTime = System.currentTimeMillis()

        enemySpawnTimer += deltaMs
        if (enemySpawnTimer >= enemySpawnInterval) {
            spawnEnemy()
            enemySpawnTimer = 0L
        }

        playerShootTimer += deltaMs
        if (playerShootTimer >= playerShootInterval) {
            val bulletX = player.x + player.width / 2
            val bulletY = player.y
            spawnBullet(BulletOwner.PLAYER, bulletX, bulletY)
            playerShootTimer = 0L
        }

        parallaxLayers.forEach { it.update() }

        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            enemy.update(deltaMs) // Pasar deltaMs como deltaTime
            if (enemy.canShoot) {
                val bulletX = enemy.x + enemy.width / 2
                val bulletY = enemy.y + enemy.height
                spawnBullet(BulletOwner.ENEMY, bulletX, bulletY)
                enemy.canShoot = false
            }
            if (enemy.y > height) {
                enemyIterator.remove()
            }
        }

        val bulletIterator = bullets.iterator()
        while (bulletIterator.hasNext()) {
            val bullet = bulletIterator.next()
            bullet.update()
            if (bullet.y < -bullet.height || bullet.y > height) {
                bulletIterator.remove()
            }
        }

        player.update()
        checkCollisions()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!isSetup) return

        canvas?.let {
            // 1. Dibujar fondos
            parallaxLayers.forEach { layer -> layer.draw(it) }

            // 2. Dibujar enemigos
            enemies.forEach { enemy -> enemy.draw(it) }

            // 3. <<< NUEVO: Dibujar balas >>>
            bullets.forEach { bullet -> bullet.draw(it) }

            // 4. Dibujar jugador
            player.draw(it)

            // <<< NUEVO: Si el juego ha terminado, dibujar la pantalla de Game Over >>>
            if (isGameOver) {
                // Dibujar un fondo semitransparente oscuro
                it.drawARGB(180, 0, 0, 0)
                // Dibujar el texto centrado
                val centerX = (width / 2).toFloat()
                val centerY = (height / 2).toFloat()
                it.drawText("GAME OVER", centerX, centerY, gameOverPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSetup) return true

        // <<< NUEVO: Si el juego ha terminado, cualquier toque reinicia el juego >>>
        if (isGameOver) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                setupGame() // Reinicia todas las variables y listas
            }
            return true
        }

        val touchX = event.x
        val touchY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (touchY < player.y) {
                    player.x = (touchX - player.width / 2).toInt()
                    if (player.x < 0) player.x = 0
                    if (player.x > width - player.width) player.x = width - player.width
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        gameThread?.setRunning(false)
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        gameThread = null
    }

    fun resume() {
        if (gameThread == null) {
            gameThread = GameThread(holder, this).apply {
                setRunning(true)
                start()
            }
        }
    }
}

