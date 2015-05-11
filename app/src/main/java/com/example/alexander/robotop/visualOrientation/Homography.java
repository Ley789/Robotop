package com.example.alexander.robotop.visualOrientation;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

/**
 * Created by Alexander on 28/04/2015.
 */
public class Homography {
    private Mat homography = null;
    private static Homography instance = null;

    private Homography(){

    }

    public static synchronized Homography getInstance(){
        if(instance == null){
            instance = new Homography();
        }
        return instance;
    }

    public boolean setHomographyMatrix(Mat mRgba){
        homography = new Mat();
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
            Calib3d.drawChessboardCorners(mRgba, mPatternSize, mCorners, mPatternWasFound); //for visualization
            homography =  Calib3d.findHomography(mCorners, RealWorldC);
            return true;
        }else{
         return false;
        }
    }

    public Point getPosition(Point cameraSpace){
        Mat src =  new Mat(1, 1, CvType.CV_32FC2);
        Mat dest = new Mat(1, 1, CvType.CV_32FC2);
        src.put(0, 0, new double[] { cameraSpace.x, cameraSpace.y }); // ps is a point in image coordinates
        Core.perspectiveTransform(src, dest, homography); //homography is your homography matrix
        //we get points in millimeter, to convert them to cm we divide by 10
        return new Point(dest.get(0, 0)[0] / 10 , dest.get(0, 0)[1] / 10);
    }

    /**
     * calculates the translation matrix and returns the translated point
     * @param roboPosition the postion of the roboter which delivers the odomtry
     * @param relativeBallPosition the position of the ball in the robots coordinate system
     * @return the translated point
     */
    private static com.example.alexander.robotop.datastruct.Point translate(com.example.alexander.robotop.datastruct.Point relativeBallPosition, com.example.alexander.robotop.datastruct.Point roboPosition){
        int x = relativeBallPosition.getX();
        int y = relativeBallPosition.getY();
        int transX = roboPosition.getX();
        int transY = roboPosition.getY();
        double [] vectorData = {x,y,1};
        double [][] transData = {{1,0,transX},{0,1,transY},{0,0,1}};
        RealMatrix trans = new Array2DRowRealMatrix(transData);
        RealMatrix vector = new Array2DRowRealMatrix(vectorData);
        RealMatrix mult = trans.multiply(vector);
        int newX = (int)Math.round(mult.getEntry(0,0));
        int newY = (int)Math.round(mult.getEntry(1,0));
        com.example.alexander.robotop.datastruct.Point ret = new com.example.alexander.robotop.datastruct.Point(newX,newY);
        return ret;
    }

    /**
     * calculates the rotation matrix and rotates the point by the angle
     * @param angle angle of rotation
     * @param p the relative ball position in the robots coordinate system
     * @return rotated point
     */
    private static com.example.alexander.robotop.datastruct.Point rotate(com.example.alexander.robotop.datastruct.Point p, int angle){
        int x = p.getX();
        int y = p.getY();
        double radian = Math.toRadians(angle);
        double [] vectorData = {x,y,1};
        double cos = Math.cos(radian);
        double sin = Math.sin(radian);
        double [][] rotData = {{cos,-sin,0},{sin,cos,0},{0,0,1}};
        RealMatrix rotation = new Array2DRowRealMatrix(rotData);
        RealMatrix vector = new Array2DRowRealMatrix(vectorData);
        RealMatrix mult = rotation.multiply(vector);
        int newX = (int)Math.round(mult.getEntry(0,0));
        int newY = (int)Math.round(mult.getEntry(1,0));
        com.example.alexander.robotop.datastruct.Point ret = new com.example.alexander.robotop.datastruct.Point(newX, newY);
        return ret;
    }

    /**
     * calculates the balls position in world coordinates
     * @param relativeBallPosition the relative ball position
     * @param angle the angle of the robot
     * @param roboPosition the postion of the robot
     * @return
     */
    public static com.example.alexander.robotop.datastruct.Point toWorldCoordinates(com.example.alexander.robotop.datastruct.Point relativeBallPosition, int angle, com.example.alexander.robotop.datastruct.Point roboPosition){
        com.example.alexander.robotop.datastruct.Point rotation = rotate(relativeBallPosition,angle);
        com.example.alexander.robotop.datastruct.Point ret = translate(rotation, roboPosition);
        return ret;
    }

}
