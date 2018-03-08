package filters;

import ij.util.ThreadUtil;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.processing.FastFilters3D;

public class DifferenceOfMeansOrMedians
{
    private final DifferenceOfMeansOrMediansSettings settings;
    int numThreads;

    public DifferenceOfMeansOrMedians( DifferenceOfMeansOrMediansSettings settings )
    {
        this.settings = settings;
        this.numThreads = ThreadUtil.getNbCpus();
    }

    public ImageHandler compute( ImageHandler input )
    {
        ImageHandler floatInput = new ImageFloat( input.getImagePlus() );

        ImageHandler smallBlur = FastFilters3D.filterImage(
                floatInput,
                settings.method,
                settings.smallFilterSizeXY,
                settings.smallFilterSizeXY,
                settings.smallFilterSizeZ,
                numThreads,
                false);

        ImageHandler largeBlur = FastFilters3D.filterImage(
                floatInput,
                settings.method,
                settings.largeFilterSizeXY,
                settings.largeFilterSizeXY,
                settings.largeFilterSizeZ,
                numThreads,
                false);

        ImageHandler difference = smallBlur.addImage( largeBlur, 1, -1  );

        return difference;
    }


}
