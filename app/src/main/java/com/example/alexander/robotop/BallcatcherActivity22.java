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

import com.example.alexander.robotop.ThreadControll.Executer;
import com.example.alexander.robotop.datastruct.BallColors;
import com.example.alexander.robotop.datastruct.ColorBound;
import com.example.alexander.robotop.movement.BallSearcher;
import com.example.alexander.robotop.movement.RobotMovement;
import com.example.alexander.robotop.robotData.RobotOdometry;
import com.example.alexander.robotop.robotData.RobotTracker;
import com.example.alexander.robotop.visualOrientation.DetectBalls;
import com.example.alexander.robotop.visualOrientation.DetectBallsGray;
import com.example.alexander.robotop.visualOrientation.Homography;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alexander on 28/04/2015.
 */
public class BallcatcherActivity22 extends ActionBarActivity  implements CvCameraViewListener2 {

    private enum States {
        INIT, COLLECT, GO_HOME;
    }
    boolean looking = true;
    int catched = 0;
    private final byte RAISE_BAR = (byte)150;
    private boolean start = true; //TODO make false
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
    private final int DEGREES_TO_TURN = 90;
    private static final String TAG = "Coord";

    private Executer<Mat> exe = new Executer<Mat>();
    private Homography homography;
    private Tutorial3View mOpenCvCameraView;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_ballcatcher);


        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.java_surface_view);




        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(800, 600);



        mOpenCvCameraView.setCvCameraViewListener(this);


        homography = Homography.getInstance();

        tracker = new RobotTracker();
        new Thread(tracker).start();


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
            mOpenCvCameraView.flash();
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


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        Log.d("inside", "inside");
        if(start) {

            if (looking) {
                lookForBalls(mRgba);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               // move.robotTurn(DEGREES_TO_TURN);
                //egreesTurned += DEGREES_TO_TURN;
                            }
        }
        return mRgba;

    }








    public void lookForBalls(Mat mRgba){
        Log.d("colors", BallColors.colors.size() + "");
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
                    Core.circle(mRgba, new Point(data[0], data[1]), (int)data[2],new Scalar(0,0,255));
                    points.add(p);
                    com.example.alexander.robotop.datastruct.Point wc = homography.toWorldCoordinates(new com.example.alexander.robotop.datastruct.Point((int) p.y, (int) p.x), odometry.getAngle(), odometry.getPoint());
                    if (!checkContain(wc)) {
                        worldCoordinates.add(wc);
                        Log.d("WorldCoord", wc.getX() + " " + wc.getY());
                        Log.d("WorldCoord", "Relative points: " + p.x + " " + p.y);
                    } else {
                        Log.d("WorldCoord", "coord not added " + wc.getX() + " " + wc.getY());
                    }

                }
            }else{
                Log.d("NULL", "result is null");
            }
        }

            Log.d("Points:", "-----------------------");
            for(int i = 0; i < points.size(); i++){
                Log.d("Points: ", points.get(i).toString());
                //Log.d("WC: ", worldCoordinates.get(i).toString());

            }




    }


    public void lookForBallsGray(Mat mRgba){
        exe.execute(new DetectBallsGray(mRgba));
        Mat result = null;
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
                    Core.circle(mRgba, new Point(data[0], data[1]), (int)data[2],new Scalar(0,0,255));
                    points.add(p);
                    com.example.alexander.robotop.datastruct.Point wc = homography.toWorldCoordinates(new com.example.alexander.robotop.datastruct.Point((int) p.y, (int) p.x), odometry.getAngle(), odometry.getPoint());
                    if (!checkContain(wc)) {
                        worldCoordinates.add(wc);
                        Log.d("WorldCoord", wc.getX() + " " + wc.getY());
                        Log.d("WorldCoord", p.x + " " + p.y);
                    } else {
                        Log.d("WorldCoord", "coord not added " + wc.getX() + " " + wc.getY());
                    }

                }
            }else{
                Log.d("NULL", "result is null");
            }


        Log.d("Points:", "-----------------------");
        for(int i = 0; i < points.size(); i++){
            Log.d("Points: ", points.get(i).toString());
            //Log.d("WC: ", worldCoordinates.get(i).toString());

        }




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

}

