package segmentation;

import filters.DifferenceOfMeansOrMedians;
import ij.util.ThreadUtil;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.Segment3DSpots;
import mcib3d.image3d.processing.FastFilters3D;

public class SpotSegmenter
{

    private final SpotSegmenterSettings settings;
    int numThreads;

    public SpotSegmenter( SpotSegmenterSettings settings )
    {
        this.settings = settings;
        this.numThreads = ThreadUtil.getNbCpus();
    }

    public SpotSegmenterOutput segment( ImageHandler input )
    {

        ImageHandler filtered = filter( new ImageFloat( input ) );

        filtered.getImagePlus().show();

        ImageHandler localMaxima = new ImageShort( FastFilters3D.filterFloatImage( new ImageFloat( filtered ), FastFilters3D.MAXLOCAL, settings.localMaximaRadiusXY, settings.localMaximaRadiusXY, settings.localMaximaRadiusZ, numThreads, false ), false );

        localMaxima.getImagePlus().show();

        Segment3DSpots segment3DSpots = new Segment3DSpots( filtered, localMaxima );
        segment3DSpots.show = false;
        segment3DSpots.setSeedsThreshold( settings.seedsThreshold );
        segment3DSpots.setLocalThreshold( 0 ); // TODO: what does that do?
        segment3DSpots.setWatershed( settings.useWatershedForObjectSplitting );
        segment3DSpots.setVolumeMin( settings.minVolume );
        segment3DSpots.setVolumeMax( settings.maxVolume );
        segment3DSpots.setMethodLocal( Segment3DSpots.LOCAL_GAUSS );
        segment3DSpots.setGaussPc( settings.gaussFitBorderInSigma );
        segment3DSpots.setGaussMaxr( settings.gaussFitMaxRadius );

        segment3DSpots.segmentAll();

        SpotSegmenterOutput spotSegmenterOutput = new SpotSegmenterOutput();
        spotSegmenterOutput.objects = segment3DSpots.getObjects();
        spotSegmenterOutput.labelMask = segment3DSpots.getLabelImage();

        return spotSegmenterOutput;
    }


    public ImageHandler filter( ImageHandler input )
    {
        if ( settings.preprocessWithDifferenceOfMeansOrMedians )
        {
            DifferenceOfMeansOrMedians filter = new DifferenceOfMeansOrMedians( settings.differenceOfMeansOrMediansSettings );
            return filter.compute( input );
        }
        else
        {
            return input;
        }
    }


}
