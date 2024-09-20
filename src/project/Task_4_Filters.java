package project;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

public class Task_4_Filters implements PlugInFilter {

    protected int[][] SobelX = {{1,0,-1},{2,0,-2},{1,0,-1}};
    protected int[][] SobelY = {{1,2,1},{0,0,0},{-1,-2,-1}};

    protected int[][] ScharrX = {{47,0,-47},{162,0,-162},{47,0,-47}};
    protected int[][] ScharrY = {{47,162,47},{0,0,0},{-47,-162,-47}};

    protected int[][] PrewittX = {{1,0,-1},{1,0,-1},{1,0,-1}};
    protected int[][] PrewittY = {{1,1,1},{0,0,0},{-1,-1,-1}};

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {

        String[] Filters = {"Sobel","Scharr","Prewitt"};
        GenericDialog gd = new GenericDialog("Edge Detection");
        gd.addChoice("Filter:", Filters, Filters[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        String selectedFilter = gd.getNextChoice();

        int[][] kernelX = new int[3][3];
        int[][] kernelY = new int[3][3];

        switch (selectedFilter) {
            case "Sobel":
                kernelX = SobelX;
                kernelY = SobelY;
                break;
            case "Scharr":
                kernelX = ScharrX;
                kernelY = ScharrY;
                break;
            case "Prewitt":
                kernelX = PrewittX;
                kernelY = PrewittY;
                break;
        }
        FloatProcessor fp = imageProcessor.convertToFloatProcessor();
        // Apply the selected filter in X and Y directions
        FloatProcessor fpX = applyFilter(fp, kernelX);
        FloatProcessor fpY = applyFilter(fp, kernelY);
        FloatProcessor gradientFp = getGradient(fpX, fpY);

        ImagePlus resultImage = new ImagePlus(selectedFilter + " Edge Detection", gradientFp);
        resultImage.show();
    }

    public FloatProcessor applyFilter (FloatProcessor In, int[][] kernel){
        int width = In.getWidth();
        int height = In.getHeight();

        FloatProcessor resultFp = new FloatProcessor(width,height);
        // Iterate over the image, excluding the outermost row and column
        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                double sum =0;
                // Perform the convolution operation
                for (int kernelY  = -1; kernelY  <= 1; kernelY ++) {
                    for (int kernelX  = -1; kernelX  <= 1; kernelX ++) {
//                        (i, j) = (-1, -1) corresponds to the top-left neighbor.
//                        (i, j) = (0, -1) corresponds to the top-center neighbor.
//                        (i, j) = (1, -1) corresponds to the top-right neighbor.
//                        (i, j) = (-1, 0) corresponds to the middle-left neighbor.
//                        (i, j) = (0, 0) corresponds to the center pixel.
//                        (i, j) = (1, 0) corresponds to the middle-right neighbor.
//                        (i, j) = (-1, 1) corresponds to the bottom-left neighbor.
//                        (i, j) = (0, 1) corresponds to the bottom-center neighbor.
//                        (i, j) = (1, 1) corresponds to the bottom-right neighbor.
                        double pixelValue = In.getPixelValue(x - kernelX,y - kernelY );
//                        When you access the kernel with kernel[j + 1][i + 1], you’re mapping the (-1, 0, 1) range to the (0, 1, 2) index range of the kernel array:
//                        j + 1 shifts -1, 0, 1 to 0, 1, 2 for the row index.
//                        i + 1 shifts -1, 0, 1 to 0, 1, 2 for the column index.
                        sum +=  pixelValue * kernel[kernelY + 1][kernelX + 1];
//                        A row has its orientation in a vertical direction, while a column is in a horizontal direction.
//                        a 2D array (like the kernel) is indexed as array[row][column]. This convention means the first index (y or row)
//                        refers to the vertical position (rows), and the second index (x or column) refers to the horizontal position (columns).
                    }
                }
                resultFp.putPixelValue(x,y,sum);
            }
        }
        return resultFp;
    }

    public FloatProcessor getGradient (FloatProcessor In_X, FloatProcessor In_Y){

        if (In_X.getWidth() != In_Y.getWidth() || In_X.getHeight() != In_Y.getHeight()) {
            throw new IllegalArgumentException("Input images must have the same dimensions");
        }
        FloatProcessor resultFp = new FloatProcessor(In_X.getWidth(),In_X.getHeight());
        for (int y = 0; y < In_X.getHeight(); y++) {
            for (int x = 0; x < In_X.getWidth(); x++) {
                double pixelValue_X = In_X.getPixelValue(x, y);
                double pixelValue_Y = In_Y.getPixelValue(x, y);
                double gradientValue = Math.sqrt(((Math.pow(pixelValue_X,2)) + (Math.pow(pixelValue_Y,2))));
                resultFp.putPixelValue(x, y, gradientValue);
            }
        }
        return resultFp;
    }
}
