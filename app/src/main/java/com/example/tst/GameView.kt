package com.example.tst

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.app.AlertDialog
import kotlin.random.Random

class GameView(ctx: Context) : View(ctx) {

    private val paint = Paint()
    private val circles = mutableListOf<Circle>()
    private var targetCircleIndex = 0
    private var draggingCircle: Circle? = null
    private val holeRect = RectF()

    private val targetColor: Int
        get() = circles.getOrNull(targetCircleIndex)?.color ?: circles.firstOrNull()?.color ?: Color.BLACK

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        holeRect.set(left + 100f, bottom - 300f, left + 300f, bottom - 100f)

        // Генерация кружков при первом вызове onLayout
        if (circles.isEmpty()) {
            generateCircles(5) // Создаем 5 случайных кружков
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE) // Рисуем фон

        // Рисуем лунку
        paint.color = targetColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(holeRect, paint)

        // Рисуем кружки
        for (circle in circles) {
            paint.color = circle.color
            canvas.drawCircle(circle.cx, circle.cy, circle.radius, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggingCircle = circles.find { it.contains(event.x, event.y) }
            }
            MotionEvent.ACTION_MOVE -> {
                draggingCircle?.let {
                    it.cx = event.x
                    it.cy = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                draggingCircle?.let {
                    if (holeRect.contains(it.cx, it.cy) && it.color == targetColor) {
                        circles.remove(it) // Удаляем кружок
                        targetCircleIndex++ // Переходим к следующему цвету

                        // Переход к следующему цвету
                        while (targetCircleIndex < circles.size && circles.none { circle -> circle.color == targetColor }) {
                            targetCircleIndex++
                        }

                        if (targetCircleIndex >= circles.size && circles.isNotEmpty()) {
                            targetCircleIndex = 0
                        }

                        if (circles.isEmpty()) {
                            showVictoryDialog() // Показываем диалог победы
                        }
                    }
                    draggingCircle = null
                    invalidate()
                }
            }
        }
        return true
    }

    private fun generateCircles(count: Int) {
        val radius = 50f
        repeat(count) {
            var cx: Float
            var cy: Float
            do {
                cx = Random.nextFloat() * (width - 2 * radius) + radius
                cy = Random.nextFloat() * (height - 2 * radius) + radius
            } while (circles.any { it.overlaps(cx, cy, radius) })
            circles.add(Circle(cx, cy, radius, randomColor()))
        }
    }

    private fun randomColor(): Int {
        return Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
    }

    private fun showVictoryDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Поздравляем!")
        builder.setMessage("Игра окончена! Вы выиграли!")
        builder.setPositiveButton("Рестарт") { dialog, which ->
            restartGame() // Рестарт игры
        }
        builder.show()
    }

    private fun restartGame() {
        // Перезапуск игры: очищаем текущие данные и создаем новые кружки
        circles.clear()
        targetCircleIndex = 0
        generateCircles(5) // Генерация новых кружков
        invalidate() // Обновление вида
    }

    data class Circle(var cx: Float, var cy: Float, val radius: Float, val color: Int) {
        fun contains(x: Float, y: Float): Boolean {
            return (x - cx) * (x - cx) + (y - cy) * (y - cy) <= radius * radius
        }

        fun overlaps(x: Float, y: Float, r: Float): Boolean {
            val dx = cx - x
            val dy = cy - y
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            return distance < radius + r
        }
    }
}
