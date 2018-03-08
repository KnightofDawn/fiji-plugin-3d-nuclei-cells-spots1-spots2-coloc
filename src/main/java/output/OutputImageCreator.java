package output;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ZProjector;
import mcib3d.image3d.ImageHandler;

public abstract class OutputImageCreator
{

    public static void saveLabelMaskAsMaximumProjection( ImageHandler labelMask, String path )
    {
        ImagePlus imp = labelMask.getImagePlus();
        imp = ZProjector.run( imp, "max" );
        IJ.run(imp, "3-3-2 RGB", "");
        IJ.run(imp, "Enhance Contrast", "saturated=0.1");
        IJ.saveAs(imp, "Jpeg", path );
    }

}
