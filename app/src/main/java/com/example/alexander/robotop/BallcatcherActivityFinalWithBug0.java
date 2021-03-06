package com.example.alexander.robotop;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alexander.robotop.ThreadControll.Executer;
import com.example.alexander.robotop.bugAlgorithms.Bug0Alg;
import com.example.alexander.robotop.datastruct.BallColors;
import com.example.alexander.robotop.datastruct.Beacons;
import com.example.alexander.robotop.datastruct.ColorBound;
import com.example.alexander.robotop.datastruct.MassCenter;
import com.example.alexander.robotop.movement.BallSearcher;
import com.example.alexander.robotop.movement.RobotMovement;
import com.example.alexander.robotop.robotData.RobotOdometry;
import com.example.alexander.robotop.robotData.RobotTracker;
import com.example.alexander.robotop.visualOrientation.Beacon;
import com.example.alexander.robotop.visualOrientation.DetectBalls;
import com.example.alexander.robotop.visualOrientation.Homography;
import com.example.alexander.robotop.visualOrientation.Selflocalization;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.alexander.robotop.communication.Connection.comReadWrite;

/**
 * Created by Alexander on 28/04/2015.
 */
public class BallcatcherActivityFinalWithBug0 extends ActionBarActivity  implements CvCameraViewListener2 {

    private enum States {
        INIT, COLLECT, GO_HOME;
    }
    private Bug0Alg bug0 = new Bug0Alg();
    private final int LOOK_BEACON_MAX = 10;
    private int beaconCount = 0;
    private int halfWayLookCount = 0;
    private boolean lookForBeacons = true;
    private boolean halfWayLook = false;
    private boolean looking = false;
    private final int CNT_LOOK = 3;
    private final int APPROACH = 50;
    private int lookedOnce = 0;
    private int i = 0;
    int catched = 0;
    private final byte RAISE_BAR = (byte)130;
    private boolean start = false;
    private States state = States.INIT;
    private RobotMovement move = RobotMovement.getInstance();
    private BallSearcher searcher = new BallSearcher();
    private int aimX;
    private int aimY;
    private EditText editY;
    private EditText editX;
    private final int SAFETY_DIST=30;
    private EditText editAngle;
    private RobotOdometry odometry = RobotOdometry.getInstance();
    private LinkedList<Point> points = new LinkedList<>();
    private LinkedList<com.example.alexander.robotop.datastruct.Point> worldCoordinates = new LinkedList<>();
    private int degreesTurned = 0;
    private final int DEGREES_TO_TURN = 45;
    private static final String TAG = "Coord";

