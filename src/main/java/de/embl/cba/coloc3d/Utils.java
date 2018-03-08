package de.embl.cba.coloc3d;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Binner;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;

public abstract class Utils
{

    public static ImageByte getByteImageHandler( ImagePlus imp, int channel, int frame )
    {
        ImagePlus impChannelFrame = new Duplicator().run( imp , channel, channel, 1, imp.getNSlices(), frame, frame);
        return new ImageByte( impChannelFrame );
    }

    public static ImagePlus getImagePlus( ImagePlus imp, int channel, int frame )
    {
        ImagePlus impChannelFrame = new Duplicator().run( imp , channel, channel, 1, imp.getNSlices(), frame, frame);
        return impChannelFrame;
    }

    public static ImageHandler getBinnedByteImageHandler( ImagePlus imp, int nucleiChannel, int binX, int binY, int binZ, int frame )
    {
        ImagePlus nucleiImp = getImagePlus( imp, nucleiChannel, frame );
        ImagePlus binnedNucleiImp = new Binner().shrink( nucleiImp, binX, binY, binZ, Binner.MEDIAN );
        return new ImageByte( binnedNucleiImp );
    }


}
