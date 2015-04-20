package com.example.alexander.robotop;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.alexander.robotop.movement.RobotMovement;


public class CalibrationActivity extends ActionBarActivity {

    private EditText editAngle;
    private EditText editDistance;
    private RobotMovement movement = RobotMovement.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        editAngle =(EditText) findViewById(R.id.editAngle);
        editDistance = (EditText) findViewById(R.id.editDistance);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calibration, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonRotate(View v){
        int degree = Integer.parseInt(editAngle.getText().toString());
        movement.robotTurn(degree);
    }

    public void onButtonDrive(View v){
        int distance = Integer.parseInt(editDistance.getText().toString());
        movement.robotDrive(distance);
    }
}
