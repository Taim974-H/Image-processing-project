package project;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Task_2_EvaluateSegmentation implements PlugInFilter {

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        ImagePlus referenceImage = IJ.openImage();
        if (referenceImage == null) {
            throw new NullPointerException("Reference image is null.");
        }
        ImageProcessor referenceIp = referenceImage.getProcessor();
        EvaluationResult result = evaluateSegmentation(imageProcessor, referenceIp);

        IJ.log("Sensitivity: " + result.getSensitivity());
        IJ.log("Specificity: " + result.getSpecificity());

    }

    private EvaluationResult evaluateSegmentation ( ImageProcessor segmentation , ImageProcessor reference ){
        // Ensure that segmentation and reference images have the same dimensions
        if (segmentation.getWidth() != reference.getWidth() || segmentation.getHeight() != reference.getHeight()) {
            return null;
        }
        // Initialize counters for True Positives (TP), True Negatives (TN), False Positives (FP), and False Negatives (FN)
        int TP = 0, TN = 0, FP = 0, FN = 0;

        // Iterate through each pixel in the images
        for (int y = 0; y < segmentation.getHeight(); y++) {
            for (int x = 0; x < segmentation.getWidth(); x++) {
                int segPixel = segmentation.getPixel(x, y);
                int refPixel = reference.getPixel(x, y);

                if (segPixel == 255 && refPixel == 255) {
                    TP++;
                } else if (segPixel == 0 && refPixel == 0) {
                    TN++;
                } else if (segPixel == 255 && refPixel == 0) {
                    FP++;
                } else if (segPixel == 0 && refPixel == 255) {
                    FN++;
                }
            }
        }
        // Calculate Sensitivity and Specificity
        double sensitivity = TP / (double) (TP + FN);
        double specificity = TN / (double) (TN + FP);

        EvaluationResult result = new EvaluationResult(specificity, sensitivity);
        return result;
    }
}
