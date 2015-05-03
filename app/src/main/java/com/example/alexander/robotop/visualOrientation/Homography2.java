package com.example.alexander.robotop.visualOrientation;

/**
 * Alternative vom letzen Semester.
 */



    import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

    public class Homography2 extends Mat {
        private static final String TAG = "Homography";

        public Homography2(Mat image) {
            super(setHomographyMatrix(image), Range.all(), Range.all());
        }


        /**
         *
         * @param p the Point for which distance has to be calculated
         * @return distance to point from camera in cm
         */
        public double getPosition(Point p) {
            Mat src = new Mat(1, 1, CvType.CV_32FC2);
            Mat dest = new Mat(1, 1, CvType.CV_32FC2);
            src.put(0, 0, new double[] { p.x, p.y });
            Core.perspectiveTransform(src, dest, this);
            Point dest_point = new Point(src.get(0, 0)[0], dest.get(0, 0)[1]);
            return dest_point.y/10;
        }

        /**
         * Calculates a homography matrix with help of given chessboard pattern
         * @param image current frame of camera
         * @return homography matrix
         */
        public static Mat setHomographyMatrix(Mat image){

            Mat gray=new Mat();
            final Size mPatternSize = new Size(6, 9);
            MatOfPoint2f mCorners,RealWorldC;
            mCorners = new MatOfPoint2f();
            Mat homography=new Mat();
            boolean mPatternWasFound = false;

            //second version of points, in real world coordinates X=width, Y=height
            RealWorldC = new MatOfPoint2f(
                    new Point(-48.0f, 309.0f-160), new Point(-48.0f, 321.0f-160),new Point(-48.0f, 333.0f-160), new Point(-48.0f, 345.0f-160),new Point(-48.0f, 357.0f-160), new Point(-48.0f, 369.0f-160),
                    new Point(-36.0f, 309.0f-160), new Point(-36.0f, 321.0f-160),new Point(-36.0f, 333.0f-160), new Point(-36.0f, 345.0f-160),new Point(-36.0f, 357.0f-160), new Point(-36.0f, 369.0f-160),
                    new Point(-24.0f, 309.0f-160), new Point(-24.0f, 321.0f-160),new Point(-24.0f, 333.0f-160), new Point(-24.0f, 345.0f-160),new Point(-24.0f, 357.0f-160), new Point(-24.0f, 369.0f-160),
                    new Point(-12.0, 309.0f-160),  new Point(-12.0, 321.0f-160), new Point(-12.0, 333.0f-160),  new Point(-12.0, 345.0f-160), new Point(-12.0, 357.0f-160),  new Point(-12.0, 369.0f-160),
                    new Point(0.0f, 309.0f-160),   new Point(0.0f, 321.0f-160),  new Point(0.0f, 333.0f-160),   new Point(0.0f, 345.0f-160),  new Point(0.0f, 357.0f-160),   new Point(0.0f, 369.0f-160),
                    new Point(12.0, 309.0f-160),  new Point(12.0, 321.0f-160), new Point(12.0, 333.0f-160),  new Point(12.0, 345.0f-160), new Point(12.0, 357.0f-160),  new Point(12.0, 369.0f-160),
                    new Point(24.0f, 309.0f-160),  new Point(24.0f, 321.0f-160), new Point(24.0f, 333.0f-160),  new Point(24.0f, 345.0f-160), new Point(24.0f, 357.0f-160),  new Point(24.0f, 369.0f-160),
                    new Point(36.0f, 309.0f-160),  new Point(36.0f, 321.0f-160), new Point(36.0f, 333.0f-160),  new Point(36.0f, 345.0f-160), new Point(36.0f, 357.0f-160),  new Point(36.0f, 369.0f-160),
                    new Point(48.0f, 309.0f-160),  new Point(48.0f, 321.0f-160), new Point(48.0f, 333.0f-160),  new Point(48.0f, 345.0f-160), new Point(48.0f, 357.0f-160),  new Point(48.0f, 369.0f-160));


            Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGBA2GRAY);
            //getting inner corners of chessboard
            List<Mat> mCornersBuffer = new ArrayList<Mat>();
            mPatternWasFound=Calib3d.findChessboardCorners(gray,mPatternSize, mCorners);

            if (mPatternWasFound) {
                // Calib3d.drawChessboardCorners(mRgba, mPatternSize, mCorners, mPatternWasFound);//for testing
                mCornersBuffer.add(mCorners.clone());
                homography=Calib3d.findHomography(mCorners,  RealWorldC);
                Log.i(TAG, "homography Found");
                return homography;
            }
            return null;

        }

}
