package nz.ac.aut.engineering.icingprinter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.UUID;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.widget.Toast;

/*
 *  Author:  Jony Hill <xyc8034@aut,ac,nz>
 *
 */

public class Draw extends AppCompatActivity implements View.OnClickListener {

    // Hold a reference to the custom view
    private DrawingView drawView;

    // Hold a reference to the current colour
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, printBtn, openBtn;

    // Store the dimension values for the brushes
    private float smallBrush, mediumBrush, largeBrush;

    private String path = "";
    private boolean isPositioning;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save this activities context view
        context = getApplicationContext();


        // Set the content view
        setContentView(R.layout.activity_draw);

        // Retreive reference to the view
        drawView = (DrawingView)findViewById(R.id.drawing);

        // Retreive reference to the draw button
        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        // Retreive reference to the erase button
        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        // Retreive reference to the new button
        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        // Retreive reference to the save button
        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        // Retreive reference to the open button
        openBtn = (ImageButton)findViewById(R.id.open_btn);
        openBtn.setOnClickListener(this);

        // Retreive reference to the print button
        printBtn = (ImageButton)findViewById(R.id.print_btn);
        printBtn.setOnClickListener(this);

        // Set default brush size
        drawView.setBrushSize(mediumBrush);

        // Set the default colour
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.brush_colour);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        // Initialise the brushes
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        isPositioning = false;
    }

    public void paintClicked(View view){
        // Turn erase off if it is set, and restore the last bush size
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());

        // Update chosen colour
        if(view != currPaint)
        {
            // Update colour
            ImageButton imgView = (ImageButton)view;
            String colour = view.getTag().toString();

            drawView.setColor(colour);

            // Set selected
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }

    }

    @Override
    public void onClick(View view){
        // Respond to user input
        if(view.getId()==R.id.draw_btn){
            // Draw button was clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");

            brushDialog.setContentView(R.layout.brush_chooser);

            // Construct the brush dialogue box
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            // Show the dialogue box
            brushDialog.show();
        }
        else if(view.getId()==R.id.erase_btn){
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            // Construct the dialogue box
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });

            // Show the dialogue box
            brushDialog.show();
        }
        else if(view.getId()==R.id.new_btn){
            // Double check to see if user intended to start new drawing
            // Construct dialogue box
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    // Call method to start new drawing
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    // Cancel clearing drawing
                    dialog.cancel();
                }
            });

            // Show dialogue box
            newDialog.show();
        }
        else if(view.getId()==R.id.save_btn){
            // Ensure user wanted to save image to gallery
            // Construct dialogue box
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);

                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");

                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }

                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });

            // Show Dialogue box
            saveDialog.show();
        }
        else if(view.getId() == R.id.open_btn)
        {
            // Start an intent for output that will allow the user to load an image form the camera
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);

        }
        else if(view.getId() == R.id.print_btn)
        {
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Print drawing");
            saveDialog.setMessage("Print drawing to Device?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    // We have to save the image to disk so the next activity can reload it from disk
                    // Enable the drawing cache
                    drawView.setDrawingCacheEnabled(true);

                    // Get the bitmap
                    Bitmap image = drawView.getDrawingCache();

                    String fileName = "img";

                    // Try to write the file to disk
                    try {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
                        fo.write(bytes.toByteArray());
                        // remember close file output
                        fo.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileName = null;
                    }

                    // Check for error
                    if(fileName == null)
                    {
                        // Exception Raised
                        // Create a toast message
                    }
                    else
                    {
                        // File saved intent to next activity
                        Intent intent = new Intent(context, Preview.class);

                        // Send the filename of the saved file
                        intent.putExtra("filename", fileName);

                        // Start Activity
                        startActivity(intent);
                    }

                    // Destroy the cache
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });

            // Show Dialogue box
            saveDialog.show();
            // The user wants to print the image
            // progress to the preview and printer
            // connection activity

            // Save the image data and add it in the intent
            // Intent to the drawing activity

            //Intent intent = new Intent(this, Draw.class);

            //intent.add(image data)

            //startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Call up to the super classes method first
        super.onActivityResult(requestCode, resultCode, data);


        // This is called when the gallery or camera intents back to our application
        // with the image data
        if(requestCode==1)
        {
            // User has selected an image from their gallery
            Uri photoUri = data.getData();
            if (photoUri != null)
            {
                {
                    Context context = getApplicationContext();
                    CharSequence text = "Loading Image";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                // Load image location
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                Log.v("Load Image", "Gallery File Path=====>>>" + filePath);


                // Get the bitmap image
                Bitmap bitmap = BitmapFactory.decodeFile(filePath.trim());

                try {
//                    // Save a copy of the image in our application
//                    String filename = "src.png";
//                    FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//                    //Cleanup
//                    stream.close();
//                    bitmap.recycle();
//
//                    // Intent to the image cropping / resizing screen
//                    // and pass the path to the bitmap we saved in our application
//                    Intent intent = new Intent(this, ImageLoad.class);
//                    intent.putExtra("image", filename);
//                    startActivity(intent);

                    // We now need to let the user select an area where to center the image that is being loaded
                    {
                        Context context = getApplicationContext();
                        CharSequence text = "Select Image Center";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }

                    // Set the position boolean
                    isPositioning = true;
                    drawView.setPosition(isPositioning);
                    drawView.setImageBitmap(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(requestCode==2)
        {
//            // User has selected to take a photo with the camera
//            Log.v("Load Image", "Camera File Path=====>>>"+path);
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            bitmap = Bitmap.createScaledBitmap(bitmap,500, 500, true);
//            //Drawable d=new BitmapDrawable(doDithering(bitmap));
//            Drawable d=new BitmapDrawable(bitmap);
//
//            //iv.setImageDrawable(d);


        }
    }
}
