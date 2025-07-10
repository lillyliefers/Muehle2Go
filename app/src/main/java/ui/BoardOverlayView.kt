package ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BoardOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val linePaint = Paint().apply {
        color = 0xFF000000.toInt()
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val connections = listOf(
        // outer square
        Pair(Pair(0f, 0f), Pair(0f, 3f)), Pair(Pair(0f, 3f), Pair(0f, 6f)),
        Pair(Pair(0f, 6f), Pair(3f, 6f)), Pair(Pair(3f, 6f), Pair(6f, 6f)),
        Pair(Pair(6f, 6f), Pair(6f, 3f)), Pair(Pair(6f, 3f), Pair(6f, 0f)),
        Pair(Pair(6f, 0f), Pair(3f, 0f)), Pair(Pair(3f, 0f), Pair(0f, 0f)),

        // middle square
        Pair(Pair(1f, 1f), Pair(1f, 3f)), Pair(Pair(1f, 3f), Pair(1f, 5f)),
        Pair(Pair(1f, 5f), Pair(3f, 5f)), Pair(Pair(3f, 5f), Pair(5f, 5f)),
        Pair(Pair(5f, 5f), Pair(5f, 3f)), Pair(Pair(5f, 3f), Pair(5f, 1f)),
        Pair(Pair(5f, 1f), Pair(3f, 1f)), Pair(Pair(3f, 1f), Pair(1f, 1f)),

        // inner square
        Pair(Pair(2f, 2f), Pair(2f, 3f)), Pair(Pair(2f, 3f), Pair(2f, 4f)),
        Pair(Pair(2f, 4f), Pair(3f, 4f)), Pair(Pair(3f, 4f), Pair(4f, 4f)),
        Pair(Pair(4f, 4f), Pair(4f, 3f)), Pair(Pair(4f, 3f), Pair(4f, 2f)),
        Pair(Pair(4f, 2f), Pair(3f, 2f)), Pair(Pair(3f, 2f), Pair(2f, 2f)),

        // center connections
        Pair(Pair(0f, 3f), Pair(1f, 3f)), Pair(Pair(1f, 3f), Pair(2f, 3f)),
        Pair(Pair(4f, 3f), Pair(5f, 3f)), Pair(Pair(5f, 3f), Pair(6f, 3f)),
        Pair(Pair(3f, 0f), Pair(3f, 1f)), Pair(Pair(3f, 1f), Pair(3f, 2f)),
        Pair(Pair(3f, 4f), Pair(3f, 5f)), Pair(Pair(3f, 5f), Pair(3f, 6f))
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSizeX = width / 7f
        val cellSizeY = height / 7f

        for ((start, end) in connections) {
            val startX = ((start.second + 0.5f) * cellSizeX)
            val startY = ((start.first + 0.5f) * cellSizeY)
            val endX = ((end.second + 0.5f) * cellSizeX)
            val endY = ((end.first + 0.5f) * cellSizeY)

            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }
}