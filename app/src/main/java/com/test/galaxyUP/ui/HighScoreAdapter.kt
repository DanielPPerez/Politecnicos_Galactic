package com.test.galaxyUP.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.test.galaxyUP.R
import com.test.galaxyUP.api.LeaderboardEntry

class HighscoreAdapter(private val scores: List<LeaderboardEntry>) : RecyclerView.Adapter<HighscoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankTextView: TextView = view.findViewById(R.id.rankTextView)
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val scoreTextView: TextView = view.findViewById(R.id.scoreTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.highscore_item, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val scoreEntry = scores[position]
        holder.rankTextView.text = "${position + 1}."
        holder.nameTextView.text = scoreEntry.usuario.username
        holder.scoreTextView.text = scoreEntry.puntuacion.toString()
    }

    override fun getItemCount() = scores.size
}