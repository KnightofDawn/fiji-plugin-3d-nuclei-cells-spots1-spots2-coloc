import ij.ImagePlus;
import ij.process.ImageProcessor;
import imagereader.LeicaLifReader;
import mcib3d.image3d.ImageHandler;
import net.imagej.ImageJ;
import segmentation.CellSegmenter;
import segmentation.CellSegmenterSettings;
import segmentation.NucleusSegmenter;
import segmentation.NucleusSegmenterSettings;

public class SegmentNucleiCells
{
    public static void main(final String... args) throws Exception
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();


        String imagePath = "/Users/tischer/Documents/ratislav-horos--3d-colocalization-autophagy--data/180207 test aa baf.lif";
        //String imagePath = "/Volumes/cba/tischer/projects/ratislav-horos--3d-colocalization-autophagy--data/Replicate-2017-12-12/Imaged-2018-03-02/180302imaging_of_171212wt_exp.lif";

        ImagePlus imp = LeicaLifReader.open( imagePath, 1  );

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
        ImageHandler binnedNucleusImage = Utils.getBinnedImageHandler( imp, nucleusChannel, binX, binY, binZ, frame );

        NucleusSegmenterSettings nucleusSegmenterSettings = new NucleusSegmenterSettings();
        nucleusSegmenterSettings.smooth = false;
        nucleusSegmenterSettings.minVolume = 10;
        nucleusSegmenterSettings.threshold = 20;
        NucleusSegmenter nucleusSegmenter = new NucleusSegmenter( nucleusSegmenterSettings );

        ImageHandler binnedNucleusLabelMask = nucleusSegmenter.segment( binnedNucleusImage );

        binnedNucleusLabelMask.getImagePlus().show();

        // Segment cells
        //
        ImageHandler binnedCellImage = Utils.getBinnedImageHandler( imp, cellChannel, binX, binY, binZ, frame );

        CellSegmenterSettings cellSegmenterSettings = new CellSegmenterSettings();
        cellSegmenterSettings.smooth = false;
        cellSegmenterSettings.threshold = 20;
        CellSegmenter cellSegmenter = new CellSegmenter( cellSegmenterSettings );

        ImageHandler binnedCellLabelMask = cellSegmenter.segment( binnedCellImage, binnedNucleusLabelMask );

        ImageHandler cellLabelMask = binnedCellLabelMask.resample( nx, ny, nz, ImageProcessor.NEAREST_NEIGHBOR );

        cellLabelMask.getImagePlus().show();

    }

}
