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
import androidx.lifecycle.lifecycleScope
import com.test.galaxyUP.R
import com.test.galaxyUP.api.*
import com.test.galaxyUP.core.SoundManager
import com.test.galaxyUP.ui.ShopView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // --- Declaración de Vistas ---
    private lateinit var titleTextView: TextView
    private lateinit var playButton: Button
    private lateinit var highscoreButton: Button
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

    // Propiedad para acceder al token de autenticación
    private var authToken: String?
        get() = sharedPreferences.getString("auth_token", null)
        set(value) {
            sharedPreferences.edit().putString("auth_token", value).apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)

        // Orden de inicialización correcto para evitar crashes
        bindViews()
        setupClickListeners()
        setupShop()
        checkLoginStatus()
    }

    private fun bindViews() {
        titleTextView = findViewById(R.id.titleTextView)
        playButton = findViewById(R.id.playButton)
        highscoreButton = findViewById(R.id.highscoreButton)
        authGroup = findViewById(R.id.authGroup)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        confirmRegisterButton = findViewById(R.id.confirmRegisterButton)
        shopContainer = findViewById(R.id.shopContainer)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener { showRegistrationScreen() }
        confirmRegisterButton.setOnClickListener { handleRegistration() }
        loginButton.setOnClickListener { handleLogin() }
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("selected_skin_res", sharedPreferences.getInt("selected_skin", R.drawable.ship1blue))
            }
            startActivity(intent)
        }
        highscoreButton.setOnClickListener {
            startActivity(Intent(this, HighscoreActivity::class.java))
        }
    }

    private fun setupShop() {
        shopView = ShopView(this)
        shopContainer.addView(shopView)
        shopView.visibility = View.GONE
        shopView.setOnSkinSelectedListener { skinRes ->
            sharedPreferences.edit().putInt("selected_skin", skinRes).apply()
        }
        shopView.setOnBuySkinListener { cost, _ ->
            performPurchase(cost)
        }
    }

    private fun checkLoginStatus() {
        if (authToken != null) {
            fetchUserProfile()
        } else {
            showLoginScreen()
        }
    }

    private fun showLoginScreen() {
        titleTextView.visibility = View.GONE
        playButton.visibility = View.GONE
        highscoreButton.visibility = View.GONE
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

    private fun showWelcomeScreen(userName: String, coins: Int) {
        authGroup.visibility = View.GONE
        titleTextView.text = "¡Bienvenido $userName!"
        titleTextView.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        highscoreButton.visibility = View.VISIBLE
        shopContainer.visibility = View.VISIBLE
        shopView.visibility = View.VISIBLE
        shopView.setCoins(coins)
        loadLocalShopData()
    }

    private fun handleRegistration() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(username = name, email = email, password = password)
                val response = ApiClient.instance.register(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MainActivity, "¡Registro exitoso! Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
                    showLoginScreen()
                } else {
                    val errorMsg = response.body()?.message ?: "Error desconocido en el registro."
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = AuthRequest(login = email, password = password)
                val response = ApiClient.instance.login(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.token?.let {
                        authToken = it
                        Toast.makeText(this@MainActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        fetchUserProfile()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Credenciales incorrectas."
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchUserProfile() {
        lifecycleScope.launch {
            val token = authToken
            if (token == null) {
                showLoginScreen() // Asegurarse de volver al login si no hay token
                return@launch
            }
            try {
                val response = ApiClient.instance.getProfile("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.profile?.let {
                        showWelcomeScreen(it.username, it.monedas)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Sesión expirada.", Toast.LENGTH_SHORT).show()
                    authToken = null
                    showLoginScreen()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red al cargar perfil.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performPurchase(cost: Int) {
        val token = authToken
        if (token == null) {
            Toast.makeText(this, "Debes iniciar sesión para comprar.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = SpendCoinsRequest(amount = cost)
                val response = ApiClient.instance.spendCoins("Bearer $token", request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val newCoinTotal = response.body()?.data?.monedasRestantes
                    if (newCoinTotal != null) {
                        shopView.setCoins(newCoinTotal)
                        saveOwnedSkins()
                        Toast.makeText(this@MainActivity, "¡Skin comprada!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "No se pudo completar la compra."
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadLocalShopData() {
        val ownedSkinsStr = sharedPreferences.getStringSet("owned_skins", setOf())
        shopView.setOwnedSkins(ownedSkinsStr?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf())
        shopView.setSelectedSkin(sharedPreferences.getInt("selected_skin", R.drawable.ship1blue))
    }

    private fun saveOwnedSkins() {
        sharedPreferences.edit().apply {
            putStringSet("owned_skins", shopView.getOwnedSkins().map { it.toString() }.toSet())
            apply()
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.playMenuMusic(this)
        if (authToken != null && shopContainer.visibility == View.VISIBLE) {
            fetchUserProfile()
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