package de.embl.cba.coloc3d.segmentation;

import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

import java.util.ArrayList;

public class SpotSegmenterOutput
{
    public ArrayList< Object3D > objects;
    public ImageInt labelMask;

}
