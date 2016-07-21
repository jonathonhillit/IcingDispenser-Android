package nz.ac.aut.engineering.icingprinter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.FileOutputStream;

public class Load extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);



        // Get the file name
        String filename = getIntent().getStringExtra("image");

        // Get the bitmap image
        Bitmap bitmap = BitmapFactory.decodeFile(filename.trim());

        try {
            // Save a copy of the image in our application
            String file = "src.png";
            FileOutputStream stream = this.openFileOutput(file, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            bitmap.recycle();

            // Intent to the image cropping / resizing screen
            // and pass the path to the bitmap we saved in our application
            Intent intent = new Intent(this, Draw.class);
            intent.putExtra("image", filename);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
