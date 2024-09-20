package project;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Task_3_Otsu implements PlugInFilter {
    int NUM_INTENSITY_LEVELS = 256;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        // instance of Task_1_Threshold to correct illumination
        Task_1_Threshold Threshold = new Task_1_Threshold();
        ByteProcessor correctBp = Threshold.correctIllumination(imageProcessor);
        //Otsu's threshold value
        int threshold = otsuGetThreshold(correctBp);
        System.out.print(threshold);
        //Otsu's segmentation with the computed threshold
        ByteProcessor thresholdedIp = otsuSegementation(correctBp, threshold);
        ImagePlus thresholdedImage = new ImagePlus("Otsu-Segmentation Image", thresholdedIp);
        thresholdedImage.show();

    }

    public ByteProcessor otsuSegementation(ImageProcessor ip, int threshold){
        ByteProcessor resultBP = new ByteProcessor(ip.getWidth(), ip.getHeight());
        // Apply the threshold to each pixel
        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                int pixelValue = ip.get(x, y);
                if (pixelValue >= threshold) {
                    resultBP.set(x, y, 255);
                } else {
                    resultBP.set(x, y, 0);
                }
            }
        }
        return resultBP;
    }

    public double[] getHistogram(ImageProcessor in) {
        double width = in.getWidth();
        double height = in.getHeight();
        double totalPixels = width*height;
        double[] histogram = new double[NUM_INTENSITY_LEVELS];
        // Compute histogram by counting pixel occurrences
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = in.getPixel(x, y);
                histogram[pixelValue]++;
            }
        }
        // Normalize histogram
        for (int i = 0; i < NUM_INTENSITY_LEVELS; i++) {
            histogram[i] /= totalPixels;
        }
        return histogram;
    };

    public double[] getP1(double[] histogram){
        int numGrayLevels = histogram.length;
        double[] P1 = new double[numGrayLevels];
        P1[0] = histogram[0];
        for (int i=1; i < numGrayLevels;i++) {
            P1[i] = P1[i-1] + histogram[i];
        }
        return P1;
    };

    public double[] getP2(double[] P1){
        int numGrayLevels = P1.length;
        double[] P2 = new double[numGrayLevels];
        for (int i = 0; i < numGrayLevels; i++) {
            P2[i] = 1.0 - P1[i];
        }
        return P2;
    };

    public double[] getMu1(double[] histogram, double[] P1){
        int numGrayLevels = histogram.length;
        double[] Mu1 = new double[numGrayLevels];
        double sum;
        for (int i = 0; i < numGrayLevels; i++) {
            sum=0;
            for (int y = 0; y <= i; y++) {
                sum += (y+1) * histogram[y];
            }
            Mu1[i] = sum / (P1[i] + 1e-10);// Add small value to avoid division by zero
        }
        return Mu1;
    };

    public double[] getMu2(double[] histogram, double[] P2){
        int numGrayLevels = histogram.length;
        double[] Mu2 = new double[numGrayLevels];
        double sum;
        for (int t = 0; t < numGrayLevels; t++) {
            sum = 0;
            for (int i = t+1; i < numGrayLevels; i++) {
                sum += (i +1) * histogram[i];
            }
            Mu2[t] = sum / (P2[t] + 1e-10);// Add small value to avoid division by zero
        }
        return Mu2;
    };

    public double[] getSigmas(double[] histogram, double[] P1, double[] P2, double[] mu1, double[] mu2){
        double[] sigma = new double[histogram.length];
        for (int i = 0; i < histogram.length; i++) {
            sigma[i] = P1[i]*P2[i]*(Math.pow((mu1[i]-mu2[i]),2));
        }
        return sigma;
    };

    public int getMaximum(double[] sigmas){
        if (sigmas.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        int maxIndex = 0;
        double maxSigma = sigmas[0];

        for (int i = 1; i < sigmas.length; i++) {
            if (sigmas[i] > maxSigma) {
                maxSigma = sigmas[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    };

    public int otsuGetThreshold(ImageProcessor in) {
        double[] histogram = getHistogram(in);
        double[] P1 = getP1(histogram);
        double[] P2 = getP2(P1);
        double[] mu1 = getMu1(histogram, P1);
        double[] mu2 = getMu2(histogram, P2);
        double[] sigmas = getSigmas(histogram, P1, P2, mu1, mu2);
        return getMaximum(sigmas);
    }
}