import filters.DifferenceOfMeansOrMediansSettings;
import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import net.imagej.ImageJ;
import output.OutputImageCreator;
import segmentation.*;

import java.io.File;

public class SegmentNucleiCellsSpots
{
    public static void main(final String... args) throws Exception
    {

        // Input parameters
        //

        //String imagePath = "/Users/tischer/Documents/ratislav-horos--3d-colocalization-autophagy--data/180207 test aa baf.lif";
        //String imagePath = "/Volumes/cba/tischer/projects/ratislav-horos--3d-colocalization-autophagy--data/Replicate-2017-12-12/Imaged-2018-03-02/180302imaging_of_171212wt_exp.lif";

        String inputDirectory = "/Users/tischer/Documents/tmp-data";
        String inputFile = "171212 wt exp kd aa.lif - lane 5 image 3 - crop.tif";
        String outputDirectory = "/Users/tischer/Documents/tmp-data/results";


        // Workflow
        //

        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        String inputPath = inputDirectory + File.separator + inputFile;
        String outputFile;

        IJ.log("Opening: " + inputPath );
        ImagePlus imp = IJ.openImage( inputPath );
        //ImagePlus imp = LeicaLifReader.open( imagePath, 1  );

        imp.show();

        int nx = imp.getWidth();
        int ny = imp.getHeight();
        int nz = imp.getNSlices();

        int nucleusChannel = 1;
        int spots1Channel = 2;
        int cellChannel = 3;
        int spots2Channel = 4;

        int binX = 5;
        int binY = 5;
        int binZ = 1;

        int frame = 1;

        // Segment nuclei
        //
        IJ.log("Segmenting nuclei..." );

        ImageHandler binnedNucleusImage = Utils.getBinnedByteImageHandler( imp, nucleusChannel, binX, binY, binZ, frame );

        NucleusSegmenterSettings nucleusSegmenterSettings = new NucleusSegmenterSettings();
        nucleusSegmenterSettings.smooth = false;
        nucleusSegmenterSettings.minVolume = 10;
        nucleusSegmenterSettings.threshold = 20;
        NucleusSegmenter nucleusSegmenter = new NucleusSegmenter( nucleusSegmenterSettings );

        ImageHandler binnedNucleusLabelMask = nucleusSegmenter.segment( binnedNucleusImage );

        outputFile = inputFile + "--binnedNucleusLabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( binnedNucleusLabelMask, outputDirectory + File.separator + outputFile );


        // Segment cells
        //
        IJ.log("Segmenting cells..." );

        ImageHandler binnedCellImage = Utils.getBinnedByteImageHandler( imp, cellChannel, binX, binY, binZ, frame );

        CellSegmenterSettings cellSegmenterSettings = new CellSegmenterSettings();
        cellSegmenterSettings.smooth = false;
        cellSegmenterSettings.threshold = 20;
        CellSegmenter cellSegmenter = new CellSegmenter( cellSegmenterSettings );

        ImageHandler binnedCellLabelMask = cellSegmenter.segment( binnedCellImage, binnedNucleusLabelMask );

        outputFile = inputFile + "--binnedCellLabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( binnedCellLabelMask, outputDirectory + File.separator + outputFile );


        //ImageHandler cellLabelMask = binnedCellLabelMask.resample( nx, ny, nz, ImageProcessor.NEAREST_NEIGHBOR );
        //cellLabelMask.getImagePlus().show();

        // Segment spots - channel 1
        //
        IJ.log("Segmenting spots 1..." );

        // TODO:
        // - remove binnig in final version
        // - this is slow...
        ImageHandler spots1 = Utils.getBinnedByteImageHandler( imp, spots1Channel, 2, 2, 1, frame );

        SpotSegmenterSettings spotSegmenterSettings = new SpotSegmenterSettings();
        spotSegmenterSettings.preprocessWithDifferenceOfMeansOrMedians = true;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings = new DifferenceOfMeansOrMediansSettings();
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.largeFilterSizeXY = 6;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.smallFilterSizeXY = 2;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.largeFilterSizeZ = 1;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.smallFilterSizeZ = 1;
        spotSegmenterSettings.localMaximaRadiusXY = 3;
        spotSegmenterSettings.localMaximaRadiusZ = 2;
        spotSegmenterSettings.seedsThreshold = 15;
        spotSegmenterSettings.minVolume = 4;
        spotSegmenterSettings.maxVolume = 10000;
        spotSegmenterSettings.gaussFitMaxRadius = 10;
        spotSegmenterSettings.gaussFitBorderInSigma = 1.2;

        spotSegmenterSettings.useWatershedForObjectSplitting = false;

        SpotSegmenter spotSegmenter = new SpotSegmenter( spotSegmenterSettings );

        SpotSegmenterOutput spotSegmenterOutput1 = spotSegmenter.segment( spots1 );

        outputFile = inputFile + "--spots1LabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( spotSegmenterOutput1.labelMask, outputDirectory + File.separator + outputFile );


        // Segment spots - channel 2
        //
        IJ.log("Segmenting spots 2..." );

        // TODO:
        // - remove binnig in final version
        // - this is slow...
        ImageHandler spots2 = Utils.getBinnedByteImageHandler( imp, spots2Channel, 2, 2, 1, frame );

        spotSegmenterSettings.seedsThreshold = 15;

        SpotSegmenterOutput spotSegmenterOutput2 = spotSegmenter.segment( spots1 );

        spotSegmenterOutput2.labelMask.getImagePlus().show();

        outputFile = inputFile + "--spots2LabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( spotSegmenterOutput2.labelMask, outputDirectory + File.separator + outputFile );

    }

}
