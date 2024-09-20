package project;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.Blitter;

public class Task_1_Threshold implements PlugInFilter {

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Thresholding");
        gd.addNumericField("Threshold value:", 128, 0);
        gd.addCheckbox("Correct uneven illumination", false);
        gd.showDialog();
        //check if the dialog was canceled
        if (gd.wasCanceled())
            return;

        //get user choices
        int threshold = (int) gd.getNextNumber();
        boolean correct = gd.getNextBoolean();

        //correct illumination if selected
        ImageProcessor ipCopy;
        if (correct) {
            ipCopy = correctIllumination(ip);
        } else {
            ipCopy = ip; // Use original image if no correction
        }
        // threshold the image and display
        ByteProcessor thresholdedIp = threshold(ipCopy, threshold);
        ImagePlus thresholdedImage = new ImagePlus("Thresholded Image", thresholdedIp);
        thresholdedImage.show();

    }

    public ByteProcessor threshold ( ImageProcessor ip , int threshold ){

        int width = ip.getWidth();
        int height = ip.getHeight();

        ByteProcessor resultBP = new ByteProcessor(width,height);

        // Iterate through each pixel and apply thresholding
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = ip.get(x,y);// Get pixel value
                if (pixelValue > threshold) {
                    resultBP.set(x,y,255); // Set pixel to white if above threshold
                } else {
                    resultBP.set(x,y,0);// Set pixel to black if below threshold
                }
            }
        }
        return resultBP;
    }

    public ByteProcessor correctIllumination(ImageProcessor ip) {
        FloatProcessor originalFP = ip.convertToFloatProcessor();
        FloatProcessor blurFP = ip.convertToFloatProcessor();
        blurFP.blurGaussian(75);
        //division of originalFP and blurFP
        FloatProcessor resultFP = new FloatProcessor(ip.getWidth(), ip.getHeight());
        resultFP.copyBits(originalFP, 0, 0, Blitter.COPY); // Copy original image to result
        resultFP.copyBits(blurFP, 0, 0, Blitter.DIVIDE);  // Divide by blurred image
        ByteProcessor resultBP = resultFP.convertToByteProcessor();
        return resultBP;
    }

}

