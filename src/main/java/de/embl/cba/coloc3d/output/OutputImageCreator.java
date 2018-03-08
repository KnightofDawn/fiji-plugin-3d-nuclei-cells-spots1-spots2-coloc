package de.embl.cba.coloc3d.output;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ZProjector;
import mcib3d.image3d.ImageHandler;

public abstract class OutputImageCreator
{

    public static void saveLabelMaskAsMaximumProjection( ImageHandler labelMask, String path )
    {
        ImagePlus imp = labelMask.getImagePlus().duplicate();
        imp = ZProjector.run( imp, "max" );
        IJ.run( imp, "Grays", "" );
        IJ.run( imp, "Enhance Contrast", "saturated=0.0" );
        IJ.saveAs( imp, "Jpeg", path );
    }

}
