package de.embl.cba.coloc3d.segmentation;

import ij.util.ThreadUtil;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.Segment3DImage;
import mcib3d.image3d.processing.FastFilters3D;

//Segment3DImage,ImageShort,ImageFloat,Segment3DSpots

public class NucleusSegmenter
{

    NucleusSegmenterSettings settings;
    int numThreads;

    public NucleusSegmenter( NucleusSegmenterSettings settings )
    {
        this.settings = settings;
        this.numThreads = ThreadUtil.getNbCpus();
    }

    public ImageHandler segment( ImageHandler input )
    {

        ImageHandler smoothed = smooth( input );

        ImageHandler labelMask = thresholdAndConnectedComponents( smoothed );

        /*
        // close holes
        closingRadiusXY = _inputParameters.getParameterValue( "Nucleus Closing Radius XY" )
        closingRadiusZ = _inputParameters.getParameterValue( "Nucleus Closing Radius Z" )
        maskImageHandler = BinaryMorpho.binaryCloseMultilabel( maskImageHandler, closingRadiusXY, closingRadiusZ )
        FillHoles3D().process( ImageShort( maskImage ), 1, ncpu, False )
        */

        // Split touching objects
        //


        // nucleiObjects = Objects3DPopulation( maskImage )

        // Remove touching borders
        //

        // nucleiObjects.removeObjectsTouchingBorders( _nucleusImage, False )

        return labelMask;
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

    private ImageHandler thresholdAndConnectedComponents( ImageHandler smoothed )
    {
        Segment3DImage nucleiSegmentor = new Segment3DImage( smoothed, settings.threshold, Float.MAX_VALUE );
        nucleiSegmentor.setMinSizeObject( settings.minVolume );
        nucleiSegmentor.setMaxSizeObject( settings.maxVolume );
        nucleiSegmentor.segment();
        return nucleiSegmentor.getLabelledObjectsImage3D();
    }


}
