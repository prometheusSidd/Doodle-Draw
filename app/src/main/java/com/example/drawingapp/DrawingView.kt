package com.example.drawingapp

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View



// 1. Making DrawingView class , And Inherit it from view class :: **
class DrawingView(context: Context, attrs: AttributeSet) : View(context,attrs) { // Inherited view class because If you want your class to be treated by Android system as view contains all necessary code which is required in order for your class to be recognized by Android Sytem..

    // 1 . Setup Our Canvas

    // drawing path ***

    private lateinit var drawPath:FingerPath // It provide Path where do want to draw or go up or down And also any Geometrical path

    // defines what to draw (canvas) ***
    private lateinit var canvasPaint: Paint
    private lateinit var drawPaint: Paint // IT provide method to define that line's color or fill object with color
    private var color = Color.BLACK
    private lateinit var canvas: Canvas  // It hold the draw calls written into bitmap
    private lateinit var canvasBitmap: Bitmap // Bitmap holds the pixels
    private var brushSize: Float = 0.toFloat()

    private val paths = mutableListOf<FingerPath>() // 4. Go to onTouchEvent() -> MotionEvent.... to connect the ' paths ' variable.


    init {
        setUpDrawing()
    }

    // 4 . This function will be called by the system when the user is going to touch the Screen :*****
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x ?:return false
        val touchY = event?.y ?:return false
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                // Create the path when finger touches the screen
                drawPath = FingerPath(color,brushSize)
                drawPath.color = color
                drawPath.brushThickness = brushSize.toFloat()

                drawPath.reset() // here we resetting path before we see initial point
                drawPath.moveTo(touchX!!,touchY!!)

                // Here , the Event will be fired when User starts to move it's finger . this will be fired continually util user pick up his finger
            }   MotionEvent.ACTION_MOVE -> {
                   drawPath.lineTo(touchX!!,touchY!!)
        }
            MotionEvent.ACTION_UP -> {

                paths.add(drawPath) // .5. Connecting 'paths' variable to onTOuchEvent() , GO to onDraw()
                drawPath = FingerPath(color,brushSize)
            }
            else -> return false
        }
        invalidate() // Refreshing the layout to reflect the drawing changes
        return true
    } // Next , we are going to create a New Variable called 'paths' above [Because when we lift the finger drawing got disappeared Or Erased.]

    // 3. override the function ::
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Safety check for invalid dimensions
        if (w <= 0 || h <= 0) return

        // Recycles old bitmap if view resizes (prevents memory leaks)
        if (::canvasBitmap.isInitialized && !canvasBitmap.isRecycled) {
            canvasBitmap.recycle()
        }

        // Safely create new mutable bitmap and attach it to off-screen canvas
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)

    }

    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)

        for (path in paths){ // 5. ** By adding this when we pick up our finger from screen The Drawing not Erased automatically because we provide qwn paths ::
            drawPaint.strokeWidth = path.brushThickness
            drawPaint.color = path.color
            canvas.drawPath(path,drawPaint)
        } //*** 6. Next , We are Going to change Brush Size ***: : Lets Create a New function 'changeBrushSize()' below


        if(!drawPath.isEmpty){
            drawPaint.strokeWidth = drawPath.brushThickness
            drawPaint.color = drawPath.color
            canvas.drawPath(drawPath,drawPaint) // drawing path on canvas ***
        }
    }
    private fun setUpDrawing (){ // 2. Creating a setUpDrawing() to setup the initial parameters for drawing Screen.

        drawPaint = Paint() // First , drawPaint object is instance of paint class and , Used to  define style of drawing.

        drawPath = FingerPath(color,brushSize) // Second, drawPath is instance of FingerPath(), USed to store current path being drawn by User.

        drawPaint.color = color // Third ,

        drawPaint.style = Paint.Style.STROKE // Here we set style of paint which is set to paint a style stroke So, part being drawn is only line and not field shape.

        drawPaint.strokeJoin = Paint.Join.ROUND // Join style is set to paint that jointed round So,that corner of Pot that are being drawn around it.

        drawPaint.strokeCap = Paint.Cap.ROUND //

        canvasPaint = Paint(Paint.DITHER_FLAG) // It is used to define style of canvas. The paint dether_flag is set that canvas use datering to smooth colors being drawn.

        brushSize = 20.toFloat()
        this.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // 3.  Next, we going to ' override the onSizeChange function '
    }

    // 6. Adding a Brush Size to canvas ::
    fun changeBrushSize(newSize:Float){
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
         // TypedValue.applyDimension() is used to convert value type into pixel value

        drawPaint.strokeWidth = brushSize  // Go to activity_main.xml and give id to DrawingView And Then Go to MainActivity.kt
    }

    // 12. Change or adding the different color in brush ******** //
    fun setColor(newColor: Any) {
        if (newColor is String) {
            color = Color.parseColor(newColor)
            drawPaint.color = color
        }// 13. Go to MainActivity.kt to implement View.onClickListener in MainActivity Class
        else{
            color = newColor as Int
            drawPaint.color = color
        }
    }
    fun setUndo(){
        if (paths.size > 0){
            paths.removeAt(paths.size-1)
            invalidate() // refreshing the layout to reflect the drawing changes
        } // Go to MainActivity.kt to create variable , setOnCLickListener for Undo
    }

    internal inner class FingerPath(var color:Int, var brushThickness:Float): Path()

}