package com.example.alexander.robotop.visualOrientation;

import com.example.alexander.robotop.datastruct.ColorBound;
import com.example.alexander.robotop.datastruct.Point;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Alexander on 11/05/2015.
 */
public class Beacon {
    private static double mMinContourArea = 0.1;
    private Point worldCoordinate;

    private List<ColorBound> bounds = new ArrayList<>();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();


    public Beacon(Point world, List<ColorBound> color){
        bounds = color;
        worldCoordinate = world;
    }

    public void process(Mat rgbaImage) {
        Mat mPyrDownMat = new Mat();
        Mat mHsvMat = new Mat();
        Mat mDilatedMask = new Mat();
        Mat mHierarchy = new Mat();
        Mat mMask = new Mat();
        Mat mTmpMask = new Mat();
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        for(ColorBound c: bounds ){
            Core.inRange(mHsvMat, c.getmLowerBound(), c.getmUpperBound(), mTmpMask);
            //test coz mMaks is new map so dimension should not be equal
            Core.bitwise_or(mMask,mTmpMask,mMask);
        }

        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }
        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
            }
        }
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }



    public void setWorldCoordinate(Point location){
        worldCoordinate = location;
    }
    public Point getWorldCoordinate(){
        return worldCoordinate;
    }
    public org.opencv.core.Point getRelativePoint(){

        org.opencv.core.Point mc= null;
        int min;

        List<Moments> mu = new ArrayList(mContours.size() );
        for( int i = 0; i < mContours.size(); i++ )
        { mu.add(i,Imgproc.moments( mContours.get(i) , false )); }

        ///  Get the mass centers:
        mc = new org.opencv.core.Point(mu.get(0).get_m10() / mu.get(0).get_m00(), mu.get(0).get_m01() / mu.get(0).get_m00());
        return mc;
    }
}
