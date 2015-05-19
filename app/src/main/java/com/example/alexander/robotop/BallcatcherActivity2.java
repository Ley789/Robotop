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
import com.example.alexander.robotop.movement.BallSearcher;
import com.example.alexander.robotop.movement.RobotMovement;
import com.example.alexander.robotop.robotData.RobotOdometry;
import com.example.alexander.robotop.robotData.RobotTracker;
import com.example.alexander.robotop.visualOrientation.DetectGreenBlobs;
import com.example.alexander.robotop.visualOrientation.Homography;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import static com.example.alexander.robotop.communication.Connection.comReadWrite;

/**
 * Created by Alexander on 28/04/2015.
 */
public class BallcatcherActivity2 extends ActionBarActivity  implements CvCameraViewListener2 {

    private enum States {
        INIT, COLLECT, GO_HOME;
    }
    private boolean start = true; //TODO make false
    private States state = States.INIT;
    private RobotMovement move = RobotMovement.getInstance();
    private BallSearcher searcher = new BallSearcher();
    private int aimX;
    private int aimY;
    private EditText editY;
    private EditText editX;
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
        //dialog.show();

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
            setXYBtn = (Button) dialog.findViewById(R.id.btn_setXY);
            setXYBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aimX = Integer.parseInt(editX.getText().toString());
                    aimY = Integer.parseInt(editX.getText().toString());
                    odometry.setOdometry(aimX,aimY,0);
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

        if(start) {

            if (state == States.INIT) {
                lookForBalls(mRgba);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //move.robotTurn(DEGREES_TO_TURN);
                degreesTurned += DEGREES_TO_TURN;
                if (degreesTurned == 360) {
                    state = States.COLLECT;
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

    }

    public void bringToChamber(){
        comReadWrite(new byte[]{'o',(byte) 0, '\r', '\n'});
        move.moveBlind(new com.example.alexander.robotop.datastruct.Point(aimX, aimY));
        comReadWrite(new byte[]{'o', (byte) 255, '\r', '\n'});
        move.robotDrive(-20);
    }

    public void lookForBalls(Mat mRgba){

        exe.execute(new DetectGreenBlobs(mRgba));
        Mat result = null;
        try {
            result = exe.getResult();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(result!=null) {
            int row = result.rows();
            int elements = (int) result.elemSize();


            for(int i=0; i<result.cols();i++){
                float[] data = new float[row * elements / 4];
                result.get(0,i,data);
                Point p = homography.getPosition(new Point(data[0], data[1]));
                points.add(p);
                com.example.alexander.robotop.datastruct.Point wc = homography.toWorldCoordinates(new com.example.alexander.robotop.datastruct.Point((int)p.y,(int)p.x),odometry.getAngle(), odometry.getPoint());
                worldCoordinates.add(wc);
            }

            Log.d("Points:", "-----------------------");
            for(int i = 0; i < points.size(); i++){
                Log.d("Points: ", points.get(i).toString());
                Log.d("WC: ", worldCoordinates.get(i).toString());

            }
        }

        Log.d("NULL", "result is null");

    }

}

