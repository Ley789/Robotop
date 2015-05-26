package com.example.alexander.robotop.datastruct;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Alexander on 20/05/2015.
 */
public class MassCenter {
    private double mMinContourArea = 0.1;
    private static MassCenter instance = null;
    //we got 4 diferent Colors so we got 4 mass points
    //indexes will match color indexes e.g. for massCenter.get(0) there will be stored all red mass centers
    private List<List<org.opencv.core.Point>> massCenter = new ArrayList<>();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    //first color in colorList mass points will be in massCenters first element
    public List<ColorBound> colorList = new ArrayList<>();


    private MassCenter() {
    }

    public static MassCenter getInstance() {
        if (instance == null) {
            instance = new MassCenter();
        }
        return instance;
    }

    public void calculateMassCenter(Mat mRgba) {
        massCenter.clear();
        mContours.clear();
        ColorBound c;
        for (int i = 0; i < colorList.size(); i++) {
            c = colorList.get(i);
            if (c != null) {

                //original
                //massCenter.add(i, getMassPoint(process(mRgba, c)));

                //test
                List<Rect> rects = getMassPoint(process(mRgba, c));
                List<Point> points = new ArrayList<>();

                for (Rect r : rects) {
                    points.add(new Point(r.x + (r.width / 2), r.y + (r.height / 2)));
                }

                massCenter.add(i, points);
            }
        }
    }

    public List<List<org.opencv.core.Point>> getMassCenter() {
        return massCenter;
    }

    /* Original
   private List<org.opencv.core.Point> getMassPoint(List<MatOfPoint> mContours){
       if(mContours.size() < 1)
           return null;

       List<Moments> mu = new ArrayList(mContours.size() );

       for( int i = 0; i < mContours.size(); i++ )
       { mu.add(i, Imgproc.moments(mContours.get(i), false));}
       ///  Get the mass centers:
       List<org.opencv.core.Point> mc = new ArrayList<org.opencv.core.Point>(mu.size());
       for( int i = 0; i < mContours.size(); i++ )
       { mc.add(i,new org.opencv.core.Point(mu.get(i).get_m10() / mu.get(i).get_m00(), mu.get(i).get_m01() / mu.get(i).get_m00())); }
       return mc;


      }

*/
//test me
    private List<Rect> getMassPoint(List<MatOfPoint> mContours) {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Rect r;
        List<Rect> rect = new ArrayList<>(mContours.size());
        //For each contour found
        for (int i = 0; i < mContours.size(); i++)

        {
            //Convert contours(i) from MatOfPoint to MatOfPoint2f
            MatOfPoint2f contour2f = new MatOfPoint2f(mContours.get(i).toArray());
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);


                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());
                r = Imgproc.boundingRect(points);
                // Get bounding rect of contour
                if(r.height * r.width > 100 && approxCurve.toArray().length < 7)
                  rect.add(r);
        }


        return rect;
    }

    private List<MatOfPoint> process(Mat rgbaImage, ColorBound c) {
        Mat mPyrDownMat = new Mat();
        Mat mHsvMat = new Mat();
        Mat mDilatedMask = new Mat();
        Mat mHierarchy = new Mat();
        Mat mMask = new Mat();

        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, c.getmLowerBound(), c.getmUpperBound(), mMask);

        Imgproc.dilate(mMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

        Imgproc.erode(mDilatedMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

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
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                mContours.add(contour);
            }
        }
        //TODO maybe filter squares
        return mContours;
    }
}
