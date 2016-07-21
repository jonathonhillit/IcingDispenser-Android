package nz.ac.aut.engineering.icingprinter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Connect extends BlunoLibrary {

    TextView statusText;

    String btInputStream = "";

    Switch virtual;

    private TCPClient mTcpClient;

    // Hold a process dialogue so we can see the different stages of printing
    ProgressDialog progressDialog;


    // Hold some variables for our state and the printers state
    boolean startPrinting = false;
    boolean isPrinting = false;

    // We must get the cake printers image size
    // and now when it has been set.
    boolean isDimensionSet = false;
    int printerMaxX = 0;
    int printerMaxY = 0;

    // Hold references to our view objects
    TextView diagnosticsTextView;

    Button sendPrinter;
    Button cancelPrinting;
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // Initialise the bluno library creation
        onCreateProcess();

        // Set the Baudrate of the Bluetooth chip
        serialBegin(115200);

        virtual = (Switch)(findViewById(R.id.debug));
        virtual.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Start the TCP server
                    // connect to the server
                    new connectTask().execute("STATUS");
                } else {
                    // Stop the TCP server
                }
            }
        });

        statusText = (TextView)(findViewById(R.id.statusText));

        // Setup the listener for the scanning button
        Button buttonScan = (Button)(findViewById(R.id.scanButton));
        buttonScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buttonScanOnClickProcess();
            }
        });

        // Save references to the view objects we want to manipulate
        diagnosticsTextView = (TextView)(findViewById(R.id.diagnosticsView));

        Button diag = (Button)(findViewById(R.id.dianositcs));

        if (diag != null) {
            diag.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    createDiagnostics();
                }
            });
        }

        sendPrinter = (Button)(findViewById(R.id.sendPrinter));
        sendPrinter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startPrintJob();
            }
        });

        cancelPrinting = (Button)(findViewById(R.id.cancelPrinting));
        cancelPrinting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cancelPrintJob();
            }
        });

        // Finally load the image
        String filename = "source";

        FileInputStream fo = null;
        byte[] bytes = null;

        try {
            fo = openFileInput(filename);

        } catch (FileNotFoundException e) {
            // File did not exist or failed to create.  Display error message
            Context context = getApplicationContext();
            CharSequence text = "ERROR LOADING IMAGE";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Get the bitmap image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        imageBitmap = BitmapFactory.decodeStream(fo, null, options);


    }

    // Class which represents the job to be sent to the printer.
    // Includes the Bitmap and the DIM options to resize the bitmap
    private class Job{
        int dimx;
        int dimy;

        Bitmap bitmap;

    }

    // Private inner class that extends the AsyncTask class so we can avoid
    // processing on the view thread.
    private class PrintingJob extends AsyncTask<Job, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(Job... params) {
            // We need to resize the bitmap and convert it into the textual representation
            publishProgress("Resizing Image");
            Bitmap resized = Bitmap.createScaledBitmap(params[0].bitmap, params[0].dimx, params[0].dimy, true);

            // Save the rows of the image into an array list so we can send them in order
            ArrayList<String> imageStringArray = new ArrayList<>();

            publishProgress("Converting Image");
            // Loop over the image
            for(int i = 0; i < resized.getHeight(); ++i)
            {
                String currentRow = "";
                for(int j = 0; j < resized.getWidth(); ++j)
                {
                    int pixel = resized.getPixel(j, i);

                    if(pixel > 0xBBBBBBBB)
                    {
                        // Pixel is white
                        currentRow += "1";
                    }
                    else
                    {
                        // Pixel is black
                        currentRow += "0";
                    }
                }
            }

            publishProgress("Transmitting Data");
            // We inform the printer that we want to begin data transmission
            sendBluetooth("START\n");

            // Make sure the device has time to process it
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // We need to issue the commands for data transfer to the device
            for(int i = 0; i < imageStringArray.size(); ++i)
            {
                // Send the current row with its row number
                sendBluetooth("DATA " + i + " " + imageStringArray.get(i) + "\n");

                // Let the printer catch up
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // We inform the printer that we have finished data transfer and to begin printing
            sendBluetooth("END\n");

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result != null)
            {
                //drawingView.setImageBitmap(result);
               // drawingView.invalidate();
            }
            else
            {
                // An error has occured in the image algorithm
            }


            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(String... progress)
        {
            progressDialog.setMessage(progress[0]);
        }




    }

    private void startPrintJob() {
        // We first need to check that the printer is ready for a job and that we have
        // exchanged the DIM information
        if(isDimensionSet)
        {
            // Show an progress dialog while the job is sending
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Printing");
            progressDialog.setMessage("Beginning");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Job job = new Job();
            job.dimx = printerMaxX;
            job.dimy = printerMaxY;

            job.bitmap = imageBitmap;

            new PrintingJob().execute(job);

        }
        else {
            // Dimension information not set
            Context context = getApplicationContext();
            CharSequence text = "DIM not set";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }


    }

    private void cancelPrintJob() {
    }



    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoLibrary
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onStop() {
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                diagnosticsTextView.append("Connected\n");
                statusText.setText("Connecting");
                sendPrinter.setEnabled(true);
                cancelPrinting.setEnabled(true);

                // Because we just connected to a new device we need to get the current device state
                // and to request the DIM size of the printer
                sendBluetooth("STATUS\n");
                sendBluetooth("DIM\n");
                break;
            case isConnecting:
                diagnosticsTextView.append("Connecting\n");
                statusText.setText("Connecting");
                break;
            case isToScan:
                diagnosticsTextView.append("Scan\n");
                statusText.setText("Scan");
                break;
            case isScanning:
                diagnosticsTextView.append("Scanning\n");
                statusText.setText("Scanning");
                break;
            case isDisconnecting:
                diagnosticsTextView.append("isDisconnecting\n");
                statusText.setText("isDisconnecting");
                sendPrinter.setEnabled(false);
                cancelPrinting.setEnabled(false);
                break;
            default:
                break;
        }
    }

    public void createDiagnostics() {
        //
        final Dialog diagnosticsDialog = new Dialog(this);

        diagnosticsDialog.setTitle("Diagnostics Panel");

        diagnosticsDialog.setContentView(R.layout.diagnostics_panel);

        diagnosticsListener list = new diagnosticsListener();

        // Construct the brush dialogue box
        Button getStatus = (Button)diagnosticsDialog.findViewById(R.id.boardLEDOn);
        Button reset = (Button)diagnosticsDialog.findViewById(R.id.boardLEDOff);
        Button stepLeft = (Button)diagnosticsDialog.findViewById(R.id.xMotorRight);
        Button stepRight = (Button)diagnosticsDialog.findViewById(R.id.xMotorLeft);
        Button stepUp = (Button)diagnosticsDialog.findViewById(R.id.yMotorUp);
        Button stepDown = (Button)diagnosticsDialog.findViewById(R.id.yMotorDown);
        Button headDown = (Button)diagnosticsDialog.findViewById(R.id.icingPump);
        Button headUp = (Button)diagnosticsDialog.findViewById(R.id.yrgLEDOn);
        Button dim = (Button)diagnosticsDialog.findViewById(R.id.yrgLEDOff);

        Button initialPosition = (Button)diagnosticsDialog.findViewById(R.id.initialPosition);
        Button motordistance = (Button)diagnosticsDialog.findViewById(R.id.motorDistance);
        Button ultrasonic = (Button)diagnosticsDialog.findViewById(R.id.ultrasonic);
        Button printdata = (Button)diagnosticsDialog.findViewById(R.id.printData);

        // Set the onClick listeners for our buttons
        getStatus.setOnClickListener(list);
        reset.setOnClickListener(list);
        stepLeft.setOnClickListener(list);
        stepRight.setOnClickListener(list);
        stepUp.setOnClickListener(list);
        stepDown.setOnClickListener(list);
        headDown.setOnClickListener(list);
        headUp.setOnClickListener(list);
        dim.setOnClickListener(list);
        initialPosition.setOnClickListener(list);
        motordistance.setOnClickListener(list);
        ultrasonic.setOnClickListener(list);
        printdata.setOnClickListener(list);

        // Show the dialogue box
        diagnosticsDialog.show();
    }

    //
    // Private inner class that will handle the different diagnostics input from the user
    private class diagnosticsListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if(v.getId() == R.id.boardLEDOn)
            {
                sendBluetooth("a");
            }
            else if(v.getId() == R.id.boardLEDOff)
            {
                sendBluetooth("b");
            }
            else if(v.getId() == R.id.xMotorRight)
            {
                sendBluetooth("c");
            }
            else if(v.getId() == R.id.xMotorLeft)
            {
                sendBluetooth("d");
            }
            else if(v.getId() == R.id.yMotorUp)
            {
                sendBluetooth("e");
            }
            else if(v.getId() == R.id.yMotorDown)
            {
                sendBluetooth("f");
            }
            else if(v.getId() == R.id.icingPump)
            {
                sendBluetooth("g");
            }
            else if(v.getId() == R.id.yrgLEDOn)
            {
                sendBluetooth("h");
            }
            else if(v.getId() == R.id.yrgLEDOff)
            {
                sendBluetooth("i");
            }
            else if(v.getId() == R.id.initialPosition)
            {
                sendBluetooth("j");
            }
            else if(v.getId() == R.id.motorDistance)
            {
                sendBluetooth("k");
            }
            else if(v.getId() == R.id.ultrasonic)
            {
                sendBluetooth("l");
            }
            else if(v.getId() == R.id.printData)
            {
                sendBluetooth("z");
            }
        }
    }

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        btInputStream += theString;		// Append the incomming data onto the input stream string
        diagnosticsTextView.append("REPLY:"+theString);

        // Process the incomming bluetooth message
        String[] command = btInputStream.split("\n");

        String[] parts = command[0].split(" ");

        if(parts[0].equals("STATUS"))
        {
            // This is a status update
        }
        else if(parts[0].equals("DIM"))
        {
            // This is the device sending its dimensions
            int X = Integer.parseInt(parts[1]);
            int Y = Integer.parseInt(parts[2]);

            if(X != 0 && Y != 0)
            {
                isDimensionSet = true;
                printerMaxX = X;
                printerMaxY = Y;
            }
        }

    }

    public void sendBluetooth(String data) {
        // Send the data via bluetooth to the printer
        diagnosticsTextView.append("SENT:" + data);

        if(virtual.isChecked())
        {
            tcpSend(data);
        }
        else {
            serialSend(data);
        }
    }

    public void tcpSend(String data)
    {
        if(mTcpClient != null)
        {
            mTcpClient.sendMessage(data);
        }
    }

    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();
            publishProgress("Starting TCP Client");

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            onSerialReceived(values[0]);
        }
    }
}
