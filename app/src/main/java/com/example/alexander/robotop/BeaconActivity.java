package com.example.alexander.robotop;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.alexander.robotop.datastruct.BallColors;
import com.example.alexander.robotop.datastruct.Beacons;
import com.example.alexander.robotop.datastruct.ColorBound;
import com.example.alexander.robotop.datastruct.MassCenter;
import com.example.alexander.robotop.modell.Detector;
import com.example.alexander.robotop.visualOrientation.Beacon;
import com.example.alexander.robotop.visualOrientation.Homography;
import com.example.alexander.robotop.visualOrientation.Selflocalization;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander on 11/05/2015.
 */
public class BeaconActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2,View.OnTouchListener {
    //var to setup beacons
    private MassCenter massCenter = MassCenter.getInstance();
    private ColorBound tmpColor = new ColorBound();
    private Beacons beacons = Beacons.getInstance();
    private List<Beacon> seenBeacons = new ArrayList<>();
    private boolean showTouchedColor = false;
    private Homography homography = Homography.getInstance();
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private Tutorial3View mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private MenuItem mItemSaveColor = null;
    private MenuItem mItemSaveBallColor = null;
    private MenuItem mItemResetColor = null;
    private MenuItem mItemToggleVision = null;
    private static String TAG ="Test Beacon";


    //Testing beacon fucntion
    private static double mMinContourArea = 0.1;

    private List<ColorBound> bounds = new ArrayList<>();

    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    //done
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(BeaconActivity.this);
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

        setContentView(R.layout.activity_beacon);



        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
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
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("flash");
        mItemSaveColor = menu.add("Save selected color");
        mItemResetColor = menu.add("Reset selected color");
        mItemToggleVision = menu.add("Toggle vision");
        mItemSaveBallColor = menu.add("add ball color");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.flash();
        }else if(item == mItemSaveColor) {
            saveColor();
        }else if(item == mItemResetColor){
            resetColorList();
        }else if(item == mItemToggleVision){
            toggleVision();
        }else if(item == mItemSaveBallColor){
            saveBallColor();
            Log.d("saved", "ballcolor");
        }

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat mHsv = new Mat();
        if(showTouchedColor && tmpColor != null) {
            Mat nRgba = new Mat();
            Imgproc.cvtColor(mRgba,mHsv, Imgproc.COLOR_RGB2HSV_FULL);
//            Core.inRange(mHsv,tmpColor.getmLowerBound(), tmpColor.getmUpperBound(),nRgba);
            nRgba = Detector.detectBlob(mHsv,tmpColor);
            //TODO delete following methods coz they are written to test beacon methods
            MassCenter.getInstance().calculateMassCenter(mRgba);
            for(int i = 0; i < beacons.size(); i++) {
                Beacon b = beacons.getBeacon(i);

                b.searchBeaconPoint();
                org.opencv.core.Point p = b.getRelativeCoordinate();

                if(MassCenter.getInstance().getMassRect() != null && MassCenter.getInstance().getMassRect().size() >0 && MassCenter.getInstance().getMassRect().get(0) != null)
                Log.d(TAG, "number first color Rects "+ MassCenter.getInstance().getMassRect().get(0).size());
                if (p != null) {
                    Core.circle(nRgba, p, 10, new Scalar(100, 100, 100), 8);
                    seenBeacons.add(b);
                    log(p,b);
                }
            }
            if(seenBeacons.size() >= 2) {
                Beacon[] bs = selectBeacons(seenBeacons);
                if(bs != null) {
                    updateOdoWithBeacons(bs[0], bs[1]);
                }
            }
            seenBeacons.clear();
            return nRgba;
        }


        MassCenter.getInstance().calculateMassCenter(mRgba);
        for(int i = 0; i < beacons.size(); i++) {
            Beacon b = beacons.getBeacon(i);

            b.searchBeaconPoint();
            org.opencv.core.Point p = b.getRelativeCoordinate();

            if(MassCenter.getInstance().getMassRect() != null && MassCenter.getInstance().getMassRect().size() >0 && MassCenter.getInstance().getMassRect().get(0) != null)
                Log.d(TAG, "number first color Rects "+ MassCenter.getInstance().getMassRect().get(0).size());
            if (p != null) {
                Core.circle(mRgba, p, 10, new Scalar(100, 100, 100), 8);
                seenBeacons.add(b);
                log(p,b);
            }
        }
        if(seenBeacons.size() >= 2) {
            Beacon[] bs = selectBeacons(seenBeacons);
            if(bs != null) {
                updateOdoWithBeacons(bs[0], bs[1]);
            }
        }
        seenBeacons.clear();
        return mRgba;
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

    public void log(Point p, Beacon b){
        Log.d(TAG, "My id is " + b.getId() + " point " + p.toString() + " and my sec color id " + b.centerIndex.second);
        Point a = homography.getPosition(p);
        int x = (int)a.y;
        int y = (int)-a.x;
        Log.d(TAG, "x: " + x + " y: +" + y);


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "called onTouch");

        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;
        int squareSize = 4;
        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>squareSize) ? x-squareSize : 0;
        touchedRect.y = (y>squareSize) ? y-squareSize : 0;

        touchedRect.width = (x+squareSize < cols) ? x + squareSize - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+squareSize < rows) ? y + squareSize - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;


        tmpColor.setHsvColor(mBlobColorHsv);
        Log.d("COLOR", tmpColor.toString());
        touchedRegionRgba.release();
        touchedRegionHsv.release();
        return false; // don't need subsequent touch events
    }



    private void resetColorList(){
        massCenter.colorList.clear();
    }
    private void saveColor(){
        if(tmpColor != null){
            massCenter.colorList.add(new ColorBound(tmpColor));
        }
    }
    private void toggleVision(){
        showTouchedColor = !showTouchedColor;
    }

    private void saveBallColor(){
        BallColors.colors.add(new ColorBound(tmpColor));
    }

    private void updateOdoWithBeacons(Beacon a, Beacon b){
        Point egoP = homography.getPosition(a.getRelativeCoordinate());
        egoP = new Point(egoP.y, -egoP.x);
        Point egoP2 = homography.getPosition(b.getRelativeCoordinate());
        egoP2 = new Point(egoP2.y, -egoP2.x);

        com.example.alexander.robotop.datastruct.Point egoPoint = new com.example.alexander.robotop.datastruct.Point((int)egoP.x,(int)egoP.y);
        com.example.alexander.robotop.datastruct.Point egoPoint2 = new com.example.alexander.robotop.datastruct.Point((int)egoP2.x, (int)egoP2.y);
        com.example.alexander.robotop.datastruct.Point odoPoint = Selflocalization.selfLocalisation(egoPoint, egoPoint2, a.getWorldCoordinate(), b.getWorldCoordinate());
        double angle = Selflocalization.getOrientation(odoPoint, a.getWorldCoordinate(),egoPoint);
        Log.d("BeaconOdo", "x: " + odoPoint.getX() + " y: " + odoPoint.getY() + " angle: " + angle);


    }


}
