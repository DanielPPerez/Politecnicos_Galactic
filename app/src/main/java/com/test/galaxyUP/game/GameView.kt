package com.test.galaxyUP.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.test.galaxyUP.R
import com.test.galaxyUP.entities.*
import com.test.galaxyUP.ui.HUD
import kotlin.random.Random

class GameView(context: Context, private val selectedSkinRes: Int) : SurfaceView(context), SurfaceHolder.Callback {

    private enum class GameState {
        NORMAL_PLAY,
        BOSS_BATTLE,
        PAUSED
    }
    private var currentGameState = GameState.NORMAL_PLAY
    private var gameStateBeforePause = GameState.NORMAL_PLAY

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
    private val bossWaveInterval = 180f
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
    private val gameOverButtonRects: List<Rect> by lazy {
        val btnW = (width * 0.5f).toInt(); val btnH = 120; val spacing = 40; val cX = (width / 2).toFloat(); val cY = (height / 2).toFloat(); val yStart = (cY + 120).toInt()
        val rR = Rect(cX.toInt() - btnW / 2, yStart, cX.toInt() + btnW / 2, yStart + btnH)
        val rM = Rect(cX.toInt() - btnW / 2, yStart + btnH + spacing, cX.toInt() + btnW / 2, yStart + 2 * btnH + spacing)
        listOf(rR, rM)
    }
    var gameOverAction: ((Int) -> Unit)? = null

    private var pauseButtonDrawable: Drawable? = null

    // --- BOTÓN PAUSA MODIFICADO ---
    private val pauseButtonRect: Rect by lazy {
        // 1. Hacer el botón más grande (de 0.12f a 0.15f)
        val btnSize = (width * 0.15f).toInt()

        // 2. Aumentar el margen para bajarlo y separarlo del borde
        val marginH = (width * 0.04f).toInt() // Margen horizontal (desde la derecha)
        val marginV = (width * 0.06f).toInt() // Margen vertical (desde arriba), es más grande ahora

        Rect(
            width - btnSize - marginH,  // left
            marginV,                    // top
            width - marginH,            // right
            marginV + btnSize           // bottom
        )
    }

    private val pauseMenuOverlayPaint = Paint().apply { color = Color.argb(200, 0, 0, 0) }
    private val pauseMenuTitlePaint = Paint().apply { color = Color.WHITE; textSize = 120f; textAlign = Paint.Align.CENTER }
    private val pauseMenuButtonRects: List<Rect> by lazy {
        val btnW = (width * 0.5f).toInt(); val btnH = 120; val spacing = 40; val cX = (width / 2).toFloat(); val cY = (height / 2).toFloat(); val yStart = (cY - btnH / 2).toInt()
        val resumeRect = Rect(cX.toInt() - btnW / 2, yStart, cX.toInt() + btnW / 2, yStart + btnH)
        val menuRect = Rect(cX.toInt() - btnW / 2, yStart + btnH + spacing, cX.toInt() + btnW / 2, yStart + 2 * btnH + spacing)
        listOf(resumeRect, menuRect)
    }
    var pauseAction: ((Int) -> Unit)? = null


    private var leftArrowDrawable: Drawable? = null
    private var rightArrowDrawable: Drawable? = null
    private var isLeftPressed = false
    private var isRightPressed = false

