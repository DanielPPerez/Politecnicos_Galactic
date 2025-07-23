package com.test.galaxyUP.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.test.galaxyUP.R
import com.test.galaxyUP.core.ScoreManager
import com.test.galaxyUP.core.SoundManager
import com.test.galaxyUP.entities.ScoreEntry
import com.test.galaxyUP.ui.ShopView

class MainActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var playButton: Button
    private lateinit var highscoreButton: Button // <-- AÑADIDO
    private lateinit var authGroup: Group
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var confirmRegisterButton: Button
    private lateinit var shopContainer: FrameLayout
    private lateinit var shopView: ShopView

    private lateinit var sharedPreferences: SharedPreferences

    // Lanza el juego y espera a que termine para recibir monedas y puntaje.
    private val gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            // Procesar monedas
            val coinsFromGame = data?.getIntExtra("coins_collected", 0) ?: 0
            if (coinsFromGame > 0) {
                val currentCoins = sharedPreferences.getInt("coins", 0)
                val newTotalCoins = currentCoins + coinsFromGame
                sharedPreferences.edit().putInt("coins", newTotalCoins).apply()
                shopView.setCoins(newTotalCoins)
                Toast.makeText(this, "¡Has ganado $coinsFromGame monedas!", Toast.LENGTH_SHORT).show()
            }

            // <-- CORREGIDO: Procesar y guardar el puntaje
            val scoreFromGame = data?.getIntExtra("player_score", 0) ?: 0
            if (scoreFromGame > 0) {
                val userEmail = sharedPreferences.getString("logged_in_user", "default_user")!!
                val userName = sharedPreferences.getString("name_for_$userEmail", "Jugador") ?: "Jugador"
                val scoreEntry = ScoreEntry(playerName = userName, score = scoreFromGame)
                ScoreManager.saveScore(this, scoreEntry)
                Toast.makeText(this, "Puntaje final: $scoreFromGame", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        if (!sharedPreferences.contains("coins")) {
            sharedPreferences.edit().putInt("coins", 100).apply()
        }

        bindViews()
        setupShop()
        checkLoginStatus()
        setupClickListeners()
    }

    private fun bindViews() {
        titleTextView = findViewById(R.id.titleTextView)
        playButton = findViewById(R.id.playButton)
        highscoreButton = findViewById(R.id.highscoreButton) // <-- AÑADIDO
        authGroup = findViewById(R.id.authGroup)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        confirmRegisterButton = findViewById(R.id.confirmRegisterButton)
        shopContainer = findViewById(R.id.shopContainer)
    }

    private fun setupShop() {
        shopView = ShopView(this)
        shopContainer.addView(shopView)
        shopView.visibility = View.GONE

        shopView.setOnSkinSelectedListener { skinRes ->
            sharedPreferences.edit().putInt("selected_skin", skinRes).apply()
        }
        shopView.setOnBuySkinListener { cost, newCoinTotal ->
            saveOwnedSkinsAndCoins()
        }
    }

    private fun checkLoginStatus() {
        val loggedInUserEmail = sharedPreferences.getString("logged_in_user", null)
        if (loggedInUserEmail != null) {
            val userName = sharedPreferences.getString("name_for_$loggedInUserEmail", "Jugador")
            showWelcomeScreen(userName ?: "Jugador")
        } else {
            showLoginScreen()
        }
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener { showRegistrationScreen() }
        confirmRegisterButton.setOnClickListener { handleRegistration() }
        loginButton.setOnClickListener { handleLogin() }

        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("selected_skin_res", sharedPreferences.getInt("selected_skin", R.drawable.ship1blue))
            }
            gameLauncher.launch(intent)
        }

        // <-- CORREGIDO: Listener para el botón de puntajes
        highscoreButton.setOnClickListener {
            val intent = Intent(this, HighscoreActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoginScreen() {
        titleTextView.visibility = View.GONE
        playButton.visibility = View.GONE
        highscoreButton.visibility = View.GONE // <-- AÑADIDO
        shopContainer.visibility = View.GONE
        authGroup.visibility = View.VISIBLE
        nameEditText.visibility = View.GONE
        confirmRegisterButton.visibility = View.GONE
        loginButton.visibility = View.VISIBLE
        registerButton.visibility = View.VISIBLE
    }

    private fun showRegistrationScreen() {
        nameEditText.visibility = View.VISIBLE
        loginButton.visibility = View.GONE
        registerButton.visibility = View.GONE
        confirmRegisterButton.visibility = View.VISIBLE
    }

    private fun showWelcomeScreen(userName: String) {
        authGroup.visibility = View.GONE
        titleTextView.text = "¡Bienvenido $userName!"
        titleTextView.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        highscoreButton.visibility = View.VISIBLE // <-- AÑADIDO
        shopContainer.visibility = View.VISIBLE
        shopView.visibility = View.VISIBLE
        loadShopData()
    }

    private fun handleRegistration() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        sharedPreferences.edit().apply {
            putString("name_for_$email", name)
            putString("password_for_$email", password)
            apply()
        }
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
        showLoginScreen()
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }
        val savedPassword = sharedPreferences.getString("password_for_$email", null)
        if (savedPassword != null && savedPassword == password) {
            val userName = sharedPreferences.getString("name_for_$email", "Jugador")
            sharedPreferences.edit().putString("logged_in_user", email).apply()
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            showWelcomeScreen(userName ?: "Jugador")
        } else {
            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadShopData() {
        shopView.setCoins(sharedPreferences.getInt("coins", 100))
        val ownedSkinsStr = sharedPreferences.getStringSet("owned_skins", setOf())
        shopView.setOwnedSkins(ownedSkinsStr?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf())
        shopView.setSelectedSkin(sharedPreferences.getInt("selected_skin", R.drawable.ship1blue))
    }

    private fun saveOwnedSkinsAndCoins() {
        sharedPreferences.edit().apply {
            putStringSet("owned_skins", shopView.getOwnedSkins().map { it.toString() }.toSet())
            putInt("coins", shopView.getCoins())
            apply()
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.playMenuMusic(this)
        if (shopContainer.visibility == View.VISIBLE) {
            loadShopData()
        }
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}