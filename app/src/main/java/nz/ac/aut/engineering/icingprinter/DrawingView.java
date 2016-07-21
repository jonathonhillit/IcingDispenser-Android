package nz.ac.aut.engineering.icingprinter;

// Android Imports
import android.graphics.Color;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

/**
 * Author:  Jony Hill <xyc8034@aut,ac,nz>
 *
 */
public class DrawingView extends View {
    // Path Drawing
    private Path drawingPath;

    // Drawing and canvas paint
    private Paint drawingPaint, canvasPaint;

    // Initial color
    private int paintColor = 0xFF660000;

    // Canvas object
    private Canvas drawCanvas;

    //canvas bitmap object
    private Bitmap canvasBitmap;

    // Brush sizes
    private float brushSize, lastBrushSize;

    // Erase condition
    private boolean erase = false;

    public boolean isPosition() {
        return position;
    }

    public void setPosition(boolean position) {
        this.position = position;
    }

    public void setImageBitmap(Bitmap image)
    {
        if(image != null) {
            imageBitmap = image;
        }
        else
        {
            throw new NullPointerException("Bitmap Image NULL");
        }
    }

    public Bitmap getImageBitmap()
    {
        return imageBitmap;
    }

    Bitmap imageBitmap = null;
    private boolean position = false;
    private boolean down = false;
    private float posx = 0.0f;
    private float posy = 0.0f;

    //
    // Member Methods
    //

    // Constructor Method
    public DrawingView(Context con, AttributeSet attr)
    {
        super(con, attr);

        // Initialise the drawing view
        initialiseDrawing();
    }

    // Initialisation function that sets up the custom view
    public void initialiseDrawing()
    {
        // Set default brush size
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        // Initialize the Drawing View
        drawingPath = new Path();
        drawingPaint = new Paint();

        // Set the initial colour
        drawingPaint.setColor(paintColor);

        // Set some properties on the drawingPaint object
        drawingPaint.setAntiAlias(true);
        drawingPaint.setStrokeWidth(brushSize);
        drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setStrokeJoin(Paint.Join.ROUND);
        drawingPaint.setStrokeCap(Paint.Cap.ROUND);

        // Instantate the canvas paint object
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    // Method to set the brush size
    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawingPaint.setStrokeWidth(brushSize);
    }

    // Method to set and get the last brush size
    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    // Method to set if we are erasing or not
    public void setErase(boolean isErase){
        erase=isErase;

        if(erase) drawingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawingPaint.setXfermode(null);
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    //
    // Overridden methods from superclass
    //
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // View sized has changed
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the custom view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawingPath, drawingPaint);

        if(imageBitmap != null && position && down)
        {
            float width = imageBitmap.getWidth();
            float height = imageBitmap.getHeight();
            canvas.drawBitmap(imageBitmap, posx - (width/2), posy - (height/2), drawingPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle user touch input
        float touchX = event.getX();
        float touchY = event.getY();



        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN && position && imageBitmap != null)
        {
            // The user is positioning the bitmap
            down = true;

            posx = touchX;
            posy = touchY;

        }
        else if(action == MotionEvent.ACTION_DOWN)
        {
            // Start the line path as the user presses their finger
            drawingPath.moveTo(touchX, touchY);

        }
        else if(action == MotionEvent.ACTION_MOVE && position && imageBitmap != null && down)
        {
            // Move the line as the user moves their finger
            posx = touchX;
            posy = touchY;


        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            // Move the line as the user moves their finger
            drawingPath.lineTo(touchX, touchY);

        }
        else if(action == MotionEvent.ACTION_UP && position)
        {
            // Make sure we don't trigger the other event when the user lifts finger
            float width = imageBitmap.getWidth();
            float height = imageBitmap.getHeight();
            drawCanvas.drawBitmap(imageBitmap, touchX - (width/2), touchY - (height/2), drawingPaint);
            imageBitmap.recycle();
            imageBitmap = null;
            position = false;
            down = false;
            posx = 0.0f;
            posy = 0.0f;

        }
        else if(action == MotionEvent.ACTION_UP)
        {
            // Draw the line when the user lifts their finger
            drawCanvas.drawPath(drawingPath, drawingPaint);
            drawingPath.reset();

        }
        else
        {
            // UNHANDLED INPUT
            return false;
        }

        // Invalidate the view and force it to redraw
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        // Set the current colour
        invalidate();

        paintColor = Color.parseColor(newColor);
        drawingPaint.setColor(paintColor);
    }
}
