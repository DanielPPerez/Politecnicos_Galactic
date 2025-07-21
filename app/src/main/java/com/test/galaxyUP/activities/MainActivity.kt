package com.test.galaxyUP.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.test.galaxyUP.R
import com.test.galaxyUP.ui.ShopView
import com.test.galaxyUP.core.SoundManager

class MainActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var playButton: Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        bindViews()
        setupShop()
        checkLoginStatus()
        setupClickListeners()
    }

    private fun bindViews() {
        titleTextView = findViewById(R.id.titleTextView)
        playButton = findViewById(R.id.playButton)
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
            // --- MODIFICADO: Detener la música del menú antes de empezar el juego ---
            SoundManager.stopMusic()

            val intent = Intent(this, GameActivity::class.java).apply {
                val selectedSkin = sharedPreferences.getInt("selected_skin", R.drawable.ship1blue)
                putExtra("selected_skin_res", selectedSkin)
            }
            startActivity(intent) // No necesitamos 'gameLauncher' si GameActivity maneja sus reinicios
        }
    }


    private fun showLoginScreen() {
        titleTextView.visibility = View.GONE
        playButton.visibility = View.GONE
        shopView.visibility = View.GONE
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
        val editor = sharedPreferences.edit()
        editor.putString("name_for_$email", name)
        editor.putString("password_for_$email", password)
        editor.apply()
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
        val coins = sharedPreferences.getInt("coins", 100)
        val selectedSkin = sharedPreferences.getInt("selected_skin", R.drawable.ship1blue)
        val ownedSkinsStr = sharedPreferences.getStringSet("owned_skins", setOf())
        val ownedSkinsInt = ownedSkinsStr?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf()

        shopView.setCoins(coins)
        shopView.setOwnedSkins(ownedSkinsInt)
        shopView.setSelectedSkin(selectedSkin)
    }

    private fun saveOwnedSkinsAndCoins() {
        val ownedSkinsStr = shopView.getOwnedSkins().map { it.toString() }.toSet()
        val coins = shopView.getCoins()
        sharedPreferences.edit()
            .putStringSet("owned_skins", ownedSkinsStr)
            .putInt("coins", coins)
            .apply()
    }

    // --- NUEVO: Manejo del ciclo de vida para la música ---
    override fun onResume() {
        super.onResume()
        // Cuando la actividad vuelve a estar visible, reanuda o inicia la música del menú.
        SoundManager.playMenuMusic(this)
        // También actualizamos la tienda por si volvemos del juego
        if (shopView.visibility == View.VISIBLE) {
            loadShopData()
        }
    }

    override fun onPause() {
        super.onPause()
        // Pausamos la música si la app se va a segundo plano
        SoundManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberamos todos los recursos de música cuando la app se cierra por completo
        SoundManager.release()
    }
}