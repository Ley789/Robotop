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
import com.example.alexander.robotop.robotData.RobotOdometry;
import com.example.alexander.robotop.visualOrientation.DetectRedBlobs;
import com.example.alexander.robotop.visualOrientation.Homography;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.concurrent.ExecutionException;

/**
 * Created by uli on 05.05.15.
 */
public class SearchActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private RobotOdometry odometry = RobotOdometry.getInstance();
    private Homography homography;
    private Executer<Mat> exe = new Executer<Mat>();
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("Callback", "OpenCV loaded successfully");
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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        odometry.setOdometry(10,0,0);
        setContentView(R.layout.activity_search);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface_view);
        mOpenCvCameraView.setMaxFrameSize(1920, 1080);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        homography = Homography.getInstance();


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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("Search", "called onCreateOptionsMenu");

        return true;
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i("Search", "called onOptionsItemSelected; selected item: " + item);

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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();


        Mat redCircles = null;
        Mat blueCircles = null;
        Mat result = null;
        //       exe.execute(new DetectRedBlobs(mRgba));
        exe.execute(new DetectRedBlobs(mRgba));
        try {
            //        redCircles = exe.getResult();
            blueCircles = exe.getResult();
        } catch (ExecutionException e) {
        } catch (Exception e) {
        }
        int row = 0;
        int elements = 0;
        if (redCircles != null) {
            result = redCircles;

        } else if (blueCircles != null) {
            result = blueCircles;

        }
        if(result!=null) {
            row = result.rows();
            elements = (int) result.elemSize();
            //we only care for the first occurence
            float[] data = new float[row * elements / 4];
            result.get(0, 0, data);
            //test the turns
            Point p = homography.getPosition(new Point(data[0], data[1])); //CHECK X AND Y;
            com.example.alexander.robotop.datastruct.Point wc = homography.toWorldCoordinates(new com.example.alexander.robotop.datastruct.Point((int)p.y,(int)p.x),odometry.getAngle(), odometry.getPoint());
            Log.d("BallPos: ", p.x + "  " + p.y);
            Log.d("WorldCoords: ", wc.getX() + "  " + wc.getY());
        }


        return mRgba;
    }


}
