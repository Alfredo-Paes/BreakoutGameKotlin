package com.alfredopaesdaluz.breakoutgamekotlin

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import com.alfredopaesdaluz.breakoutgamekotlin.screens.GameView

class MainActivity : AppCompatActivity() {

    private lateinit var text2: TextView
    private lateinit var startbtn: ImageButton
    private var startSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        text2 = findViewById(R.id.text2)
        startbtn = findViewById(R.id.startbtn)
    }

    /**
     * staryGame(): Responsável por iniciar o jogo, e redirecionar para a tela do game.*/
    fun startGame(view: View) {

        startSound = MediaPlayer.create(this, R.raw.beggin)
        startSound?.start()
        startSound?.setOnCompletionListener {
            it.release()
            startSound = null
        }

        val gameView = GameView(this)
        setContentView(gameView)
    }

    /**
     * onDestroy(): Responsável em encerrar a execução do arquivo de música, para não haver vazamento de memória.
     */
    override fun onDestroy() {
        super.onDestroy()
        startSound?.release()
        startSound = null
    }
}
