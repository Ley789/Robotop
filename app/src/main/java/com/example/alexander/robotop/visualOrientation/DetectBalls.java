package com.example.alexander.robotop.visualOrientation;

/**
 * Created by Alexander on 28/04/2015.
 */
import com.example.alexander.robotop.datastruct.ColorBound;
import com.example.alexander.robotop.modell.Detector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Callable;

public class DetectBalls implements Callable<Mat>{
    private Mat mRgba;
    private ColorBound color;
    public DetectBalls(Mat mat, ColorBound c){
        mRgba = mat;
        color =c;

    }
    /**
     * Returns a mat of circles of balls
     */
    @Override
    public Mat call() throws Exception {
        Mat mHSV = new Mat();
        Mat res = new Mat();
        Mat circles = new Mat();
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV_FULL);

        res = Detector.detectBlob(mHSV,color);
        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate( res, res, erodeElement );
        Imgproc.erode(res, res, erodeElement);

        Imgproc.GaussianBlur(res, res, new Size(9,9),0,0);

        Imgproc.HoughCircles(res, circles, Imgproc.CV_HOUGH_GRADIENT, 2, res.height()/4,100, 70, 0, 150);
        if(circles.rows() > 0) {
            return circles;
        }
        return null;
    }

}

