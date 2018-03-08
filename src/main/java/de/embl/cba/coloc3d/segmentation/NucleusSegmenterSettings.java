package de.embl.cba.coloc3d.segmentation;

public class NucleusSegmenterSettings
{
    public int minVolume = 0;
    public int maxVolume = Integer.MAX_VALUE;
    public boolean smooth = false;
    public float smoothingRadiusX = 0, smoothingRadiusY = 0, smoothingRadiusZ = 0;
    public float threshold = 0;
}
