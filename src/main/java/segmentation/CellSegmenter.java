package segmentation;

import ij.util.ThreadUtil;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.image3d.regionGrowing.Watershed3D;

public class CellSegmenter
{
    int numThreads;
    CellSegmenterSettings settings;

    public CellSegmenter( CellSegmenterSettings settings )
    {
        this.settings = settings;
        this.numThreads = ThreadUtil.getNbCpus();

    }

    public ImageHandler segment( ImageHandler cellImage, ImageHandler nucleusLabelMask )
    {
        ImageHandler smoothed = smooth( cellImage );

        ImageHandler cellLabelMask = seededWatershed( nucleusLabelMask, smoothed );

        return  cellLabelMask;
    }

    private ImageHandler seededWatershed( ImageHandler nucleusLabelMask, ImageHandler smoothed )
    {
        int seedsThreshold = 1;

        Watershed3D watershed3D = new Watershed3D( smoothed, nucleusLabelMask, settings.threshold, seedsThreshold );

        return watershed3D.getWatershedImage3D();
    }

    private ImageHandler smooth( ImageHandler input )
    {
        ImageHandler smoothed = input;

        if ( settings.smooth )
        {
            smoothed = FastFilters3D.filterImage( input, FastFilters3D.MEDIAN, settings.smoothingRadiusX, settings.smoothingRadiusY, settings.smoothingRadiusZ, numThreads, false );
        }

        return smoothed;
    }

}



