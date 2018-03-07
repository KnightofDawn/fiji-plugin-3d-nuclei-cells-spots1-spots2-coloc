import ij.ImagePlus;
import imagereader.LeicaLifReader;
import net.imagej.ImageJ;

public class OpenImageFromLif
{

    public static void main(final String... args) throws Exception
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        ImagePlus imp = LeicaLifReader.open("/Volumes/cba/tischer/projects/ratislav-horos--3d-colocalization-autophagy--data/Replicate-2017-12-12/Imaged-2018-03-02/180302imaging_of_171212wt_exp.lif", 1  );
        imp.show();
    }

}
