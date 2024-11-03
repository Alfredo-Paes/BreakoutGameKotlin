package com.alfredopaesdaluz.breakoutgamekotlin.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.view.Display
import android.view.MotionEvent
import android.view.View
import com.alfredopaesdaluz.breakoutgamekotlin.R
import com.alfredopaesdaluz.breakoutgamekotlin.components.BricksComponent
import com.alfredopaesdaluz.breakoutgamekotlin.utils.VelocityUtils
import java.util.*

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var ballX = 0f
    private var ballY = 0f
    private val velocity = VelocityUtils(25, 32)
    private val handler = Handler()
    private val UPDATE_MILLIS = 30L
    private val runnable = Runnable { invalidate() }
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val brickPaint = Paint()
    private val TEXT_SIZE = 120f
    private var paddleX = 0f
    private var paddleY = 0f
    private var oldX = 0f
    private var oldPaddleX = 0f
    private var points = 0
    private var life = 3
    private var ball: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ball)
    private var paddle: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.paddle)
    private var dWidth = 0
    private var dHeight = 0
    private var ballWidth = 0
    private var ballHeight = 0
    private var mpHit: MediaPlayer? = null
    private var mpMiss: MediaPlayer? = null
    private var mpBreak: MediaPlayer? = null
    private val random = Random()
    private val bricks = Array(30) { BricksComponent(0, 0, 0, 0) }
    private var numBricks = 0
    private var brokenBricks = 0
    private var gameOver = false

    init {
        //mpHit = MediaPlayer.create(context, R.raw.hit)
        //mpMiss = MediaPlayer.create(context, R.raw.miss)
        //mpBreak = MediaPlayer.create(context, R.raw.breaking)

        textPaint.color = Color.BLACK
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT

        healthPaint.color = Color.GREEN
        brickPaint.color = Color.argb(255, 249, 129, 0)

        val display: Display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        dWidth = size.x
        dHeight = size.y
        ballX = random.nextInt(dWidth - 50).toFloat()
        ballY = dHeight / 3f
        paddleY = (dHeight * 4) / 5f
        paddleX = (dWidth / 2 - paddle.width / 2).toFloat()
        ballWidth = ball.width
        ballHeight = ball.height

        createBricks()
    }

    private fun createBricks() {
        val brickWidth = dWidth / 8
        val brickHeight = dHeight / 16
        for (column in 0 until 8) {
            for (row in 0 until 3) {
                bricks[numBricks] = BricksComponent(row, column, brickWidth, brickHeight)
                numBricks++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        ballX += velocity.x.toFloat()
        ballY += velocity.y.toFloat()

        // Colisão com as bordas
        if (ballX >= dWidth - ball.width || ballX <= 0) {
            velocity.x *= -1
        }

        if (ballY <= 0) {
            velocity.y *= -1
        }

        // Colisão com o chão
        if (ballY > paddleY + paddle.height) {
            ballX = 1 + random.nextInt(dWidth - ball.width - 1).toFloat()
            ballY = dHeight / 3f
            mpMiss?.start()
            velocity.x = xVelocity()
            velocity.y = 32
            life--
            if (life == 0) {
                gameOver = true
                launchGameOver()
            }
        }

        // Colisão com a plataforma (paddle)
        if ((ballX + ball.width >= paddleX) && (ballX <= paddleX + paddle.width)
            && (ballY + ball.height >= paddleY) && (ballY + ball.height <= paddleY + paddle.height)) {
            mpHit?.start()
            velocity.x += 1
            velocity.y = (velocity.y + 1) * -1
        }

        canvas.drawBitmap(ball, ballX, ballY, null)
        canvas.drawBitmap(paddle, paddleX, paddleY, null)

        // Renderização dos tijolos
        for (i in 0 until numBricks) {
            if (bricks[i].isVisible) {
                canvas.drawRect(
                    (bricks[i].column * bricks[i].width + 1).toFloat(),
                    (bricks[i].row * bricks[i].height + 1).toFloat(),
                    (bricks[i].column * bricks[i].width + bricks[i].width - 1).toFloat(),
                    (bricks[i].row * bricks[i].height + bricks[i].height - 1).toFloat(),
                    brickPaint
                )
            }
        }

        // Desenho da pontuação e das vidas
        canvas.drawText("$points", 20f, TEXT_SIZE, textPaint)
        healthPaint.color = when (life) {
            2 -> Color.YELLOW
            1 -> Color.RED
            else -> Color.GREEN
        }
        canvas.drawRect((dWidth - 200).toFloat(), 30f, (dWidth - 200 + 60 * life).toFloat(), 80f, healthPaint)

        // Colisão da bola com os tijolos
        for (i in 0 until numBricks) {
            if (bricks[i].isVisible) {
                if (ballX + ballWidth >= bricks[i].column * bricks[i].width
                    && ballX <= bricks[i].column * bricks[i].width + bricks[i].width
                    && ballY <= bricks[i].row * bricks[i].height + bricks[i].height
                    && ballY >= bricks[i].row * bricks[i].height
                ) {
                    mpBreak?.start()
                    velocity.y = (velocity.y + 1) * -1
                    bricks[i].setInvisible()
                    points += 10
                    brokenBricks++
                    if (brokenBricks == 24) {
                        launchGameOver()
                    }
                }
            }
        }

        // Verificação de game over
        if (brokenBricks == numBricks) {
            gameOver = true
        }
        if (!gameOver) {
            handler.postDelayed(runnable, UPDATE_MILLIS)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= paddleY) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldPaddleX = paddleX
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift = oldX - touchX
                    val newPaddleX = oldPaddleX - shift
                    paddleX = when {
                        newPaddleX <= 0 -> 0f
                        newPaddleX >= dWidth - paddle.width -> (dWidth - paddle.width).toFloat()
                        else -> newPaddleX
                    }
                }
            }
        }
        return true
    }

    private fun launchGameOver() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOverView::class.java)
        intent.putExtra("points", points)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    private fun xVelocity(): Int {
        val values = intArrayOf(-35, -30, -25, 25, 30, 35)
        return values[random.nextInt(6)]
    }
}
