package com.example.politecnicosgalactic.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.example.politecnicosgalactic.R

class MainActivity : AppCompatActivity() {

    // Variables para los elementos de la UI
    private lateinit var titleTextView: TextView
    private lateinit var playButton: Button
    private lateinit var authGroup: Group
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var confirmRegisterButton: Button

    // Variable para manejar SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Poner en pantalla completa
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)

        // Vincular las variables con los IDs del layout XML
        bindViews()

        // Comprobar si ya hay un usuario logueado
        checkLoginStatus()

        // Configurar los listeners de los botones
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
    }

    private fun checkLoginStatus() {
        // Buscamos si hay un email de usuario logueado guardado
        val loggedInUserEmail = sharedPreferences.getString("logged_in_user", null)
        if (loggedInUserEmail != null) {
            // Si lo hay, buscamos su nombre y mostramos la pantalla de bienvenida
            val userName = sharedPreferences.getString("name_for_$loggedInUserEmail", "Jugador")
            showWelcomeScreen(userName ?: "Jugador")
        } else {
            // Si no, mostramos la pantalla de login
            showLoginScreen()
        }
    }

    private fun setupClickListeners() {
        // Listener para el botón de ir a registrarse
        registerButton.setOnClickListener {
            showRegistrationScreen()
        }

        // Listener para el botón de confirmar el registro
        confirmRegisterButton.setOnClickListener {
            handleRegistration()
        }

        // Listener para el botón de iniciar sesión
        loginButton.setOnClickListener {
            handleLogin()
        }

        // Listener para el botón de jugar
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoginScreen() {
        titleTextView.visibility = View.GONE
        playButton.visibility = View.GONE
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
        titleTextView.text = "¡Bienvenido $userName!" // Personalizamos el título
        titleTextView.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
    }

    private fun handleRegistration() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Usamos el editor de SharedPreferences para guardar los datos
        val editor = sharedPreferences.edit()
        editor.putString("name_for_$email", name)
        editor.putString("password_for_$email", password) // ¡Recuerda, esto no es seguro!
        editor.apply() // .apply() guarda los cambios de forma asíncrona

        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
        showLoginScreen() // Volvemos a la pantalla de login
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Buscamos la contraseña guardada para ese email
        val savedPassword = sharedPreferences.getString("password_for_$email", null)

        if (savedPassword != null && savedPassword == password) {
            // ¡Login correcto!
            val userName = sharedPreferences.getString("name_for_$email", "Jugador")

            // Guardamos el estado de "logueado"
            val editor = sharedPreferences.edit()
            editor.putString("logged_in_user", email)
            editor.apply()

            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            showWelcomeScreen(userName ?: "Jugador")
        } else {
            // Error en el login
            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }
}