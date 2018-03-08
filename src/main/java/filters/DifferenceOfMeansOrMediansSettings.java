package filters;

import mcib3d.image3d.processing.FastFilters3D;

public class DifferenceOfMeansOrMediansSettings
{
    public int method = FastFilters3D.MEAN;

    public float smallFilterSizeXY = 1.0F;
    public float smallFilterSizeZ = 1.0F;

    public float largeFilterSizeXY = 1.0F;
    public float largeFilterSizeZ = 1.0F;


}
