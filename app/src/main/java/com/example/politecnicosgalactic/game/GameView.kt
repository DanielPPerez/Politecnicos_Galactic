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
import com.example.politecnicosgalactic.entities.* // Importar todas las entidades
import com.example.politecnicosgalactic.ui.HUD
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private lateinit var player: Player
    private val parallaxLayers = mutableListOf<ParallaxLayer>()
    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val asteroids = mutableListOf<Asteroid>()
    private var boss: Boss? = null
    private var isBossActive = false
    private val bouncingBullets = mutableListOf<BouncingBullet>()

    private lateinit var hud: HUD
    private var score = 0
    private var playerLives = 3
    private var gameTimeSeconds = 0f

    private var enemySpawnTimer = 0L
    private val enemySpawnInterval = 1200L
    private var asteroidSpawnTimer = 0L
    private val asteroidSpawnInterval = 2500L

    private var playerShootTimer = 0L
    private val playerShootInterval = 300L

    private var isGameOver = false
    private val gameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
    }

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
        resume()
    }

    private fun setupGame() {
        val screenWidth = width
        val screenHeight = height

        player = Player(context, screenWidth, screenHeight)
        hud = HUD(context, screenWidth, screenHeight)
        score = 0
        playerLives = HUD.MAX_LIVES
        gameTimeSeconds = 0f

        parallaxLayers.clear()
        enemies.clear()
        bullets.clear()
        asteroids.clear()
        bouncingBullets.clear()
        boss = null
        isBossActive = false

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

    private fun spawnEnemy() { enemies.add(Enemy(context, width, height)) }
    private fun spawnAsteroid() { asteroids.add(Asteroid(context, width, height)) }
    private fun spawnBullet(owner: BulletOwner, startX: Float, startY: Float) { bullets.add(Bullet(context, startX.toInt(), startY.toInt(), owner)) }

    private fun spawnBoss() {
        if (boss == null) {
            enemies.clear()
            asteroids.clear()
            bullets.filter { it.owner == BulletOwner.ENEMY }.toMutableList().clear()

            boss = Boss(context, width, height)
            isBossActive = true
        }
    }

    private fun checkCollisions() {
        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()
        val asteroidsToRemove = mutableListOf<Asteroid>()
        val bouncingBulletsToRemove = mutableListOf<BouncingBullet>()

        // Colisiones de balas del jugador
        for (bullet in bullets.filter { it.owner == BulletOwner.PLAYER }) {
            // Contra enemigos normales
            for (enemy in enemies) {
                if (Rect.intersects(bullet.collisionRect, enemy.collisionRect)) {
                    bulletsToRemove.add(bullet)
                    enemiesToRemove.add(enemy)
                    // --- AJUSTE: Puntuación por nave enemiga ---
                    score += 20
                }
            }
            // Contra asteroides
            for (asteroid in asteroids) {
                if (Rect.intersects(bullet.collisionRect, asteroid.collisionRect)) {
                    bulletsToRemove.add(bullet)
                    asteroid.takeHit()
                    if (!asteroid.isAlive) {
                        asteroidsToRemove.add(asteroid)
                        // --- AJUSTE: Puntuación por asteroide ---
                        score += 50
                    }
                }
            }
            // Contra el jefe
            boss?.let {
                if (it.isAlive && Rect.intersects(bullet.collisionRect, it.collisionRect)) {
                    bulletsToRemove.add(bullet)
                    it.takeHit()
                    // --- AJUSTE: Puntuación por golpear al jefe ---
                    score += 10
                    if (!it.isAlive) {
                        // --- AJUSTE: Puntuación por derrotar al jefe ---
                        score += 200
                        isBossActive = false
                        boss = null
                    }
                }
            }
        }

        // Colisiones de balas enemigas (NO rebotadoras) con el jugador
        for (bullet in bullets.filter { it.owner == BulletOwner.ENEMY }) { if (!player.isInvincible && Rect.intersects(bullet.collisionRect, player.collisionRect)) { bulletsToRemove.add(bullet); handlePlayerHit() } }

        // Colisiones de balas del jefe (rebotadoras) con el jugador
        for(bouncingBullet in bouncingBullets) { if (!player.isInvincible && Rect.intersects(bouncingBullet.collisionRect, player.collisionRect)) { bouncingBulletsToRemove.add(bouncingBullet); handlePlayerHit() } }

        // Colisiones de naves con jugador
        for (enemy in enemies) { if (!player.isInvincible && Rect.intersects(enemy.collisionRect, player.collisionRect)) { enemiesToRemove.add(enemy); handlePlayerHit() } }
        for (asteroid in asteroids) { if (!player.isInvincible && Rect.intersects(asteroid.collisionRect, player.collisionRect)) { asteroidsToRemove.add(asteroid); handlePlayerHit() } }
        boss?.let { if (it.isAlive && !player.isInvincible && Rect.intersects(it.collisionRect, player.collisionRect)) { handlePlayerHit() } }

        bullets.removeAll(bulletsToRemove)
        enemies.removeAll(enemiesToRemove)
        asteroids.removeAll(asteroidsToRemove)
        bouncingBullets.removeAll(bouncingBulletsToRemove)
    }


    private fun handlePlayerHit() {
        player.takeHit()
        playerLives--
        if (playerLives <= 0) { isGameOver = true }
    }

    fun updateWithDelta(deltaTime: Float) {
        if (!isSetup || isGameOver) return

        gameTimeSeconds += deltaTime
        val deltaMs = (deltaTime * 1000).toLong()

        if (!isBossActive && gameTimeSeconds >= 180) {
            spawnBoss()
        }

        if (!isBossActive) {
            enemySpawnTimer += deltaMs
            if (enemySpawnTimer >= enemySpawnInterval) { spawnEnemy(); enemySpawnTimer = 0L }
            asteroidSpawnTimer += deltaMs
            if (asteroidSpawnTimer >= asteroidSpawnInterval) { spawnAsteroid(); asteroidSpawnTimer = 0L }
        }

        playerShootTimer += deltaMs
        if (playerShootTimer >= playerShootInterval) {
            spawnBullet(BulletOwner.PLAYER, player.x + player.width / 2, player.y)
            playerShootTimer = 0L
        }

        parallaxLayers.forEach { it.update() }
        asteroids.removeAll { it.y > height || !it.isAlive }
        asteroids.forEach { it.update(deltaTime) }
        enemies.removeAll { it.y > height }
        enemies.forEach { enemy ->
            enemy.update(deltaTime)
            if (enemy.canShoot) {
                spawnBullet(BulletOwner.ENEMY, enemy.x + enemy.width / 2f, enemy.y + enemy.height)
                enemy.resetShootFlag()
            }
        }

        boss?.update(deltaTime)
        boss?.let {
            if (it.canShoot) {
                val bulletBaseSpeed = 200f
                val speedX = bulletBaseSpeed
                val speedY = bulletBaseSpeed / 2

                bouncingBullets.add(BouncingBullet(context, it.x + it.width * 0.1f, it.y + it.height * 0.7f, -speedX, speedY))
                bouncingBullets.add(BouncingBullet(context, it.x + it.width * 0.9f, it.y + it.height * 0.7f, speedX, speedY))
                it.resetShootFlag()
            }
        }

        bouncingBullets.removeAll { it.y > height }
        bouncingBullets.forEach { it.update(width, height) }

        bullets.removeAll { it.y < -it.height || it.y > height }
        bullets.forEach { it.update() }
        player.update(deltaTime)
        checkCollisions()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!isSetup) return

        canvas?.let {
            parallaxLayers.forEach { layer -> layer.draw(it) }
            asteroids.forEach { asteroid -> asteroid.draw(it) }
            enemies.forEach { enemy -> enemy.draw(it) }
            boss?.draw(it)
            bouncingBullets.forEach { bullet -> bullet.draw(it) }

            bullets.forEach { bullet -> bullet.draw(it) }
            player.draw(it)

            hud.draw(it, score, playerLives, gameTimeSeconds)

            if (isGameOver) {
                it.drawARGB(180, 0, 0, 0)
                val centerX = (width / 2).toFloat()
                val centerY = (height / 2).toFloat()
                it.drawText("GAME OVER", centerX, centerY, gameOverPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSetup) return true
        if (isGameOver) { if (event.action == MotionEvent.ACTION_DOWN) { setupGame() }; return true }
        val touchX = event.x
        val screenCenter = width / 2f
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                player.movementState = if (touchX > screenCenter) Player.MovementState.MOVING_RIGHT else Player.MovementState.MOVING_LEFT
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                player.movementState = Player.MovementState.IDLE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        gameThread?.setRunning(false)
        try { gameThread?.join() } catch (e: InterruptedException) { e.printStackTrace() }
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