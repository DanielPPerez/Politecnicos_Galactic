package com.test.galaxyUP.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.test.galaxyUP.R

class ShopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val skinDrawables = listOf(
        R.drawable.ship1blue, // Skin inicial
        R.drawable.ship1red,
        R.drawable.ship1gray,
        R.drawable.ship1green,
        R.drawable.ship2red,
        R.drawable.ship2gray,
        R.drawable.ship2green,
        R.drawable.ship2blue,
        R.drawable.ship2orange,
        R.drawable.ship3orange
    )

    private var selectedIndex = 0
    private var coins = 0
    private var ownedSkins = mutableSetOf<Int>()
    private var onSkinSelectedListener: ((Int) -> Unit)? = null
    private var onBuySkinListener: ((Int, Int) -> Unit)? = null

    private val skinImageView: ImageView
    private val leftButton: ImageButton
    private val rightButton: ImageButton
    private val buyButton: Button
    private val coinsTextView: TextView
    private val welcomeTextView: TextView

    init {
        id = View.generateViewId()
        // Este fondo semi-transparente permite ver un poco el fondo de la actividad principal
        setBackgroundColor(Color.parseColor("#99000000"))

        // Contenedor principal para centrar el bloque de elementos.
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(CENTER_IN_PARENT)
                // Empuja todo el bloque de la tienda hacia abajo
                topMargin = 100.dpToPx()
            }
        }

        // TextView de bienvenida
        welcomeTextView = TextView(context).apply {
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_HORIZONTAL // Centra el texto en su propio espacio
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Ocupa todo el ancho para centrar
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Contenedor horizontal para los botones y la skin
        val skinRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Un margen superior grande para separar la tienda del saludo
                topMargin = 40.dpToPx() // Ajustado a un valor más razonable que 480
            }
        }

        // Botón izquierdo
        leftButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_arrow_left)
            background = null
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            alpha = 0.7f
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = LinearLayout.LayoutParams(120.dpToPx(), 120.dpToPx()).apply {
                marginEnd = 22.dpToPx()
            }
        }

        // Imagen de la skin
        skinImageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(160.dpToPx(), 160.dpToPx()).apply {
                marginStart = 16.dpToPx()
                marginEnd = 16.dpToPx()
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // Botón derecho
        rightButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_arrow_right)
            background = null
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            alpha = 0.7f
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = LinearLayout.LayoutParams(120.dpToPx(), 120.dpToPx()).apply {
                marginStart = 22.dpToPx()
            }
        }

        skinRow.addView(leftButton)
        skinRow.addView(skinImageView)
        skinRow.addView(rightButton)

        // Botón de seleccionar/comprar
        buyButton = Button(context).apply {
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(40.dpToPx(), 0, 40.dpToPx(), 0)
            setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                52.dpToPx()
            ).apply {
                topMargin = 50.dpToPx()
            }
        }

        // TextView de monedas (posicionado en la esquina superior derecha del RelativeLayout)
        coinsTextView = TextView(context).apply {
            textSize = 20f
            setTextColor(Color.YELLOW)
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(ALIGN_PARENT_TOP)
                addRule(ALIGN_PARENT_END)
                topMargin = 32.dpToPx()
                rightMargin = 32.dpToPx()
            }
        }

        // <-- CORREGIDO: Añadir el saludo ANTES que los otros elementos de la tienda.
        mainContainer.addView(welcomeTextView)
        mainContainer.addView(skinRow)
        mainContainer.addView(buyButton)

        // Finalmente, añadimos el contenedor principal y el texto de monedas al RelativeLayout
        addView(mainContainer)
        addView(coinsTextView)

        // Listeners (sin cambios)
        leftButton.setOnClickListener {
            if (selectedIndex > 0) {
                selectedIndex--
                updateShopUI()
            }
        }

        rightButton.setOnClickListener {
            if (selectedIndex < skinDrawables.size - 1) {
                selectedIndex++
                updateShopUI()
            }
        }

        buyButton.setOnClickListener {
            val skinRes = skinDrawables[selectedIndex]
            if (ownedSkins.contains(skinRes)) {
                onSkinSelectedListener?.invoke(skinRes)
                Toast.makeText(context, "Skin seleccionada", Toast.LENGTH_SHORT).show()
            } else {
                val cost = getCostForSkin(selectedIndex)
                if (coins >= cost) {
                    coins -= cost
                    ownedSkins.add(skinRes)
                    onBuySkinListener?.invoke(cost, coins)
                    updateShopUI()
                    Toast.makeText(context, "¡Skin comprada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No tienes suficientes monedas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        updateShopUI()
    }

    private fun getCostForSkin(index: Int): Int {
        return when {
            index == 0 -> 0
            index <= 4 -> 50
            index <= 8 -> 100
            else -> 150
        }
    }

    fun setCoins(value: Int) {
        coins = value
        coinsTextView.text = "Monedas: $coins"
    }

    // Esta función ahora actualiza el `welcomeTextView` que sí es visible.
    fun setPlayerName(name: String) {
        welcomeTextView.text = "¡Bienvenido $name!"
    }

    fun setOwnedSkins(skins: Set<Int>) {
        ownedSkins = skins.toMutableSet()
        if (skinDrawables.isNotEmpty()) {
            ownedSkins.add(skinDrawables[0])
        }
        updateShopUI()
    }

    fun setOnSkinSelectedListener(listener: (Int) -> Unit) {
        onSkinSelectedListener = listener
    }

    fun setOnBuySkinListener(listener: (Int, Int) -> Unit) {
        onBuySkinListener = listener
    }

    fun setSelectedSkin(skinRes: Int) {
        val idx = skinDrawables.indexOf(skinRes)
        if (idx >= 0) {
            selectedIndex = idx
            updateShopUI()
        }
    }

    private fun updateShopUI() {
        if (selectedIndex >= skinDrawables.size) return

        val skinRes = skinDrawables[selectedIndex]
        skinImageView.setImageResource(skinRes)
        coinsTextView.text = "Monedas: $coins"

        val cost = getCostForSkin(selectedIndex)
        buyButton.text = if (ownedSkins.contains(skinRes)) "Seleccionar" else "Comprar ($cost)"

        leftButton.visibility = if (selectedIndex == 0) View.INVISIBLE else View.VISIBLE
        rightButton.visibility = if (selectedIndex == skinDrawables.size - 1) View.INVISIBLE else View.VISIBLE
    }

    // Métodos de ayuda
    fun getOwnedSkins(): Set<Int> = ownedSkins
    fun getCoins(): Int = coins
    fun getSelectedSkinRes(): Int = skinDrawables[selectedIndex]

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}