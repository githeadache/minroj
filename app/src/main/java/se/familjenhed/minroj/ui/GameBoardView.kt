package se.familjenhed.minroj.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import se.familjenhed.minroj.domain.Cell
import se.familjenhed.minroj.domain.GameStatus

class GameBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    fun interface CellClickListener {
        fun onCellClick(row: Int, col: Int)
    }

    fun interface CellLongClickListener {
        fun onCellLongClick(row: Int, col: Int)
    }

    var cellClickListener: CellClickListener? = null
    var cellLongClickListener: CellLongClickListener? = null
    var cells: List<List<Cell>> = emptyList()
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    var gameStatus: GameStatus = GameStatus.IDLE
        set(value) {
            field = value
            invalidate()
        }

    private val cellSizePx: Float
        get() = resources.displayMetrics.density * CELL_SIZE_DP

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            cellAt(e)?.let { (row, col) ->
                cellClickListener?.onCellClick(row, col)
                performClick()
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            cellAt(e)?.let { (row, col) ->
                cellLongClickListener?.onCellLongClick(row, col)
                performLongClick()
            }
        }
    })

    private fun cellAt(e: MotionEvent): Pair<Int, Int>? {
        val col = (e.x / cellSizePx).toInt()
        val row = (e.y / cellSizePx).toInt()
        val rowCount = cells.size
        val colCount = cells.firstOrNull()?.size ?: 0
        return if (row in 0 until rowCount && col in 0 until colCount) row to col else null
    }

    private val paintCell = Paint().apply { color = 0xFFC0C0C0.toInt() }
    private val paintRevealed = Paint().apply { color = 0xFFBDBDBD.toInt() }
    private val paintExplosion = Paint().apply { color = 0xFFFF0000.toInt() }
    private val paintHighlight = Paint().apply { color = 0xFFFFFFFF.toInt() }
    private val paintShadow = Paint().apply { color = 0xFF808080.toInt() }
    private val paintBorder = Paint().apply { color = 0xFF9E9E9E.toInt() }
    private val paintBlack = Paint().apply { color = 0xFF000000.toInt() }
    private val paintWhite = Paint().apply { color = 0xFFFFFFFF.toInt() }
    private val paintRed = Paint().apply { color = 0xFFCC0000.toInt() }

    private val paintNumber = Paint().apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val numberColors = mapOf(
        1 to 0xFF0000FF.toInt(),
        2 to 0xFF007B00.toInt(),
        3 to 0xFFCC0000.toInt(),
        4 to 0xFF000080.toInt(),
        5 to 0xFF7B0000.toInt(),
        6 to 0xFF007B7B.toInt(),
        7 to 0xFF000000.toInt(),
        8 to 0xFF7B7B7B.toInt()
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cols = cells.firstOrNull()?.size ?: 0
        val rows = cells.size
        setMeasuredDimension((cols * cellSizePx).toInt(), (rows * cellSizePx).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val size = cellSizePx
        paintNumber.textSize = size * 0.6f

        cells.forEachIndexed { row, rowCells ->
            rowCells.forEachIndexed { col, cell ->
                val left = col * size
                val top = row * size
                drawCell(canvas, cell, left, top, size)
            }
        }
    }

    private fun drawCell(canvas: Canvas, cell: Cell, left: Float, top: Float, size: Float) {
        val right = left + size
        val bottom = top + size
        val border = (size * 0.1f).coerceAtLeast(2f)

        if (!cell.isRevealed) {
            canvas.drawRect(left, top, right, bottom, paintCell)
            canvas.drawRect(left, top, right, top + border, paintHighlight)
            canvas.drawRect(left, top, left + border, bottom, paintHighlight)
            canvas.drawRect(left, bottom - border, right, bottom, paintShadow)
            canvas.drawRect(right - border, top, right, bottom, paintShadow)
            if (cell.isFlagged) drawFlag(canvas, left, top, size)
        } else {
            val bg = if (cell.hasMine && gameStatus == GameStatus.LOST) paintExplosion else paintRevealed
            canvas.drawRect(left, top, right, bottom, bg)
            canvas.drawRect(left, top, right, bottom, paintBorder.apply { style = Paint.Style.STROKE; strokeWidth = 1f })
            when {
                cell.hasMine -> drawMine(canvas, left, top, size)
                cell.adjacentMines > 0 -> drawNumber(canvas, cell.adjacentMines, left, top, size)
            }
        }
    }

    private fun drawFlag(canvas: Canvas, left: Float, top: Float, size: Float) {
        val poleX = left + size * 0.35f
        val poleTop = top + size * 0.15f
        val poleBottom = top + size * 0.82f

        val polePaint = Paint().apply { color = 0xFF000000.toInt(); strokeWidth = size * 0.07f; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(poleX, poleTop, poleX, poleBottom, polePaint)

        val path = Path().apply {
            moveTo(poleX, poleTop)
            lineTo(left + size * 0.75f, top + size * 0.32f)
            lineTo(poleX, top + size * 0.49f)
            close()
        }
        canvas.drawPath(path, paintRed)

        val basePaint = Paint().apply { color = 0xFF000000.toInt(); strokeWidth = size * 0.07f; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        canvas.drawLine(left + size * 0.2f, poleBottom, left + size * 0.65f, poleBottom, basePaint)
    }

    private fun drawMine(canvas: Canvas, left: Float, top: Float, size: Float) {
        val cx = left + size / 2
        val cy = top + size / 2
        val r = size * 0.28f

        canvas.drawCircle(cx, cy, r, paintBlack)

        val spikePaint = Paint().apply { color = 0xFF000000.toInt(); strokeWidth = size * 0.09f; strokeCap = Paint.Cap.ROUND; style = Paint.Style.STROKE }
        val spikeLen = size * 0.4f
        for (angle in 0..315 step 45) {
            val rad = Math.toRadians(angle.toDouble())
            canvas.drawLine(
                (cx + r * Math.cos(rad)).toFloat(), (cy + r * Math.sin(rad)).toFloat(),
                (cx + spikeLen * Math.cos(rad)).toFloat(), (cy + spikeLen * Math.sin(rad)).toFloat(),
                spikePaint
            )
        }

        canvas.drawCircle(cx - r * 0.35f, cy - r * 0.35f, r * 0.22f, paintWhite)
    }

    private fun drawNumber(canvas: Canvas, number: Int, left: Float, top: Float, size: Float) {
        paintNumber.color = numberColors[number] ?: 0xFF000000.toInt()
        val cx = left + size / 2
        val cy = top + size / 2 - (paintNumber.descent() + paintNumber.ascent()) / 2
        canvas.drawText(number.toString(), cx, cy, paintNumber)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    companion object {
        private const val CELL_SIZE_DP = 40f
    }
}
