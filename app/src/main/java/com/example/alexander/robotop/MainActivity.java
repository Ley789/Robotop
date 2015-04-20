package com.example.alexander.robotop;

import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.alexander.robotop.communication.Connection;

import jp.ksksue.driver.serial.FTDriver;

import static com.example.alexander.robotop.communication.Connection.com;


public class MainActivity extends ActionBarActivity {

    private TextView textLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
        textLog = (TextView) findViewById(R.id.log);
        connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Connection.comReadWrite(new byte[]{'u', (byte) 255, (byte) 128, '\r', '\n'});
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonCalibration(View view){
        Intent i = new Intent(this,CalibrationActivity.class);
        startActivity(i);
    }
    public void onButtonBug(View view){
        Intent i = new Intent(this,BugAlgorithmActivity.class);
        startActivity(i);
    }

    public void connect() {

        // TODO implement permission request
        if (com.begin(9600)) {
            textLog.append("connected\n");
        } else {
            textLog.append("could not connect\n");
        }
    }
}
