package de.embl.cba.coloc3d.commands;

import automic.table.TableModel;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import java.io.File;
import java.util.ArrayList;

import de.embl.cba.coloc3d.filters.*;
import de.embl.cba.coloc3d.imagereader.*;
import de.embl.cba.coloc3d.output.*;
import de.embl.cba.coloc3d.segmentation.*;
import de.embl.cba.coloc3d.*;

import static de.embl.cba.coloc3d.Utils.getBinnedByteImageHandler;
import static de.embl.cba.coloc3d.Utils.getByteImageHandler;


@Plugin(type = Command.class, menuPath = "Plugins>Segmentation>Development>Colocalisation" )
public class RunColocalisationAnalysisCommand implements Command
{

    @Parameter
    public UIService uiService;

    @Parameter
    public DatasetService datasetService;

    @Parameter
    public LogService logService;

    @Parameter
    public ThreadService threadService;

    @Parameter
    public OpService opService;

    @Parameter
    public StatusService statusService;

    @Parameter( label = "Input image", style = FileWidget.OPEN_STYLE)
    public File inputImageFile;
    public static final String INPUT_IMAGE_FILE = "inputImageFile";

    @Parameter( label = "Leica lif series number", required = false )
    public int leicaLifSeriesNumber;

    @Parameter( label = "Output directory", style = "directory", required = false )
    public File outputImageDirectoryFile;
    public static final String OUTPUT_IMAGE_DIRECTORY = "outputImageDirectoryFile";

    @Parameter( label = "Quit after running", required = true )
    public boolean quitAfterRun = false;
    public static final String QUIT_AFTER_RUN = "quitAfterRun";

    ImagePlus inputImage;


    int nx, ny, nz;
    int nucleusChannel = 1;
    int spots1Channel = 2;
    int cellChannel = 3;
    int spots2Channel = 4;

    int binX = 5;
    int binY = 5;
    int binZ = 1;

