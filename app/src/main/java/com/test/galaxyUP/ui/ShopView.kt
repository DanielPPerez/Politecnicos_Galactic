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
) : FrameLayout(context, attrs, defStyleAttr) {

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

    init {
        setBackgroundColor(Color.parseColor("#99000000"))

        skinImageView = ImageView(context).apply {
            layoutParams = LayoutParams(350, 350, Gravity.CENTER)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        leftButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_arrow_left)
            background = null
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            alpha = 0.7f
            layoutParams = LayoutParams(120, 120, Gravity.START or Gravity.CENTER_VERTICAL)
        }
        rightButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_arrow_right)
            background = null
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            alpha = 0.7f
            layoutParams = LayoutParams(120, 120, Gravity.END or Gravity.CENTER_VERTICAL)
        }
        buyButton = Button(context).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                bottomMargin = 30
            }
        }
        coinsTextView = TextView(context).apply {
            textSize = 24f
            setTextColor(Color.YELLOW)
            setPadding(20, 20, 20, 20)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        }

        addView(skinImageView)
        addView(leftButton)
        addView(rightButton)
        addView(buyButton)
        addView(coinsTextView)

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
        if (index == 0) return 0
        return 50 + 50 * (index - 1)
    }

    fun setCoins(value: Int) {
        coins = value
        updateShopUI()
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

    // --- MÉTODOS AÑADIDOS PARA SOLUCIONAR EL ERROR ---
    fun getOwnedSkins(): Set<Int> = ownedSkins
    fun getCoins(): Int = coins
    fun getSelectedSkinRes(): Int = skinDrawables[selectedIndex]
}