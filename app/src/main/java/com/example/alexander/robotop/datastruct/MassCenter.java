package com.example.alexander.robotop.datastruct;

import com.example.alexander.robotop.modell.Detector;

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
    private double mMinContourArea = 0.5;
    private static MassCenter instance = null;
    //we got 4 diferent Colors so we got 4 mass points
    //indexes will match color indexes e.g. for massCenter.get(0) there will be stored all red mass centers
    private List<List<Rect>> massRect = new ArrayList<>();
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
        massRect.clear();

        ColorBound c;
        for (int i = 0; i < colorList.size(); i++) {
            c = colorList.get(i);
            if (c != null) {
                List<Rect> rects = getMassPoint(process(mRgba, c));
                massRect.add(i, rects);
            }
        }
    }

    public List<List<Rect>> getMassRect() {
        return massRect;
    }

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
            if(r.height * r.width > 80 && approxCurve.toArray().length < 7)
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

       // Core.inRange(mHsvMat, c.getmLowerBound(), c.getmUpperBound(), mMask);
        mMask = Detector.detectBlob(mHsvMat,c);
        Imgproc.dilate(mMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4)));

        Imgproc.erode(mDilatedMask, mDilatedMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4)));

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
        List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                mContours.add(contour);
            }
        }
        return mContours;
    }
}
