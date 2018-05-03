package de.embl.cba.coloc3d.output;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ZProjector;
import mcib3d.image3d.ImageHandler;

import java.io.File;

public abstract class OutputImageCreator
{

    public static void saveLabelMaskAsMaximumProjection( ImageHandler labelMask, String directory, String filename )
    {
        ImagePlus imp = labelMask.getImagePlus().duplicate();
        imp = ZProjector.run( imp, "max" );
        IJ.run( imp, "Grays", "" );
        IJ.run( imp, "Enhance Contrast", "saturated=0.0" );
        imp.setTitle( filename );
        IJ.saveAs( imp, "Jpeg", directory + File.separator + filename );
    }

}
