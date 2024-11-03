package com.alfredopaesdaluz.breakoutgamekotlin.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alfredopaesdaluz.breakoutgamekotlin.MainActivity
import com.alfredopaesdaluz.breakoutgamekotlin.R

class GameOverView : AppCompatActivity() {
    private lateinit var playerScore: TextView
    private lateinit var imgBtn1: ImageButton
    private lateinit var imgBtn2: ImageButton
    //private lateinit var playerWinner: ImageView
    private lateinit var gameOverText: TextView
    private lateinit var playerGameOver: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)

        imgBtn1 = findViewById(R.id.replayBtn)
        imgBtn2 = findViewById(R.id.exitBtn)

        //playerWinner = findViewById(R.id.winner)
        gameOverText =findViewById(R.id.gameOverTxt)
        playerGameOver = findViewById(R.id.gameOver)
        playerScore = findViewById(R.id.score)

        val points = intent.extras?.getInt("points") ?: 0

        if (points == 240) {
            //playerScore.visibility = View.VISIBLE
            //playerWinner.visibility = View.VISIBLE
        } else {
            gameOverText.visibility = View.VISIBLE
            playerGameOver.visibility = View.VISIBLE
        }

        playerScore.text = points.toString()
    }

    fun restart(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun exit(view: View) {
        finish()
    }
}