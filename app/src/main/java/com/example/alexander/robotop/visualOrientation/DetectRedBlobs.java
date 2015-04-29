package com.example.alexander.robotop.visualOrientation;

/**
 * Created by Alexander on 28/04/2015.
 */
import java.util.concurrent.Callable;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class DetectRedBlobs implements Callable<Mat>{
    private Mat mRgba;
    private Scalar hsv_min = new Scalar(0, 150, 100);
    private Scalar hsv_max = new Scalar(15, 255, 255);
    private Scalar hsv_min2 = new Scalar(200, 150, 100);
    private Scalar hsv_max2 = new Scalar(280, 255, 255);

    public DetectRedBlobs(Mat mat){
        mRgba = mat;
    }
    /**
     * Returns a mat of circles of red balls
     */
    @Override
    public Mat call() throws Exception {
        Mat mHSV = new Mat();
        Mat res = new Mat();
        Mat res1 = new Mat();
        Mat circles = new Mat();
        //Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8,8));
        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHSV, hsv_min, hsv_max, res);
        Core.inRange(mHSV, hsv_min2, hsv_max2, res1);
        Core.bitwise_or(res, res1, res);

        Imgproc.GaussianBlur(res, res, new Size(9,9),0,0);
        Imgproc.HoughCircles(res, circles, Imgproc.CV_HOUGH_GRADIENT, 2, res.height()/4,500, 50, 0, 0);
        if(circles.rows() > 0) {
            return circles;
        }
        return null;
    }
}

