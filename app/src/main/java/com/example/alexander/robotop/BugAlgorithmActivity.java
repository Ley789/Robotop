package com.example.alexander.robotop;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.alexander.robotop.bugAlgorithm.Bug0Alg;
import com.example.alexander.robotop.datastruct.Point;


public class BugAlgorithmActivity extends ActionBarActivity {
    private EditText xAchse;
    private EditText yAchse;
    private Point goal;
    private Bug0Alg bug;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_algorithm);
        xAchse = (EditText)findViewById(R.id.xAchse);
        yAchse = (EditText)findViewById(R.id.yAchse);
        bug = new Bug0Alg();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bug_algorithm, menu);
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

    public void onButtonBug0f(View v){
        readAndSetPoint();
        bug.forcedBug0(goal);
    }


    public void onButtonBug0(View v){
        readAndSetPoint();
        bug.bug0(goal);
    }
    public void readAndSetPoint(){
        int x = Integer.parseInt(xAchse.getText().toString());
        int y = Integer.parseInt(yAchse.getText().toString());
        goal = new Point(x,y);

    }
}
