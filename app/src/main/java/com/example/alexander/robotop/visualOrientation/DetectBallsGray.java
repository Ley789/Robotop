package com.example.alexander.robotop.visualOrientation;

/**
 * Created by Alexander on 28/04/2015.
 */
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Callable;

public class DetectBallsGray implements Callable<Mat>{
    private Mat mRgba;
    public DetectBallsGray(Mat mat){
        mRgba = mat;

    }
    /**
     * Returns a mat of circles of red balls
     */
    @Override
    public Mat call() throws Exception {
        Mat mHSV = new Mat();
        Mat res = new Mat();
        Mat circles = new Mat();
        Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2GRAY);

        Imgproc.GaussianBlur(res, res, new Size(9,9),2,2);

        Imgproc.HoughCircles(res, circles, Imgproc.CV_HOUGH_GRADIENT, 2, res.rows()/8,200, 50, 0, 0);
        if(circles.rows() > 0) {
            return circles;
        }
        return null;
    }

}

