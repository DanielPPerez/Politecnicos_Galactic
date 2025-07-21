package com.test.galaxyUP.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.test.galaxyUP.R
import com.test.galaxyUP.entities.*
import com.test.galaxyUP.ui.HUD
import kotlin.random.Random

class GameView(context: Context, private val selectedSkinRes: Int) : SurfaceView(context), SurfaceHolder.Callback {

    private enum class GameState {
        NORMAL_PLAY,
        BOSS_BATTLE
    }
    private var currentGameState = GameState.NORMAL_PLAY

    private var gameThread: GameThread? = null
    var player: Player? = null
    private val parallaxLayers = mutableListOf<ParallaxLayer>()
    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val asteroids = mutableListOf<Asteroid>()
    private val bosses = mutableListOf<Boss>()
    private val mines = mutableListOf<Mine>()
    private val coins = mutableListOf<Coin>()

    private val powerUps = mutableListOf<PowerUp>()
    private var powerUpSpawnTimer = 0L
    private val powerUpSpawnInterval = 10000L
    private var shieldTimer = 0f
    private var multiplierTimer = 0f
    private var laserTimer = 0f

    private var coinSpawnTimer = 0L
    private val coinSpawnInterval = 3500L
    private var coinsCollected = 0

    private lateinit var hud: HUD
    private var score = 0
    private var playerLives = 3
    private var gameTimeSeconds = 0f

    private var enemySpawnTimer = 0L
    private val enemySpawnInterval = 1200L
    private var asteroidSpawnTimer = 0L
    private val asteroidSpawnInterval = 2500L

    private var bossWaveTimer = 0f
    private val bossWaveInterval = 180f // 3 minutos
    // --- NUEVO: Variables para controlar la oleada de jefes ---
    private val totalBossesInWave = 2
    private var bossesSpawnedThisWave = 0

    private var playerShootTimer = 0L
    private val playerShootInterval = 300L

