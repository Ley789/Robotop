package com.example.alexander.robotop.visualOrientation;
import java.util.LinkedList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Alexander on 28/04/2015.
 */
public class Homography {
    private Mat homography = null;
    private static Homography instance = null;

    private Homography(){}

    public static synchronized Homography getInstance(){
        if(instance == null){
            instance = new Homography();
        }
        return instance;
    }

    public void setHomographyMatrix(Mat mRgba){
        final Size mPatternSize = new Size(6, 9); // number of inner corners in the used chessboard pattern
        float x = -48.0f; // coordinates of first detected inner corner on chessboard
        float y = 309.0f;
        float delta = 12.0f; // size of a single square edge in chessboard
        LinkedList<Point> PointList = new LinkedList<Point>();

        // Define real-world coordinates for given chessboard pattern:
        for (int i = 0; i < mPatternSize.height; i++) {
            y = 309.0f;
            for (int j = 0; j < mPatternSize.width; j++) {
                PointList.addLast(new Point(x,y));
                y += delta;
            }
            x += delta;
        }
        MatOfPoint2f RealWorldC = new MatOfPoint2f();
        RealWorldC.fromList(PointList);

        // Detect inner corners of chessboard pattern from image:
        Mat gray = new Mat();
        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY); // convert image to grayscale
        MatOfPoint2f mCorners = new MatOfPoint2f();
        boolean mPatternWasFound = Calib3d.findChessboardCorners(gray, mPatternSize, mCorners);

        // Calculate homography:
        if (mPatternWasFound){
            // Calib3d.drawChessboardCorners(mRgba, mPatternSize, mCorners, mPatternWasFound); //for visualization
            homography =  Calib3d.findHomography(mCorners, RealWorldC);
        }else{
            homography =  new Mat();
        }
    }

    public Point getPosition(Point cameraSpace){
        Mat src =  new Mat(1, 1, CvType.CV_32FC2);
        Mat dest = new Mat(1, 1, CvType.CV_32FC2);
        src.put(0, 0, new double[] { cameraSpace.x, cameraSpace.y }); // ps is a point in image coordinates
        Core.perspectiveTransform(src, dest, homography); //homography is your homography matrix
        //we get points in millimeter, to convert them to cm we multiply by 10
        return new Point(dest.get(0, 0)[0] * 10 , dest.get(0, 0)[1] *10);
    }

}
