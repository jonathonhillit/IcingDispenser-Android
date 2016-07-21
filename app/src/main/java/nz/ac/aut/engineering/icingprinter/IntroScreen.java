package nz.ac.aut.engineering.icingprinter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * Author:  Jony Hill <xyc8034@aut,ac,nz>
 *
 */

public class IntroScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);
    }

    public void begin(View view)
    {
        // Intent to the drawing activity
        Intent intent = new Intent(this, Draw.class);
        startActivity(intent);
    }
}