    public void run()
    {

        logService.info( "# " + "Colocalisation" );

        try
        {
            runAlgorithm();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        if ( quitAfterRun ) Commands.quitImageJ( logService );

        logService.info( "Done in command." );

        return;

    }


    private void runAlgorithm() throws Exception
    {

        TableModel table = new TableModel( outputImageDirectoryFile.toString() );
        table.addColumn( "DataSet.Name" );
        table.addValueColumn( "Nuclei.Count", "NUM"  );
        table.addValueColumn( "Spots1.Count", "NUM"  );
        table.addValueColumn( "Spots2.Count", "NUM"  );


        // Input parameters
        //

        String inputDirectory = inputImageFile.getParent();
        String inputFile = inputImageFile.getName();
        String outputDirectory = outputImageDirectoryFile.toString();

        // Workflow
        //

        table.addRow();
        int iDataSet = table.getRowCount() - 1;


        String inputPath = inputDirectory + File.separator + inputFile;
        String outputFile;

        logService.info("Opening: " + inputPath );

        ImagePlus inputImp;

        if ( inputPath.endsWith( ".tif" ) )
        {
            inputImp = IJ.openImage( inputPath );
        }
        else if ( inputPath.endsWith( ".lif" ) )
        {
            inputImp = LeicaLifReader.open( inputPath,  leicaLifSeriesNumber );
        }
        else
        {
            logService.error( "Unsupported image file type." );
            return;
        }

        //ImagePlus imp = LeicaLifReader.open( imagePath, 1  );

        inputImp.show();

        nx = inputImp.getWidth();
        ny = inputImp.getHeight();
        nz = inputImp.getNSlices();

        int frame = 1;

        // Segment nuclei
        //
        ImageHandler binnedNucleusLabelMask = getBinnedNucleusLabelMask( inputFile, outputDirectory, inputImp, frame );


        // Segment cells
        //
        ImageInt cellLabelMask = getCellLabelMask( inputFile, outputDirectory, inputImp, frame, binnedNucleusLabelMask );


        // Segment spots
        //
        SpotSegmenterSettings spotSegmenterSettings = getSpotSegmenterSettings();
        SpotSegmenter spotSegmenter = new SpotSegmenter( spotSegmenterSettings );

        // spots 1
        //
        logService.info("Segmenting spots 1..." );

        ImageHandler spots1 = getByteImageHandler( inputImp, spots1Channel, frame );
        SpotSegmenterOutput spotSegmenterOutput1 = spotSegmenter.segment( spots1 );

        outputFile = inputFile + "--spots1LabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( spotSegmenterOutput1.labelMask, outputDirectory + File.separator + outputFile );

        /*
        // spots 2
        //
        logService.info("Segmenting spots 2..." );

        // TODO: - remove binning in final version
        ImageHandler spots2 = getBinnedByteImageHandler( inputImp, spots2Channel, 2, 2, 1, frame );
        SpotSegmenterOutput spotSegmenterOutput2 = spotSegmenter.segment( spots1 );

        table.setNumericValue( spotSegmenterOutput2.objects.size(), iDataSet, "Spots2.Count" );

        outputFile = inputFile + "--spots2LabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( spotSegmenterOutput2.labelMask, outputDirectory + File.separator + outputFile );
        */

        table.writeNewFile( "results.txt", true );

        // count spots per cell
        //
        Objects3DPopulation cellObjectsPopulation = new Objects3DPopulation( cellLabelMask );
        Objects3DPopulation spotObjectsPopulation = new Objects3DPopulation( spotSegmenterOutput1.objects );


        for ( int iCell = 0; iCell < cellObjectsPopulation.getNbObjects(); ++iCell )
        {
            Object3D cell = cellObjectsPopulation.getObject( iCell );
            ArrayList< Object3D > spotsInsideCell = spotObjectsPopulation.getObjectsWithinDistanceBorder( cell, 0.0 );
            spotsInsideCell.size();
        }

        logService.info( "Done!" );
    }

    private ImageHandler getBinnedNucleusLabelMask( String inputFile, String outputDirectory, ImagePlus inputImp, int frame )
    {
        String outputFile;
        logService.info("Segmenting nuclei..." );

        ImageHandler binnedNucleusImage = getBinnedByteImageHandler( inputImp, nucleusChannel, binX, binY, binZ, frame );

        NucleusSegmenterSettings nucleusSegmenterSettings = new NucleusSegmenterSettings();
        nucleusSegmenterSettings.smooth = false;
        nucleusSegmenterSettings.minVolume = 10;
        nucleusSegmenterSettings.threshold = 20;
        NucleusSegmenter nucleusSegmenter = new NucleusSegmenter( nucleusSegmenterSettings );

        ImageHandler binnedNucleusLabelMask = nucleusSegmenter.segment( binnedNucleusImage );

        //ImageHandler nucleusLabelMask = binnedNucleusLabelMask.resample( nx, ny, nz, ImageProcessor.NEAREST_NEIGHBOR );

        //outputFile = inputFile + "--nucleusLabelMask.jpg";
        //OutputImageCreator.saveLabelMaskAsMaximumProjection( nucleusLabelMask, outputDirectory + File.separator + outputFile );

        return binnedNucleusLabelMask;
    }

    private ImageInt getCellLabelMask( String inputFile, String outputDirectory, ImagePlus inputImp, int frame, ImageHandler binnedNucleusLabelMask )
    {
        logService.info("Segmenting cells..." );

        ImageHandler binnedCellImage = getBinnedByteImageHandler( inputImp, cellChannel, binX, binY, binZ, frame );

        CellSegmenterSettings cellSegmenterSettings = new CellSegmenterSettings();
        cellSegmenterSettings.smooth = true;
        cellSegmenterSettings.smoothingRadiusX = 5;
        cellSegmenterSettings.smoothingRadiusY = 5;
        cellSegmenterSettings.smoothingRadiusZ = 1;
        cellSegmenterSettings.threshold = 5;
        CellSegmenter cellSegmenter = new CellSegmenter( cellSegmenterSettings );

        ImageHandler binnedCellLabelMask = cellSegmenter.segment( binnedCellImage, binnedNucleusLabelMask );
        ImageInt cellLabelMask = ( ImageInt ) binnedCellLabelMask.resample( nx, ny, nz, ImageProcessor.NEAREST_NEIGHBOR );

        String outputFile = inputFile + "--cellLabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( cellLabelMask, outputDirectory + File.separator + outputFile );

        return cellLabelMask;
    }

    private SpotSegmenterSettings getSpotSegmenterSettings()
    {
        SpotSegmenterSettings spotSegmenterSettings = new SpotSegmenterSettings();
        spotSegmenterSettings.preprocessWithDifferenceOfMeansOrMedians = true;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings = new DifferenceOfMeansOrMediansSettings();
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.largeFilterSizeXY = 12;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.smallFilterSizeXY = 4;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.largeFilterSizeZ = 1;
        spotSegmenterSettings.differenceOfMeansOrMediansSettings.smallFilterSizeZ = 1;
        spotSegmenterSettings.localMaximaRadiusXY = 6;
        spotSegmenterSettings.localMaximaRadiusZ = 2;
        spotSegmenterSettings.seedsThreshold = 15;
        spotSegmenterSettings.minVolume = 10;
        spotSegmenterSettings.maxVolume = 10000;
        spotSegmenterSettings.gaussFitMaxRadius = 20;
        spotSegmenterSettings.gaussFitBorderInSigma = 1.2;
        spotSegmenterSettings.useWatershedForObjectSplitting = false;
        return spotSegmenterSettings;
    }


}