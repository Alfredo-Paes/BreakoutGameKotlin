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
    private val TOP_BAR_HEIGHT = 100f

    private var ballX = 0f
    private var ballY = 0f
    private val velocity = VelocityUtils(25, 32)
    private val handler = Handler()
    private val UPDATE_MILLIS = 30L
    private val runnable = Runnable { invalidate() }

    private val textPaint = Paint()
    private val brickPaint = Paint()
    private val borderPaint = Paint()
    private val backgroundPaint = Paint()
    private val TEXT_SIZE = 60f

    private var paddleX = 0f
    private var paddleY = 0f
    private var oldX = 0f
    private var oldPaddleX = 0f
    private var points = 0
    private var life = 3
    private var lifeHeart: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.life)
    private var ball: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.metal_ball)
    private var paddle: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.paddle)
    private var dWidth = 0
    private var dHeight = 0
    private var ballWidth = 0
    private var ballHeight = 0
    private var soundHitBall: MediaPlayer? = null
    private var soundMissBall: MediaPlayer? = null
    private var soundBreakBrick: MediaPlayer? = null
    private val random = Random()
    private val bricks = Array(30) { BricksComponent(0, 0, 0, 0) }
    private var numBricks = 0
    private var brokenBricks = 0
    private var gameOver = false

    init {
        soundHitBall = MediaPlayer.create(context, R.raw.hit_ball)
        soundMissBall = MediaPlayer.create(context, R.raw.miss_ball)
        soundBreakBrick = MediaPlayer.create(context, R.raw.hit_brick)

        textPaint.color = Color.BLACK
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT

        brickPaint.color = Color.argb(255, 249, 129, 0)
        borderPaint.color = Color.BLACK
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 5f
        backgroundPaint.color = Color.WHITE

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

    /**
     * createBricks(): Método responsável por desenhar um tijolo individualmente, sua altura e
     * largura.
     * */
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

    /**
     * onDraw(canvas: Canvas): Método responsável por realizar o desenho da tela, desde as imagens
     * PNG até os tijolos renderizados.
     * */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#FCF596"))
        ballX += velocity.x.toFloat()
        ballY += velocity.y.toFloat()

        // Desenha o fundo da barra superior
        canvas.drawRect(0f, 0f, dWidth.toFloat(), TOP_BAR_HEIGHT, backgroundPaint)
        // Desenha a borda da barra superior
        canvas.drawRect(0f, 0f, dWidth.toFloat(), TOP_BAR_HEIGHT, borderPaint)

        drawTopBar(canvas)

        // Colisão com a borda superior da barra
        if (ballY <= TOP_BAR_HEIGHT) {
            velocity.y *= -1
            ballY = TOP_BAR_HEIGHT  // Reposiciona a bola logo abaixo da barra superior
        }



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
            soundMissBall?.start()
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
            soundHitBall?.start()
            velocity.x += 1
            velocity.y = (velocity.y + 1) * -1
        }

        canvas.drawBitmap(ball, ballX, ballY, null)
        canvas.drawBitmap(paddle, paddleX, paddleY, null)
        drawBricks(canvas, TOP_BAR_HEIGHT)

        // Colisão da bola com os tijolos
        for (i in 0 until numBricks) {
            if (bricks[i].isVisible) {
                if (ballX + ballWidth >= bricks[i].column * bricks[i].width
                    && ballX <= bricks[i].column * bricks[i].width + bricks[i].width
                    && ballY <= bricks[i].row * bricks[i].height + bricks[i].height
                    && ballY >= bricks[i].row * bricks[i].height
                ) {
                    soundBreakBrick?.start()
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

    /**
     * drawTopBar(canvas: Canvas): Responsável por desenhar uma barra superior no jogo,
     * para conter a pontuação do jogador e a quantidade de vidas do jogador.
     * */
    private fun drawTopBar(canvas: Canvas) {
        // Renderiza a pontuação na barra superior
        canvas.drawText("Score: $points", 20f, TOP_BAR_HEIGHT / 2, textPaint)
        drawLives(canvas)
    }

    /**
     * drawBricks(canvas: Canvas, offsetY: Float): Responsável em renderizar os tijolos do jogo.
     * */
    private fun drawBricks(canvas: Canvas, offsetY: Float) {
        // Desenha cada tijolo, aplicando um deslocamento Y para começar abaixo da barra superior
        for (i in 0 until numBricks) {
            if (bricks[i].isVisible) {
                canvas.drawRect(
                    (bricks[i].column * bricks[i].width + 1).toFloat(),
                    (bricks[i].row * bricks[i].height + 1 + offsetY),
                    (bricks[i].column * bricks[i].width + bricks[i].width - 1).toFloat(),
                    (bricks[i].row * bricks[i].height + bricks[i].height - 1 + offsetY),
                    brickPaint
                )
            }
        }
    }

    /**
     * drawLines(canvas:Canvas): Responsável em renderizar as vidas que o jogador possui, nesse caso, 3 vidas.
     * */
    private fun drawLives(canvas: Canvas) {
        val iconWidth = lifeHeart.width.toFloat()
        val startX = dWidth - 250f  // Posição inicial no eixo X, à direita da tela
        val y = 30f  // Posição no eixo Y para desenhar os ícones

        // Desenha um ícone para cada vida restante
        for (i in 0 until life) {
            val x = startX + i * (iconWidth + 10)  // Calcula a posição X para cada ícone, com espaçamento de 10
            canvas.drawBitmap(lifeHeart, x, y, null)
        }
    }

    /**
     * onTouchEvent(event: MotionEvent): Boolean: Identifica se houve toque na tela e movimentação
     * com gestos no touchscreen do dispositivo.
     */
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

    /**
     * launchGameOver(): Responsável em redireecionar o jogador para a tela de GameOver, independete se ganhou ou perdeu.
     * */
    private fun launchGameOver() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOverView::class.java)
        intent.putExtra("points", points)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    /**
     * xVelocity(): Método por calcular a velocidade da bola.
     * */
    private fun xVelocity(): Int {
        val values = intArrayOf(-35, -30, -25, 25, 30, 35)
        return values[random.nextInt(6)]
    }
}
