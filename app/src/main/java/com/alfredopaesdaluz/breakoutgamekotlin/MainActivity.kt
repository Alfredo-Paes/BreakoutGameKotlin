package com.alfredopaesdaluz.breakoutgamekotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var text2: TextView
    private lateinit var startbtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        text2 = findViewById(R.id.text2)
        startbtn = findViewById(R.id.startbtn)
    }

    fun startGame(view: View) {
        val gameView = GameView(this)
        setContentView(gameView)
    }
}
