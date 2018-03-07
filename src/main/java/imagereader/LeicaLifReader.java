package imagereader;

import ij.ImagePlus;

import loci.formats.FormatException;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImporterOptions;
import loci.plugins.in.ImportProcess;
import loci.plugins.util.ImageProcessorReader;

import java.io.IOException;


public abstract class LeicaLifReader
{

    public static ImagePlus open( String pathName, int seriesNumber )
    {
        try
        {
            ImporterOptions opts = new ImporterOptions();
            opts.setId( pathName );
            opts.setUngroupFiles( true );
            opts.setSeriesOn(seriesNumber, true );

            ImportProcess process = new ImportProcess( opts );
            process.execute();

            int numSeries = process.getSeriesCount();

            ImagePlusReader impReader = new ImagePlusReader( process );
            
            ImagePlus[] imps = impReader.openImagePlus();
            return imps[0];

        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( FormatException e )
        {
            e.printStackTrace();
        }

        return null;


    }

}
