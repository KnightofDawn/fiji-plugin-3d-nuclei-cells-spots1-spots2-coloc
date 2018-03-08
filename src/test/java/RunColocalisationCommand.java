import de.embl.cba.coloc3d.commands.RunColocalisationAnalysisCommand;
import de.embl.cba.coloc3d.filters.DifferenceOfMeansOrMediansSettings;
import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import net.imagej.ImageJ;
import de.embl.cba.coloc3d.output.OutputImageCreator;
import de.embl.cba.coloc3d.segmentation.*;

import java.io.File;

public class RunColocalisationCommand
{
    public static void main(final String... args) throws Exception
    {

        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        ij.command().run( RunColocalisationAnalysisCommand.class, true );

    }

}