    private val buttonHitboxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 5f
        alpha = 100
    }

    private val leftButtonRect: Rect by lazy {
        val btnSize = (width * 0.22f).toInt()
        val marginV = (height * 0.04f).toInt()
        val marginH = (width * 0.05f).toInt()
        Rect(marginH, height - btnSize - marginV, marginH + btnSize, height - marginV)
    }
    private val rightButtonRect: Rect by lazy {
        val btnSize = (width * 0.22f).toInt()
        val marginV = (height * 0.04f).toInt()
        val marginH = (width * 0.05f).toInt()
        Rect(width - marginH - btnSize, height - btnSize - marginV, width - marginH, height - marginV)
    }

    private var isSetup = false
    private val MAX_ENEMIES = 40
    private val MAX_ASTEROIDS = 30
    private val MAX_BULLETS = 30
    private val enemyPool = ArrayDeque<Enemy>()
    private val asteroidPool = ArrayDeque<Asteroid>()
    private val bulletPool = ArrayDeque<Bullet>()
    private val gridSize = 300
    private val gridCols get() = (width / gridSize) + 1
    private val gridRows get() = (height / gridSize) + 1
    private lateinit var spatialGrid: Array<MutableList<Any>>

    init {
        holder.addCallback(this)
        isFocusable = true
        setLayerType(LAYER_TYPE_HARDWARE, null)
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
        spatialGrid = Array(gridCols * gridRows) { mutableListOf() }
        player = Player(context, screenWidth, screenHeight).apply {
            setSkin(selectedSkinRes)
        }
        hud = HUD(context, screenWidth, screenHeight)
        leftArrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_left)
        rightArrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_right)
        pauseButtonDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause)
        leftArrowDrawable?.bounds = leftButtonRect
        rightArrowDrawable?.bounds = rightButtonRect
        pauseButtonDrawable?.bounds = pauseButtonRect

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
            parallaxLayers.add(ParallaxLayer(context, screenWidth, screenHeight, drawableId, 0.5f + i, fullScreen = false, initialX = xAleatorio))
            parallaxLayers.last().setInitialPosition(yAleatorio, xAleatorio)
        }
        parallaxLayers.add(ParallaxLayer(context, screenWidth, screenHeight, R.drawable.stars_1, 2f))
        parallaxLayers[0].setInitialPosition(0f)
        parallaxLayers[1].setInitialPosition(0f)
        parallaxLayers[2].setInitialPosition(-screenHeight / 2f)
        parallaxLayers.last().setInitialPosition(0f)
    }

    private fun spawnEnemy() {
        if (enemies.size >= MAX_ENEMIES) return
        val enemy = if (enemyPool.isNotEmpty()) { val reused = enemyPool.removeFirst(); reused.reset(width, height); reused } else Enemy(context, width, height)
        enemies.add(enemy)
    }

    private fun spawnAsteroid() {
        if (asteroids.size >= MAX_ASTEROIDS) return
        val asteroid = if (asteroidPool.isNotEmpty()) asteroidPool.removeFirst() else Asteroid(context, width, height)
        asteroids.add(asteroid)
    }
    private fun spawnBullet(owner: BulletOwner, startX: Float, startY: Float) {
        if (bullets.size >= MAX_BULLETS) return
        val bullet = if (bulletPool.isNotEmpty()) bulletPool.removeFirst() else Bullet(context, startX.toInt(), startY.toInt(), owner)
        bullet.x = startX.toInt(); bullet.y = startY.toInt(); bullet.owner = owner
        bullets.add(bullet)
    }
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
        enemies.clear(); asteroids.clear(); bossesSpawnedThisWave = 0
    }

    private fun spawnSingleBoss() { bosses.add(Boss(context, width, height)) }

    private fun spawnMineWave(boss: Boss, gapPosition: Int) {
        val numberOfColumns = 11
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
        spatialGrid.forEach { it.clear() }
        fun gridIndex(x: Float, y: Float): Int {
            val col = (x / gridSize).toInt().coerceIn(0, gridCols - 1); val row = (y / gridSize).toInt().coerceIn(0, gridRows - 1); return row * gridCols + col
        }
        enemies.forEach { spatialGrid[gridIndex(it.x, it.y)].add(it) }
        asteroids.forEach { spatialGrid[gridIndex(it.x, it.y)].add(it) }
        bullets.forEach { spatialGrid[gridIndex(it.x.toFloat(), it.y.toFloat())].add(it) }
        coins.forEach { spatialGrid[gridIndex(it.x, it.y)].add(it) }
        powerUps.forEach { spatialGrid[gridIndex(it.x, it.y)].add(it) }
        mines.forEach { spatialGrid[gridIndex(it.x, it.y)].add(it) }
        bosses.forEach { spatialGrid[gridIndex(it.x, it.y)].add(it) }
        player?.let { spatialGrid[gridIndex(it.x, it.y)].add(it) }

        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()
        val asteroidsToRemove = mutableListOf<Asteroid>()
        val coinsToRemove = mutableListOf<Coin>()
        val powerUpsToRemove = mutableListOf<PowerUp>()

        player?.let { p ->
            if (p.isLaserActive) {
                val laserWidth = p.width / 2; val laserX = p.x + (p.width / 2f) - (laserWidth / 2f); val laserRect = Rect(laserX.toInt(), 0, (laserX + laserWidth).toInt(), p.y.toInt())
                for (enemy in enemies) { if (Rect.intersects(laserRect, enemy.collisionRect)) { enemiesToRemove.add(enemy); addScore(20) } }
                for (asteroid in asteroids) { if (Rect.intersects(laserRect, asteroid.collisionRect)) { asteroidsToRemove.add(asteroid); addScore(50) } }
            }
            for (bullet in bullets.filter { it.owner == BulletOwner.PLAYER }) {
                for (enemy in enemies) { if (Rect.intersects(bullet.collisionRect, enemy.collisionRect)) { bulletsToRemove.add(bullet); enemiesToRemove.add(enemy); addScore(20) } }
                for (asteroid in asteroids) { if (Rect.intersects(bullet.collisionRect, asteroid.collisionRect)) { bulletsToRemove.add(bullet); asteroid.takeHit(); if (!asteroid.isAlive) { asteroidsToRemove.add(asteroid); addScore(50) } } }
                val bossesToRemove = mutableListOf<Boss>()
                for (boss in bosses) {
                    if (boss.isAlive && Rect.intersects(bullet.collisionRect, boss.collisionRect)) {
                        bulletsToRemove.add(bullet); boss.takeHit(); addScore(10)
                        if (!boss.isAlive) { bossesToRemove.add(boss); addScore(200) }
                    }
                }
                bosses.removeAll(bossesToRemove)
            }
            for (bullet in bullets.filter { it.owner == BulletOwner.ENEMY }) { if (Rect.intersects(bullet.collisionRect, p.collisionRect)) { bulletsToRemove.add(bullet); handlePlayerHit() } }
            for (mine in mines) { if (Rect.intersects(mine.collisionRect, p.collisionRect)) { handlePlayerHit() } }
            val idx = gridIndex(p.x, p.y)
            spatialGrid[idx].forEach {
                when (it) {
                    is Enemy -> if (Rect.intersects(p.collisionRect, it.collisionRect)) { enemiesToRemove.add(it); handlePlayerHit() }
                    is Asteroid -> if (Rect.intersects(p.collisionRect, it.collisionRect)) { asteroidsToRemove.add(it); handlePlayerHit() }
                    is Coin -> if (Rect.intersects(p.collisionRect, it.collisionRect)) { coinsToRemove.add(it); coinsCollected += it.value }
                    is PowerUp -> if (Rect.intersects(p.collisionRect, it.collisionRect)) { activatePowerUp(it.type); powerUpsToRemove.add(it) }
                    is Mine -> if (Rect.intersects(p.collisionRect, it.collisionRect)) { handlePlayerHit() }
                    is Boss -> if (it.isAlive && Rect.intersects(p.collisionRect, it.collisionRect)) { handlePlayerHit() }
                }
            }
        }
        bullets.removeAll(bulletsToRemove); enemies.removeAll(enemiesToRemove); asteroids.removeAll(asteroidsToRemove); coins.removeAll(coinsToRemove); powerUps.removeAll(powerUpsToRemove)
        enemiesToRemove.forEach { enemies.remove(it); enemyPool.add(it) }; asteroidsToRemove.forEach { asteroids.remove(it); asteroidPool.add(it) }; bulletsToRemove.forEach { bullets.remove(it); bulletPool.add(it) }
    }

    private fun addScore(points: Int) {
        val finalPoints = if (multiplierTimer > 0) points * 2 else points; score += finalPoints
    }

    private fun activatePowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.SHIELD -> { shieldTimer = 10f; player?.isShielded = true }
            PowerUpType.MULTIPLIER -> { multiplierTimer = 10f }
            PowerUpType.LASER -> { laserTimer = 5f; player?.isLaserActive = true }
        }
    }

    private fun handlePlayerHit() {
        if (player?.isShielded == true) { player?.isShielded = false; shieldTimer = 0f; return }
        if (player?.isInvincible == true) return
        player?.takeHit(); playerLives--; if (playerLives <= 0) { isGameOver = true }
    }

    fun updateWithDelta(deltaTime: Float) {
        if (currentGameState == GameState.PAUSED || isGameOver || !isSetup) return
        gameTimeSeconds += deltaTime
        val deltaMs = (deltaTime * 1000).toLong()
        if (shieldTimer > 0) { shieldTimer -= deltaTime; if (shieldTimer <= 0) { player?.isShielded = false } }
        if (multiplierTimer > 0) { multiplierTimer -= deltaTime }
        if (laserTimer > 0) { laserTimer -= deltaTime; if (laserTimer <= 0) { player?.isLaserActive = false } }
        when (currentGameState) {
            GameState.NORMAL_PLAY -> {
                bossWaveTimer += deltaTime
                if (bossWaveTimer >= bossWaveInterval) { startBossWave(); bossWaveTimer = 0f }
                enemySpawnTimer += deltaMs; if (enemySpawnTimer >= enemySpawnInterval) { spawnEnemy(); enemySpawnTimer = 0L }
                asteroidSpawnTimer += deltaMs; if (asteroidSpawnTimer >= asteroidSpawnInterval) { spawnAsteroid(); asteroidSpawnTimer = 0L }
                coinSpawnTimer += deltaMs; if (coinSpawnTimer >= coinSpawnInterval) { spawnCoin(); coinSpawnTimer = 0L }
                powerUpSpawnTimer += deltaMs; if (powerUpSpawnTimer >= powerUpSpawnInterval) { spawnPowerUp(); powerUpSpawnTimer = 0L }
            }
            GameState.BOSS_BATTLE -> {
                if (bosses.isEmpty() && bossesSpawnedThisWave < totalBossesInWave) { spawnSingleBoss(); bossesSpawnedThisWave++ }
                else if (bosses.isEmpty() && bossesSpawnedThisWave >= totalBossesInWave) { currentGameState = GameState.NORMAL_PLAY }
            }
            GameState.PAUSED -> { /* No hacer nada */ }
        }
        player?.let { p ->
            playerShootTimer += deltaMs
            if (playerShootTimer >= playerShootInterval && !p.isLaserActive) { spawnBullet(BulletOwner.PLAYER, p.x + p.width / 2, p.y); playerShootTimer = 0L }
        }
        parallaxLayers.forEach { it.update() }; asteroids.removeAll { it.y > height || !it.isAlive }; asteroids.forEach { it.update(deltaTime) }
        enemies.removeAll { it.y > height }; enemies.forEach { enemy -> enemy.update(deltaTime); if (enemy.canShoot) { spawnBullet(BulletOwner.ENEMY, enemy.x + enemy.width / 2f, enemy.y + enemy.height); enemy.resetShootFlag() } }
        powerUps.removeAll { it.y > height }; powerUps.forEach { it.update(deltaTime) }
        bosses.forEach { boss -> boss.update(deltaTime); if (boss.shouldDropMines) { spawnMineWave(boss, boss.gapPosition); boss.resetMineDropFlag() } }
        mines.removeAll { it.y > height }; mines.forEach { it.update(deltaTime) }; bullets.removeAll { it.y < -it.height || it.y > height }; bullets.forEach { it.update(deltaTime) }
        coins.removeAll { it.isOutOfScreen() }; coins.forEach { it.update(deltaTime) }
        player?.update(deltaTime)
        checkCollisions()
    }

    fun getCoinsCollected(): Int = coinsCollected

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!isSetup) return
        canvas.let {
            parallaxLayers.forEach { l -> l.draw(it) }; asteroids.filter { a -> a.y + a.height > 0 && a.y < height }.forEach { a -> a.draw(it) }
            enemies.filter { e -> e.y + e.height > 0 && e.y < height }.forEach { e -> e.draw(it) }; bosses.filter { b -> b.y + b.height > 0 && b.y < height }.forEach { b -> b.draw(it) }
            mines.filter { m -> m.y + m.height > 0 && m.y < height }.forEach { m -> m.draw(it) }; coins.filter { c -> c.y + c.height > 0 && c.y < height }.forEach { c -> c.draw(it) }
            powerUps.filter { p -> p.y + p.height > 0 && p.y < height }.forEach { p -> p.draw(it) }; bullets.filter { b -> b.y + b.height > 0 && b.y < height }.forEach { b -> b.draw(it) }
            player?.draw(it)
            if (::hud.isInitialized) hud.draw(it, score, playerLives, gameTimeSeconds, coinsCollected)

            if (isGameOver) {
                it.drawARGB(180, 0, 0, 0); val cX = (width / 2).toFloat(); val cY = (height / 2).toFloat()
                it.drawText("GAME OVER", cX, cY, gameOverPaint)
                val rects = gameOverButtonRects; it.drawRect(rects[0], gameOverButtonPaint); it.drawRect(rects[1], gameOverButtonPaint)
                val yOff = (gameOverButtonTextPaint.descent() + gameOverButtonTextPaint.ascent()) / 2
                it.drawText("Volver a jugar", cX, rects[0].centerY() - yOff, gameOverButtonTextPaint); it.drawText("Volver al menú", cX, rects[1].centerY() - yOff, gameOverButtonTextPaint)
            } else if (currentGameState == GameState.PAUSED) {
                it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pauseMenuOverlayPaint)
                val cX = (width / 2).toFloat(); val cY = (height / 2).toFloat()
                it.drawText("Pausa", cX, cY - 150, pauseMenuTitlePaint)
                val rects = pauseMenuButtonRects; it.drawRect(rects[0], gameOverButtonPaint); it.drawRect(rects[1], gameOverButtonPaint)
                val yOff = (gameOverButtonTextPaint.descent() + gameOverButtonTextPaint.ascent()) / 2
                it.drawText("Reanudar", cX, rects[0].centerY() - yOff, gameOverButtonTextPaint); it.drawText("Volver al menú", cX, rects[1].centerY() - yOff, gameOverButtonTextPaint)
            } else {
                it.drawRect(leftButtonRect, buttonHitboxPaint); it.drawRect(rightButtonRect, buttonHitboxPaint)
                val whiteColorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                leftArrowDrawable?.alpha = if (isLeftPressed) 255 else 150; leftArrowDrawable?.colorFilter = whiteColorFilter; leftArrowDrawable?.draw(it)
                rightArrowDrawable?.alpha = if (isRightPressed) 255 else 150; rightArrowDrawable?.colorFilter = whiteColorFilter; rightArrowDrawable?.draw(it)
                leftArrowDrawable?.colorFilter = null; rightArrowDrawable?.colorFilter = null
                pauseButtonDrawable?.colorFilter = whiteColorFilter; pauseButtonDrawable?.alpha = 200; pauseButtonDrawable?.draw(it); pauseButtonDrawable?.colorFilter = null
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSetup) return true
        val touchX = event.x.toInt(); val touchY = event.y.toInt()
        if (isGameOver) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                gameOverButtonRects.forEachIndexed { i, rect -> if (rect.contains(touchX, touchY)) { gameOverAction?.invoke(i); return true } }
            }
            return true
        }
        when (currentGameState) {
            GameState.PAUSED -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (pauseMenuButtonRects[0].contains(touchX, touchY)) { currentGameState = gameStateBeforePause; return true }
                    if (pauseMenuButtonRects[1].contains(touchX, touchY)) { pauseAction?.invoke(1); return true }
                }
            }
            GameState.NORMAL_PLAY, GameState.BOSS_BATTLE -> {
                if (event.action == MotionEvent.ACTION_DOWN && pauseButtonRect.contains(touchX, touchY)) {
                    gameStateBeforePause = currentGameState; currentGameState = GameState.PAUSED; isLeftPressed = false; isRightPressed = false; player?.movementState = Player.MovementState.IDLE
                    return true
                }
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        isLeftPressed = false; isRightPressed = false
                        if (rightButtonRect.contains(touchX, touchY)) { player?.movementState = Player.MovementState.MOVING_RIGHT; isRightPressed = true }
                        else if (leftButtonRect.contains(touchX, touchY)) { player?.movementState = Player.MovementState.MOVING_LEFT; isLeftPressed = true }
                        else { player?.movementState = Player.MovementState.IDLE }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { player?.movementState = Player.MovementState.IDLE; isLeftPressed = false; isRightPressed = false }
                }
            }
        }
        return true
    }

    fun setPlayerSkin(skinResId: Int) { player?.setSkin(skinResId) }

    fun pause() {
        if (gameThread?.isAlive == true) {
            gameThread?.setRunning(false)
            var retry = true
            while(retry) { try { gameThread?.join(); retry = false } catch (e: InterruptedException) { e.printStackTrace() } }
        }
    }

    fun resume() {
        if (gameThread == null || gameThread?.state == Thread.State.TERMINATED) {
            gameThread = GameThread(holder, this)
        }
        gameThread?.setRunning(true)
        if (gameThread?.state == Thread.State.NEW) { gameThread?.start() }
    }
}