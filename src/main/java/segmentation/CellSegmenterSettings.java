package segmentation;

public class CellSegmenterSettings
{
    public int minVolume = 0;
    public int maxVolume = Integer.MAX_VALUE;
    public boolean smooth = false;
    public float smoothingRadiusX = 0, smoothingRadiusY = 0, smoothingRadiusZ = 0;
    public int threshold = 0;
}