    private var isGameOver = false
    private val gameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
    }
    private val gameOverButtonPaint = Paint().apply {
        color = Color.argb(180, 0, 0, 0)
    }
    private val gameOverButtonTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    private var gameOverButtonRects: List<Rect>? = null
    var gameOverAction: ((Int) -> Unit)? = null

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

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    fun setupGame() {
        val screenWidth = width
        val screenHeight = height

        player = Player(context, screenWidth, screenHeight).apply {
            setSkin(selectedSkinRes)
        }
        hud = HUD(context, screenWidth, screenHeight)

        score = 0; playerLives = HUD.MAX_LIVES; gameTimeSeconds = 0f
        currentGameState = GameState.NORMAL_PLAY
        bossWaveTimer = 0f
        bossesSpawnedThisWave = 0

        parallaxLayers.clear(); enemies.clear(); bullets.clear(); asteroids.clear(); mines.clear(); bosses.clear()
        coins.clear(); coinsCollected = 0; coinSpawnTimer = 0L
        powerUps.clear(); powerUpSpawnTimer = 0L; shieldTimer = 0f; multiplierTimer = 0f; laserTimer = 0f
        player?.isShielded = false; player?.isLaserActive = false
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

    private fun spawnEnemy() { enemies.add(Enemy(context, width, height)) }
    private fun spawnAsteroid() { asteroids.add(Asteroid(context, width, height)) }
    private fun spawnBullet(owner: BulletOwner, startX: Float, startY: Float) { bullets.add(Bullet(context, startX.toInt(), startY.toInt(), owner)) }
    private fun spawnCoin() {
        val value = getRandomCoinValue()
        coins.add(Coin(context, width, height, value))
    }
    private fun spawnPowerUp() {
        val type = PowerUpType.values().random()
        powerUps.add(PowerUp(context, width, height, type))
    }
    private fun getRandomCoinValue(): Int {
        return when (Random.nextInt(1, 101)) {
            in 1..65 -> 1; in 66..85 -> 2; in 86..95 -> 5; else -> 10
        }
    }

    private fun startBossWave() {
        currentGameState = GameState.BOSS_BATTLE
        enemies.clear()
        asteroids.clear()
        bossesSpawnedThisWave = 0
    }

    private fun spawnSingleBoss() {
        bosses.add(Boss(context, width, height))
    }

    private fun spawnMineWave(boss: Boss, gapPosition: Int) {
        val numberOfColumns = 7
        val mineSize = width / numberOfColumns
        val startY = boss.y + boss.height

        for (i in 0 until numberOfColumns) {
            if (i != gapPosition && i != gapPosition + 1) {
                val startX = (i * mineSize).toFloat()
                mines.add(Mine(context, startX, startY, mineSize))
            }
        }
    }

    private fun checkCollisions() {
        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()
        val asteroidsToRemove = mutableListOf<Asteroid>()
        val coinsToRemove = mutableListOf<Coin>()
        val powerUpsToRemove = mutableListOf<PowerUp>()

        player?.let { p ->
            if (p.isLaserActive) {
                val laserWidth = p.width / 2
                val laserX = p.x + (p.width / 2f) - (laserWidth / 2f)
                val laserRect = Rect(laserX.toInt(), 0, (laserX + laserWidth).toInt(), p.y.toInt())
                for (enemy in enemies) { if (Rect.intersects(laserRect, enemy.collisionRect)) { enemiesToRemove.add(enemy); addScore(20) } }
                for (asteroid in asteroids) { if (Rect.intersects(laserRect, asteroid.collisionRect)) { asteroidsToRemove.add(asteroid); addScore(50) } }
            }
            for (bullet in bullets.filter { it.owner == BulletOwner.PLAYER }) {
                for (enemy in enemies) { if (Rect.intersects(bullet.collisionRect, enemy.collisionRect)) { bulletsToRemove.add(bullet); enemiesToRemove.add(enemy); addScore(20) } }
                for (asteroid in asteroids) { if (Rect.intersects(bullet.collisionRect, asteroid.collisionRect)) { bulletsToRemove.add(bullet); asteroid.takeHit(); if (!asteroid.isAlive) { asteroidsToRemove.add(asteroid); addScore(50) } } }
                val bossesToRemove = mutableListOf<Boss>()
                for (boss in bosses) {
                    if (boss.isAlive && Rect.intersects(bullet.collisionRect, boss.collisionRect)) {
                        bulletsToRemove.add(bullet)
                        boss.takeHit()
                        addScore(10)
                        if (!boss.isAlive) {
                            bossesToRemove.add(boss)
                            addScore(200)
                        }
                    }
                }
                bosses.removeAll(bossesToRemove)
            }
            for (bullet in bullets.filter { it.owner == BulletOwner.ENEMY }) { if (Rect.intersects(bullet.collisionRect, p.collisionRect)) { bulletsToRemove.add(bullet); handlePlayerHit() } }
            for (mine in mines) { if (Rect.intersects(mine.collisionRect, p.collisionRect)) { handlePlayerHit() } }
            for (enemy in enemies) { if (Rect.intersects(enemy.collisionRect, p.collisionRect)) { enemiesToRemove.add(enemy); handlePlayerHit() } }
            for (asteroid in asteroids) { if (Rect.intersects(asteroid.collisionRect, p.collisionRect)) { asteroidsToRemove.add(asteroid); handlePlayerHit() } }
            for (coin in coins) { if (Rect.intersects(coin.collisionRect, p.collisionRect)) { coinsToRemove.add(coin); coinsCollected += coin.value } }
            for (powerUp in powerUps) { if (Rect.intersects(powerUp.collisionRect, p.collisionRect)) { activatePowerUp(powerUp.type); powerUpsToRemove.add(powerUp) } }
            for (boss in bosses) { if (boss.isAlive && Rect.intersects(boss.collisionRect, p.collisionRect)) { handlePlayerHit() } }
        }

        bullets.removeAll(bulletsToRemove)
        enemies.removeAll(enemiesToRemove)
        asteroids.removeAll(asteroidsToRemove)
        coins.removeAll(coinsToRemove)
        powerUps.removeAll(powerUpsToRemove)
    }

    private fun addScore(points: Int) {
        val finalPoints = if (multiplierTimer > 0) points * 2 else points
        score += finalPoints
    }

    private fun activatePowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.SHIELD -> { shieldTimer = 10f; player?.isShielded = true }
            PowerUpType.MULTIPLIER -> { multiplierTimer = 10f }
            PowerUpType.LASER -> { laserTimer = 5f; player?.isLaserActive = true }
        }
    }

    private fun handlePlayerHit() {
        if (player?.isShielded == true) {
            player?.isShielded = false; shieldTimer = 0f; return
        }
        if (player?.isInvincible == true) return
        player?.takeHit()
        playerLives--
        if (playerLives <= 0) { isGameOver = true }
    }

    fun updateWithDelta(deltaTime: Float) {
        if (!isSetup || isGameOver) return
        gameTimeSeconds += deltaTime
        val deltaMs = (deltaTime * 1000).toLong()

        if (shieldTimer > 0) { shieldTimer -= deltaTime; if (shieldTimer <= 0) { player?.isShielded = false } }
        if (multiplierTimer > 0) { multiplierTimer -= deltaTime }
        if (laserTimer > 0) { laserTimer -= deltaTime; if (laserTimer <= 0) { player?.isLaserActive = false } }

        when (currentGameState) {
            GameState.NORMAL_PLAY -> {
                bossWaveTimer += deltaTime
                if (bossWaveTimer >= bossWaveInterval) {
                    startBossWave()
                    bossWaveTimer = 0f
                }
                enemySpawnTimer += deltaMs; if (enemySpawnTimer >= enemySpawnInterval) { spawnEnemy(); enemySpawnTimer = 0L }
                asteroidSpawnTimer += deltaMs; if (asteroidSpawnTimer >= asteroidSpawnInterval) { spawnAsteroid(); asteroidSpawnTimer = 0L }
                coinSpawnTimer += deltaMs; if (coinSpawnTimer >= coinSpawnInterval) { spawnCoin(); coinSpawnTimer = 0L }
                powerUpSpawnTimer += deltaMs; if (powerUpSpawnTimer >= powerUpSpawnInterval) { spawnPowerUp(); powerUpSpawnTimer = 0L }
            }
            GameState.BOSS_BATTLE -> {
                // Si no hay jefes en pantalla y aún no hemos generado todos los de la oleada...
                if (bosses.isEmpty() && bossesSpawnedThisWave < totalBossesInWave) {
                    spawnSingleBoss()
                    bossesSpawnedThisWave++
                }
                // Si no hay jefes en pantalla y ya los generamos todos, la oleada terminó.
                else if (bosses.isEmpty() && bossesSpawnedThisWave >= totalBossesInWave) {
                    currentGameState = GameState.NORMAL_PLAY
                }
            }
        }

        player?.let { p ->
            playerShootTimer += deltaMs
            if (playerShootTimer >= playerShootInterval && !p.isLaserActive) {
                spawnBullet(BulletOwner.PLAYER, p.x + p.width / 2, p.y); playerShootTimer = 0L
            }
        }

        parallaxLayers.forEach { it.update() }
        asteroids.removeAll { it.y > height || !it.isAlive }; asteroids.forEach { it.update(deltaTime) }
        enemies.removeAll { it.y > height }; enemies.forEach { enemy ->
            enemy.update(deltaTime); if (enemy.canShoot) {
            spawnBullet(BulletOwner.ENEMY, enemy.x + enemy.width / 2f, enemy.y + enemy.height); enemy.resetShootFlag()
        }
        }
        powerUps.removeAll { it.y > height }; powerUps.forEach { it.update(deltaTime) }
        bosses.forEach { boss ->
            boss.update(deltaTime)
            if (boss.shouldDropMines) {
                spawnMineWave(boss, boss.gapPosition); boss.resetMineDropFlag()
            }
        }
        mines.removeAll { it.y > height }; mines.forEach { it.update(deltaTime) }
        bullets.removeAll { it.y < -it.height || it.y > height }; bullets.forEach { it.update() }
        coins.removeAll { it.isOutOfScreen() }; coins.forEach { it.update() }
        player?.update(deltaTime)
        checkCollisions()
    }

    fun getCoinsCollected(): Int = coinsCollected

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!isSetup) return

        canvas.let {
            parallaxLayers.forEach { l -> l.draw(it) }; asteroids.forEach { a -> a.draw(it) }; enemies.forEach { e -> e.draw(it) }
            bosses.forEach { b -> b.draw(it) }; mines.forEach { m -> m.draw(it) }; coins.forEach { c -> c.draw(it) }
            powerUps.forEach { p -> p.draw(it) }
            bullets.forEach { b -> b.draw(it) }
            player?.draw(it)
            if (::hud.isInitialized) hud.draw(it, score, playerLives, gameTimeSeconds, coinsCollected)
            if (isGameOver) {
                it.drawARGB(180, 0, 0, 0); val cX = (width / 2).toFloat(); val cY = (height / 2).toFloat()
                it.drawText("GAME OVER", cX, cY, gameOverPaint)
                val btnW = (width * 0.5f).toInt(); val btnH = 120; val spacing = 40; val yStart = (cY + 120).toInt()
                val rR = Rect(cX.toInt() - btnW / 2, yStart, cX.toInt() + btnW / 2, yStart + btnH)
                val rM = Rect(cX.toInt() - btnW / 2, yStart + btnH + spacing, cX.toInt() + btnW / 2, yStart + 2 * btnH + spacing)
                gameOverButtonRects = listOf(rR, rM); it.drawRect(rR, gameOverButtonPaint); it.drawRect(rM, gameOverButtonPaint)
                val yOff = (gameOverButtonTextPaint.descent() + gameOverButtonTextPaint.ascent()) / 2
                it.drawText("Volver a jugar", cX, rR.centerY() - yOff, gameOverButtonTextPaint)
                it.drawText("Volver al menú", cX, rM.centerY() - yOff, gameOverButtonTextPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSetup) return true
        if (isGameOver) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                gameOverButtonRects?.forEachIndexed { i, rect ->
                    if (rect.contains(event.x.toInt(), event.y.toInt())) {
                        gameOverAction?.invoke(i); return true
                    }
                }
            }
            return true
        }
        val touchX = event.x
        player?.movementState = if (touchX > width / 2f) Player.MovementState.MOVING_RIGHT else Player.MovementState.MOVING_LEFT
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            player?.movementState = Player.MovementState.IDLE
        }
        return true
    }

    fun setPlayerSkin(skinResId: Int) {
        player?.setSkin(skinResId)
    }

    fun pause() {
        if (gameThread?.isAlive == true) {
            gameThread?.setRunning(false)
            var retry = true
            while(retry) {
                try {
                    gameThread?.join()
                    retry = false
                } catch (e: InterruptedException) { e.printStackTrace() }
            }
        }
    }

    fun resume() {
        if (gameThread == null || gameThread?.state == Thread.State.TERMINATED) {
            gameThread = GameThread(holder, this)
        }
        gameThread?.setRunning(true)
        if (gameThread?.state == Thread.State.NEW) {
            gameThread?.start()
        }
    }
}