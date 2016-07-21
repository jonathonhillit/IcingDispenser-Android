package nz.ac.aut.engineering.icingprinter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;

public class ImageLoad extends AppCompatActivity implements View.OnClickListener, ScaleGestureDetector.OnScaleGestureListener{

    private Matrix matrix = new Matrix();
    private ImageButton delete, add;
    private float scale = 1f;



    // Hold references to the image view and to our bitmap
    ImageView iv = null;
    private Bitmap current = null;

    /** The custom gesture detector we use to track scaling. */
    private ScaleGestureDetector mScaleDetector;

    /** The scale value of our internal image view. */
    private float mScaleValue = 1.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_load);

        // Save references to the delete and add buttons
        delete = (ImageButton)findViewById(R.id.del_btn);
        add = (ImageButton)findViewById(R.id.add_btn);

        // Set the on clicl listeners of these buttons to this object
        delete.setOnClickListener(this);
        add.setOnClickListener(this);


        iv = ((ImageView)(findViewById(R.id.image)));


        // Add a scale GestureDetector, with this as the listener.
        mScaleDetector = new ScaleGestureDetector(getApplicationContext(), this);




        // Prepare the image that was passed to us by the calling function
        current = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            current = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Drawable d=new BitmapDrawable(current);
        iv.setImageDrawable(d);

        // Set the scale type to MATRIX so that the scaling works.
        iv.setScaleType(ImageView.ScaleType.MATRIX);

       // ((ImageView)(findViewById(R.id.image))).setOnClickListener(new ScaleListener());
    }

    @Override
    public void onClick(View view) {
        // Handle any button presses

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean handled = mScaleDetector.onTouchEvent(event);

        if(!handled)
        {
            handled = super.onTouchEvent(event);
        }

        return handled;
    }

    @Override public boolean onScale(ScaleGestureDetector detector) {
        // Get the modified scale value
        mScaleValue *= detector.getScaleFactor();



        // Set the image matrix scale
        Matrix m = new Matrix(iv.getImageMatrix());
        m.setScale(mScaleValue, mScaleValue);
        iv.setImageMatrix(m);

        ((TextView)(findViewById(R.id.status))).setText("" + mScaleValue);

        ((View)(findViewById(R.id.imageviewView))).postInvalidate();

        return true;
    }

    @Override public boolean onScaleBegin(ScaleGestureDetector detector) {
        // Return true here to tell the ScaleGestureDetector we
        // are in a scale and want to continue tracking.
        return true;
    }

    @Override public void onScaleEnd(ScaleGestureDetector detector) {
        // We don't care about end events, but you could handle this if
        // you wanted to write finished values or interact with the user
        // when they are finished.
    }


}