    private Executer<Mat> exe = new Executer<Mat>();
    private Homography homography;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private boolean locatedPosition = false;
    private MenuItem mItemSwitchCamera = null;
    private RobotTracker tracker;
    private Dialog dialog;
    private Button setXYBtn;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        odometry.setOdometry(0,0,0); //TODO delete
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_xy_input);
        editX = (EditText) dialog.findViewById(R.id.edit_x);
        editY = (EditText) dialog.findViewById(R.id.editY);
        setXYBtn = (Button) dialog.findViewById(R.id.btn_setXY);
        setXYBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aimX = Integer.parseInt(editX.getText().toString());
                aimY = Integer.parseInt(editX.getText().toString());
                dialog.dismiss();
                start = true;
            }
        });
        dialog.show();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_ballcatcher);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface_view);
        mOpenCvCameraView.setMaxFrameSize(1920, 1080);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        //mOpenCvCameraView.setMaxFrameSize(800, 600);


        mOpenCvCameraView.setCvCameraViewListener(this);

        homography = Homography.getInstance();

        tracker = new RobotTracker();
        new Thread(tracker).start();
        move.decreaseBar();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        tracker.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Native/Java camera");
        getMenuInflater().inflate(R.menu.menu_ballcatcher2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        int id = item.getItemId();
        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsJavaCamera = !mIsJavaCamera;

            if (mIsJavaCamera) {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
                toastMesage = "Java Camera";
            } else {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface_view);
                toastMesage = "Native Camera";
            }

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }
        else if(id == R.id.change_odo){
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_xy_input);
            editX = (EditText) dialog.findViewById(R.id.edit_x);
            editY = (EditText) dialog.findViewById(R.id.editY);
            editAngle = (EditText) dialog.findViewById(R.id.editAngle);
            setXYBtn = (Button) dialog.findViewById(R.id.btn_setXY);
            setXYBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aimX = Integer.parseInt(editX.getText().toString());
                    aimY = Integer.parseInt(editY.getText().toString());
                    int angle = Integer.parseInt(editAngle.getText().toString());
                    odometry.setOdometry(aimX,aimY,angle);
                    dialog.dismiss();
                    start = true;
                }
            });
            dialog.show();
        }

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    /*
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        Log.d("inside", "inside");
        if(start) {

            if (state == States.INIT) {
                lookForBalls(mRgba);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                move.robotTurn(DEGREES_TO_TURN);
                degreesTurned += DEGREES_TO_TURN;
                if (degreesTurned == 360) {
                    state = States.COLLECT;
                    for(com.example.alexander.robotop.datastruct.Point p : worldCoordinates){
                        Log.d("WORLD", p.toString());
                    }
                }
                Log.d("turned: ", degreesTurned + "");
            } else if (state == States.COLLECT) {
                com.example.alexander.robotop.datastruct.Point currentPoint = worldCoordinates.poll();
                move.moveBlind(currentPoint);
                bringToChamber();
                if (worldCoordinates.isEmpty()) {
                    state = States.GO_HOME;
                }
            } else if (state == States.GO_HOME) {
                move.moveBlind(new com.example.alexander.robotop.datastruct.Point(0, 0)); //TODO drive home
            }

        }
        return mRgba;

    }*/

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        Log.d("inside", "inside");
        if(start) {

            if(beaconCount<LOOK_BEACON_MAX){
                Log.d("Beacon", "looking for beacons");
                beaconCount++;
                if(lookForBeacons(mRgba)){
                    beaconCount =LOOK_BEACON_MAX+100;
                    looking=true;
                }

                if(beaconCount == LOOK_BEACON_MAX-1){
                    move.robotTurn(45);
                    beaconCount = 0;
                }
            }

            if (looking) {
                lookForBalls(mRgba);
                lookedOnce++;
                if(lookedOnce > CNT_LOOK) {//the robot looks 5 times before he goes on
                    lookedOnce = 0;
                    move.robotTurn(DEGREES_TO_TURN);
                    degreesTurned += DEGREES_TO_TURN;
                    if (degreesTurned == 360) {
                        looking = false;
                        if (worldCoordinates.isEmpty()) {
                            looking = true;
                        } else {
                            catchBall(getNearestBall());
                        }
                        degreesTurned = 0;
                    }
                    Log.d("turned: ", degreesTurned + "");
                }//end if(lookedOnce > CNT_LOOK)
            }//end if(looking)

            if(halfWayLook){
                halfWayLookCount++;
                Log.d("Halfway", "looking");
                lookForBalls(mRgba);
                if(halfWayLookCount > 2) {
                    if (worldCoordinates.isEmpty()) {
                        halfWayLookCount = 0;
                        halfWayLook = false;
                        looking = true;
                        Log.d("Halfway", "cant find anymore");

                    } else {
                        halfWayLookCount = 0;
                        Log.d("Halfway", "got it");
                        halfWayLook = false;
                        catchBall(getNearestBall());
                    }
                }//end if(halfWayLookCount > 2)
            }// end if(halfWayLook)



        }// end if(start)
        return mRgba;

    }

    public void bringToChamber(){
        comReadWrite(new byte[]{'o',(byte) 0, '\r', '\n'});
        move.moveBlind(new com.example.alexander.robotop.datastruct.Point(aimX, aimY));
        comReadWrite(new byte[]{'o', (byte) 255, '\r', '\n'});
        move.robotDrive(-20);
    }

    public void bringAllToChamber(){
        bug0.bug0d(new com.example.alexander.robotop.datastruct.Point(aimX, aimY));
        comReadWrite(new byte[]{'o', (byte) 255, '\r', '\n'});
        move.robotDrive(-SAFETY_DIST);
        move.decreaseBar();
        if(catched >= 3){
            goHome();
        }else{
            looking = true;
        }
    }

    public void goHome(){
                bug0.bug0d(new com.example.alexander.robotop.datastruct.Point(0, 0)); //TODO drive home
    }

    public boolean lookForBeacons(Mat mRgba){
        boolean success = false;
        List<Beacon> seenBeacons = new ArrayList<>();
        Beacons beacons = Beacons.getInstance();
        MassCenter.getInstance().calculateMassCenter(mRgba);

        for(int i =0; i< beacons.size(); i++) {
            Beacon b = beacons.getBeacon(i);
            b.searchBeaconPoint();
            org.opencv.core.Point p = b.getRelativeCoordinate();
            if(p != null){
                seenBeacons.add(b);
            }

        }
        if(seenBeacons.size() >= 2) {
            Beacon[] bs = selectBeacons(seenBeacons);
            if(bs != null) {
                updateOdoWithBeacons(bs[0], bs[1]);
                looking = true;
                success = true;
            }
        }

        seenBeacons.clear();
        return success;
    }

    public Beacon[] selectBeacons(List<Beacon> beacons){
        Beacon [] selected = new Beacon[2];

        if(beacons.get(0).getId() == 0 && beacons.get(beacons.size()-1).getId() == 7){
            selected[0] = beacons.get(0);
            selected[1] = beacons.get(beacons.size()-1);
            return selected;
        }
        for(int i = 0; i < beacons.size()-1; i++){
            int id = beacons.get(i).getId();
            if(beacons.get(i+1).getId() == id+1){
                selected[0]= beacons.get(i);
                selected[1] = beacons.get(i+1);
                break;
            }

        }
        if(selected[0] == null || selected[1] == null){
            return null;
        }
        return selected;
    }

    public void lookForBalls(Mat mRgba){
        for(ColorBound c: BallColors.colors)
            exe.execute(new DetectBalls(mRgba, c));
        Mat result = null;
        for(int j = 0; j < BallColors.colors.size(); j++) {
            try {
                result = exe.getResult();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (result != null) {
                int row = result.rows();
                int elements = (int) result.elemSize();


                for (int i = 0; i < result.cols(); i++) {
                    float[] data = new float[row * elements / 4];
                    result.get(0, i, data);
                    Point p = homography.getPosition(new Point(data[0], data[1] + data[2]));
                    p.x = -p.x;
                    Core.circle(mRgba, p, 10,new Scalar(0,0,255));
                    points.add(p);
                    com.example.alexander.robotop.datastruct.Point wc = homography.toWorldCoordinates(new com.example.alexander.robotop.datastruct.Point((int) p.y, (int) p.x), odometry.getAngle(), odometry.getPoint());
                    if (!checkContain(wc)) {
                        worldCoordinates.add(wc);
                        Log.d("WorldCoord", wc.getX() + " " + wc.getY());
                        Log.d("Old coord", p.x + "  " + p.y);
                    } else {
                        Log.d("WorldCoord", "coord not added");
                    }

                }
            }
        }

            Log.d("Points:", "-----------------------");
            for(int i = 0; i < points.size(); i++){
                Log.d("Points: ", points.get(i).toString());
                //Log.d("WC: ", worldCoordinates.get(i).toString());

            }



        Log.d("NULL", "result is null");

    }

    private boolean checkContain(com.example.alexander.robotop.datastruct.Point wc) {
        double intervall =4;
        for(com.example.alexander.robotop.datastruct.Point p: worldCoordinates){
            if(Math.abs(wc.getX()-p.getX())<intervall&& Math.abs(wc.getY()-p.getY())<intervall){
                return true;
            }
        }
        return false;
    }

    com.example.alexander.robotop.datastruct.Point getNearestBall(){
        int min_dist = 99999;
        com.example.alexander.robotop.datastruct.Point nearest = null;
        for(com.example.alexander.robotop.datastruct.Point p : worldCoordinates){
            int dist = odometry.getPoint().distance(p);
            if(dist < min_dist){
                min_dist = dist;
                nearest = p;
            }
        }
        worldCoordinates.clear();
        return nearest;
    }

    public void catchBall(com.example.alexander.robotop.datastruct.Point point){
        if(odometry.getPoint().distance(point) > APPROACH){
            Log.d("HalfWay", "halfway");
            move.moveHalfWay(point);
            halfWayLook = true;
            return;
        }
        move.moveBlindWithSafety(point, SAFETY_DIST);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        move.raiseBar(RAISE_BAR);
        move.robotMoveBlindForward(SAFETY_DIST);

        move.decreaseBar();
        catched++;
        if((catched) % 3 == 0 || catched == 10){ //TODO make constant
            bringAllToChamber();
        }else{
            looking = true;
        }
    }

    private void updateOdoWithBeacons(Beacon a, Beacon b){
        Point egoP = homography.getPosition(a.getRelativeCoordinate());
        Point egoP2 = homography.getPosition(b.getRelativeCoordinate());

        com.example.alexander.robotop.datastruct.Point egoPoint = new com.example.alexander.robotop.datastruct.Point((int)egoP.x,(int)egoP.y);
        com.example.alexander.robotop.datastruct.Point egoPoint2 = new com.example.alexander.robotop.datastruct.Point((int)egoP2.x, (int)egoP2.y);
        com.example.alexander.robotop.datastruct.Point odoPoint = Selflocalization.selfLocalisation(egoPoint, egoPoint2, a.getWorldCoordinate(), b.getWorldCoordinate());
        double angle = Selflocalization.getOrientation(odoPoint, a.getWorldCoordinate(),egoPoint);

        odometry.setOdometry(odoPoint.getX(),odoPoint.getY(), (int)angle);
        Log.d("BeaconOdo", "x: " + odoPoint.getX() + " y: " + odoPoint.getY() + " angle: " + angle);


    }


}

