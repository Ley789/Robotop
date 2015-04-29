package com.example.alexander.robotop;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.alexander.robotop.ThreadControll.Executer;
import com.example.alexander.robotop.bugAlgorithms.Bug0Alg;
import com.example.alexander.robotop.movement.BallSearcher;
import com.example.alexander.robotop.movement.RobotMovement;
import com.example.alexander.robotop.robotData.RobotTracker;
import com.example.alexander.robotop.visualOrientation.DetectGreenBlobs;
import com.example.alexander.robotop.visualOrientation.DetectRedBlobs;
import com.example.alexander.robotop.visualOrientation.Homography;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alexander on 28/04/2015.
 */
public class BallcatcherActivity extends ActionBarActivity  implements CvCameraViewListener2 {

    //Variables for plan delta
    private boolean catchBall =false;
    private RobotMovement move = RobotMovement.getInstance();
    private int length = 20;
    private int degree = 5;
    private BallSearcher searcher = new BallSearcher();
    private int counter = 0;
    private int counter2 = 0;
    private Bug0Alg bug = new Bug0Alg();
    //done

    private static final String TAG = "Coord";

    private Executer<Mat> exe = new Executer<Mat>();
    private Homography homography;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private boolean locatedPosition = false;
    private MenuItem mItemSwitchCamera = null;
    RobotTracker tracker;

    private com.example.alexander.robotop.datastruct.Point toPoint;


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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_ballcatcher);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface_view);
        mOpenCvCameraView.setMaxFrameSize(1920, 1080);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

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

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();

        if (!locatedPosition) {
            Mat redCircles = null;
            Mat greenCircles = null;
            Mat result = null;
            exe.execute(new DetectRedBlobs(mRgba));
            exe.execute(new DetectGreenBlobs(mRgba));
            try {
                redCircles = exe.getResult();
                greenCircles = exe.getResult();
            } catch (ExecutionException e) {
            } catch (Exception e) {
            }
            int row = 0;
            int elements = 0;
            if(redCircles != null) {
                 result = redCircles;
            } else if (greenCircles != null) {
                result = greenCircles;
            }else {
                counter++;
                if(counter > 3){
                    counter = 0;
                    move.robotTurn(45);
                    counter2 +=45;
                    if(counter2>360){
                        counter2=0;
                        lookForBall();
                    }
                }
                return mRgba;
            }
            row = result.rows();
            elements = (int) result.elemSize();
            //we only care for the first occurence
            float[] data = new float[row * elements/4];
            result.get(0,0,data);
            //test the turns
            if(data[1] < 280){
                move.robotTurn(degree);
            }else if(data[1] > 480) {
                move.robotTurn(-degree);
            }
            planDelta(data[0]);
        }
        return mRgba;
    }
    //call planDelata after u entered the aim point
    public void planDelta(float a){
        if(a > 650){
            catchBall = true;
            locatedPosition = false;
        }else{
            move.robotDrive(10);

        }
    }

    public void lookForBall() {
        int min = -100 ;
        int max = 100;
        Random rand = new Random();
        int randomX= rand.nextInt((max - min) + 1) + min;
        int randomY= rand.nextInt((max - min) + 1) + min;
        bug.forcedBug0(new com.example.alexander.robotop.datastruct.Point(randomX, randomY));
    }



}
