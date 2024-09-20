package project;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Task_5_CannyEdgeDetection implements PlugInFilter {

    @Override
    public void run(ImageProcessor imageProcessor) {
        int[][] SobelX = {{1,0,-1},{2,0,-2},{1,0,-1}};
        int[][] SobelY = {{1,2,1},{0,0,0},{-1,-2,-1}};

        GenericDialog gd = new GenericDialog("Hysteresis Thresholding");
        gd.addNumericField("Sigma for Gaussian Blur:", 2, 0);
        gd.addNumericField("Upper Threshold (%):", 15, 0);
        gd.addNumericField("Lower Threshold (%):", 5, 0);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        int sigma = (int) gd.getNextNumber();
        int upperThreshold = (int) gd.getNextNumber();
        int lowerThreshold = (int) gd.getNextNumber();

        FloatProcessor blurFP = imageProcessor.convertToFloatProcessor();
        blurFP.blurGaussian(sigma);

        Task_4_Filters filters = new Task_4_Filters();
        FloatProcessor fpX = filters.applyFilter(blurFP, SobelX);
        FloatProcessor fpY = filters.applyFilter(blurFP, SobelY);
        FloatProcessor gradientFp = filters.getGradient(fpX, fpY);

        ByteProcessor dirFp = getDir(fpX, fpY);
        FloatProcessor suppressedFp = nonMaxSuppress(gradientFp, dirFp);
        ByteProcessor finalEdges = hysteresisThreshold(suppressedFp, upperThreshold, lowerThreshold);
        ImagePlus resultImage = new ImagePlus("Canny Edge Detection " + sigma + " " +upperThreshold + " " + lowerThreshold, finalEdges);

        resultImage.show();

    }
    public ByteProcessor getDir (FloatProcessor X_Deriv, FloatProcessor Y_Deriv){
        if (X_Deriv.getWidth() != Y_Deriv.getWidth() || X_Deriv.getHeight() != Y_Deriv.getHeight()) {
            throw new IllegalArgumentException("Input images must have the same dimensions");
        }
        ByteProcessor dirBP = new ByteProcessor(X_Deriv.getWidth(),X_Deriv.getHeight());
        int[] angles = {0,45,90,135,180};

        for (int y = 0; y < X_Deriv.getHeight(); y++) {
            for (int x = 0; x < X_Deriv.getWidth(); x++) {
                double gx = X_Deriv.getPixelValue(x, y);
                double gy = Y_Deriv.getPixelValue(x, y);
                double angle = Math.toDegrees(Math.atan2(-gy, gx));
                if(angle < 0) {
                    angle = angle + 180;
                }

                int closestAngle = angles[0];
                double minDiff = Math.abs(angle - closestAngle);
                for (int a : angles) {
                    double diff = Math.abs(angle - a);
                    if (diff < minDiff) {
                        minDiff = diff;
                        closestAngle = a;
                    }
                }
                dirBP.putPixel(x, y, closestAngle);
            }
        }
        return dirBP;
    }

    public FloatProcessor nonMaxSuppress(FloatProcessor Grad, ByteProcessor Dir) {
        int width = Grad.getWidth();
        int height = Grad.getHeight();
        FloatProcessor resultFP = new FloatProcessor(width, height);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gradValue = Grad.getPixelValue(x, y);
                int direction = Dir.getPixel(x, y);
                boolean isLocalMax = switch (direction) {
                    case 0 -> // 0° direction (horizontal)
                            gradValue >= Grad.getPixelValue(x - 1, y) && gradValue >= Grad.getPixelValue(x + 1, y);
                    case 45 -> // 45° direction (diagonal)
                            gradValue >= Grad.getPixelValue(x + 1, y - 1) && gradValue >= Grad.getPixelValue(x - 1, y + 1);
                    case 90 -> // 90° direction (vertical)
                            gradValue >= Grad.getPixelValue(x, y - 1) && gradValue >= Grad.getPixelValue(x, y + 1);
                    case 135 -> // 135° direction (diagonal)
                            gradValue >= Grad.getPixelValue(x - 1, y - 1) && gradValue >= Grad.getPixelValue(x + 1, y + 1);
                    default -> false;
                };
                if (isLocalMax) {
                    resultFP.putPixelValue(x, y, gradValue);
                } else {
                    resultFP.putPixelValue(x, y, 0);
                }
            }
        }
        return resultFP;
    }

    public ByteProcessor hysteresisThreshold (FloatProcessor In, int upper, int lower){
        float tHigh = ((float)In.getMax()*upper)/100f;
        float tLow = ((float)In.getMax()*lower)/100f;

        int width = In.getWidth();
        int height = In.getHeight();
        ByteProcessor resultBP = new ByteProcessor(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float pixelValue = In.getPixelValue(x, y);
                if (pixelValue >= tHigh) {
                    resultBP.set(x, y, 255);
                } else {
                    resultBP.set(x, y, 0);
                }
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int x = 0; x < In.getWidth(); x++) {
                for (int y = 0; y < In.getHeight(); y++) {
                    if (In.getPixelValue(x, y) >= tLow && hasNeighbours(resultBP, x, y) && resultBP.getPixel(x,y)==0) {
                        resultBP.set(x, y, 255);
                        changed = true;
                    }
                }
            }
        }
        return resultBP;
    }

    public boolean hasNeighbours(ByteProcessor BP, int x, int y ){
        int count = (BP.getPixel(x+1,y)+BP.getPixel(x-1,y)+BP.getPixel(x,y+1)+BP.getPixel(x,y-1)+BP.getPixel(x+1,y+1)+
                BP.getPixel(x-1,y-1)+BP.getPixel(x-1,y+1)+BP.getPixel(x+1,y-1));
        count/=255;
        return (count>0) ;
    }

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }
}
