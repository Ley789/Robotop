package com.example.alexander.robotop.datastruct;

import org.opencv.core.Scalar;

/**
 * Created by Alexander on 11/05/2015.
 */
public class ColorBound {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,100,100,0);

    public ColorBound(){}
    public ColorBound(ColorBound c){
        mLowerBound.set(c.getmLowerBound().val);
        mUpperBound.set(c.getmUpperBound().val);
    }
    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 360) ? hsvColor.val[0]+mColorRadius.val[0] : 360;
        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;
        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];
        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];
        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;
        if (mLowerBound.val[1] < 0){
            mLowerBound.val[1] = 0;
        }
        if(mUpperBound.val[1] > 255){
            mUpperBound.val[1] = 255;
        }
        if (mLowerBound.val[2] < 0){
            mLowerBound.val[2] = 0;
        }
        if(mUpperBound.val[2] > 255){
            mUpperBound.val[2] = 255;
        }
     }


    public Scalar getmLowerBound() {
        return mLowerBound;
    }

    public Scalar getmUpperBound() {
        return mUpperBound;
    }
    public boolean isRed(){
        boolean isRed = false;
        double hueValue = mUpperBound.val[0] - mColorRadius.val[0];
        if(hueValue <12 || hueValue > 235 ){
            isRed = true;
        }

        return isRed;
    }
    @Override
    public String toString(){
        return " Lower bound " + mLowerBound.toString() + " | "+ mUpperBound.toString();
    }
}
