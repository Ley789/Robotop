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
import com.example.alexander.robotop.visualOrientation.DetectGreenBlobs;
import com.example.alexander.robotop.visualOrientation.DetectRedBlobs;
import com.example.alexander.robotop.visualOrientation.Homography;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alexander on 28/04/2015.
 */
public class BallcatcherActivity extends ActionBarActivity  implements CvCameraViewListener2 {

    private static final String TAG = "Coord";

    private Executer<Mat> exe = new Executer<Mat>();
    private Homography homography;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private boolean locatedPosition = false;
    private MenuItem mItemSwitchCamera = null;
    private Bug0Alg bug;
    private List<MatOfPoint> pts = new ArrayList<>();

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

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        homography = Homography.getInstance();
        bug = new Bug0Alg();
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

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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
        if (!locatedPosition) {
            Mat mRgba = inputFrame.rgba();
            Mat redCircles = new Mat();
            Mat greenCircles = new Mat();
            exe.execute(new DetectRedBlobs(mRgba));
            exe.execute(new DetectGreenBlobs(mRgba));
            try {
                redCircles = exe.getResult();
                greenCircles = exe.getResult();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(redCircles != null){
                //we only care for the first occurence
                long elements = redCircles.elemSize();
                float[] data;

            //    homography.getPosition(new Point(redCircles.get(0,0),redCircles.get(0,1)));
            }else if(greenCircles != null){

            }
        }
        Mat mRgba = inputFrame.rgba();
        Mat newMat = new Mat(mRgba.rows(), mRgba.cols(), mRgba.type());
        pts.add(new MatOfPoint(new Point(1,2)));
        pts.add(new MatOfPoint(new Point(200,500)));
        Core.polylines(newMat, pts, false, new Scalar(255, 0, 0));


        return newMat;
    }
}
