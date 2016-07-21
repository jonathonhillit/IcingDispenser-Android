package nz.ac.aut.engineering.icingprinter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Preview extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    // Member variables
    // Hold references to the views
    private ImageView drawingView;
    private Context context;

    private Button apply, print;
    private Spinner algorithmSpinner;

    ProgressDialog progress;

    // Hold a reference to the bitmap
    Bitmap bm = null;
    Bitmap oriBm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        context = getApplicationContext();
        drawingView = ((ImageView)(findViewById(R.id.preview)));

        apply = (Button)(findViewById(R.id.apply));
        print = (Button)(findViewById(R.id.print));
        algorithmSpinner = (Spinner)(findViewById(R.id.spinner));

        // Set the onClick listeners
        apply.setOnClickListener(this);
        print.setOnClickListener(this);



        // Get the filename of the image
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String filename = extras.getString("filename");

        FileInputStream fo = null;
        byte[] bytes = null;

        try {
            fo = openFileInput(filename);

        } catch (FileNotFoundException e) {
            // File did not exist or failed to create.  Display error message
            // TOAST TODO
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Get the bitmap image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        oriBm = BitmapFactory.decodeStream(fo, null, options);
        bm = oriBm.copy(Bitmap.Config.ARGB_8888, true);//BitmapFactory.decodeStream(fo, null, options);

        if(bm == null)
        {
            // Error loading preview image
            // Display toast and intent back to the drawing screen

            Context context = getApplicationContext();
            CharSequence text = "ERROR LOADING IMAGE";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        drawingView.setImageBitmap(bm);


    }

    private void initialiseView() {
        // Initialise the spinner object
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.algorithms, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }

    @Override
    public void onClick(View v) {
        if(v == apply)
        {
            // Set up the bitmap and files for the algorithm thread
            bm = oriBm.copy(Bitmap.Config.ARGB_8888, true);
            int id = algorithmSpinner.getSelectedItemPosition();
            boolean sharpen = ((CheckBox)(findViewById(R.id.sharpen))).isSelected();
            boolean blur = ((CheckBox)(findViewById(R.id.blur))).isSelected();
            boolean doublePass = ((CheckBox)(findViewById(R.id.doublePass))).isSelected();
            boolean refine = ((CheckBox)(findViewById(R.id.refine))).isSelected();

            // Show an progress dialog while the algorithm task is running
            progress = new ProgressDialog(this);
            progress.setTitle("Processing");
            progress.setMessage("Please wait while image is processed");
            progress.setCancelable(false);
            progress.show();

            // Make the options object for the thread
            AlgorithmOptions opts = new AlgorithmOptions(id, bm);

            // Start the thread
            new AlgorithmTask().execute(opts);

        }
        else if(v == print)
        {
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Print drawing");
            saveDialog.setMessage("Print drawing to Device?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // We have to save the image to disk so the next activity can reload it from disk
                    // Enable the drawing cache
                    drawingView.setDrawingCacheEnabled(true);

                    // Get the bitmap
                    Bitmap image = drawingView.getDrawingCache();

                    String fileName = "source";

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
                    if (fileName == null) {
                        // Exception Raised
                        // Create a toast message
                    } else {
                        // File saved intent to next activity
                        Intent intent = new Intent(context, Connect.class);

                        // Send the filename of the saved file
                        intent.putExtra("dithered", fileName);

                        // Start Activity
                        startActivity(intent);
                    }

                    // Destroy the cache
                    drawingView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });


            saveDialog.show();
        }

    }

    private static class AlgorithmOptions {
        public int algorithmID;
        public Bitmap bitmap;

        AlgorithmOptions(int algorithmID, Bitmap bitmap)
        {
            this.algorithmID = algorithmID;
            this.bitmap = bitmap;
        }
    }

    private class AlgorithmTask extends AsyncTask<AlgorithmOptions, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(AlgorithmOptions... params) {
            // Perform the algorithm based on options defined in the AlgorithmOption's object
            // Get the algorithm ID
            int id = params[0].algorithmID;

            // Get the source bitmap
            Bitmap bitmap = params[0].bitmap;

            // Hold a reference to the result
            Bitmap result = null;

            // Peform the dithering algorithm
            switch(id)
            {
                case 0:
                {
                    // Floyd Steinberg
                    result = ImageLib.fsDither(bitmap);

                }
                break;
                case 1:
                {
                    // Jarvis-Judice-Ninke
                    result = ImageLib.jjnDither(bitmap);

                }
                break;
                case 2:
                {
                    // Atkinson
                    result = ImageLib.atkDither(bitmap);

                }
                break;
                case 3:
                {
                    // Sierra
                    result = ImageLib.sDither(bitmap);

                }
                break;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result != null)
            {
                drawingView.setImageBitmap(result);
                drawingView.invalidate();
            }
            else
            {
                // An error has occured in the image algorithm
            }


            progress.dismiss();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
