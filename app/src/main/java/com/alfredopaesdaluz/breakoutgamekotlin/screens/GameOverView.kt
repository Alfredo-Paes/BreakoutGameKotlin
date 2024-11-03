package com.alfredopaesdaluz.breakoutgamekotlin.screens

import android.content.Intent
import android.media.MediaPlayer
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
    private lateinit var victoryText: TextView
    private lateinit var victoryImage: ImageView
    private lateinit var gameOverText: TextView
    private lateinit var playerGameOver: ImageView
    private var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)

        imgBtn1 = findViewById(R.id.replayBtn)
        imgBtn2 = findViewById(R.id.exitBtn)

        gameOverText =findViewById(R.id.gameOverTxt)
        playerGameOver = findViewById(R.id.gameOver)

        victoryText = findViewById(R.id.victoryText)
        victoryImage = findViewById(R.id.trophy)

        playerScore = findViewById(R.id.score)

        val points = intent.extras?.getInt("points") ?: 0

        if (points == 240) {
            victoryText.visibility = View.VISIBLE
            victoryImage.visibility = View.VISIBLE

            gameOverText.visibility = View.GONE
            playerGameOver.visibility = View.GONE

            // Inicializa e toca a música de vitória em loop
            mediaPlayer = MediaPlayer.create(this, R.raw.you_win)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } else {
            gameOverText.visibility = View.VISIBLE
            playerGameOver.visibility = View.VISIBLE

            mediaPlayer = MediaPlayer.create(this, R.raw.game_over)
            mediaPlayer?.isLooping = false
            mediaPlayer?.start()
        }

        playerScore.text = points.toString()
    }
    /**
     * restar(): Método responsável em reiniciar o jogo.
     * */
    fun restart(view: View) {
        mediaPlayer?.stop()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    /**
     * exit(): Encerra a aplicação por completo.
     * */
    fun exit(view: View) {
        mediaPlayer?.stop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
