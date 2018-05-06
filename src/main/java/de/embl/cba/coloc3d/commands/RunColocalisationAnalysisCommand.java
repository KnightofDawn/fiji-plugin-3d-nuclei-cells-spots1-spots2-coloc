package de.embl.cba.coloc3d.commands;

import automic.table.TableModel;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Objects3DPopulationColocalisation;
import mcib3d.geom.PairColocalisation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import net.imagej.DatasetService;
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


    private static String DATA_SET_NAME = "DataSet.Name";
    private static String COUNT_SPOTS_1 = "Count.Spots1";
    private static String COUNT_SPOTS_2 = "Count.Spots2";
    private static String CELL_ID = "Cell.ID";


    private void runAlgorithm() throws Exception
    {

        TableModel table = new TableModel( outputImageDirectoryFile.toString() );
        table.addValueColumn( DATA_SET_NAME, "FACT" );
        table.addValueColumn( COUNT_SPOTS_1, "NUM"  );
        table.addValueColumn( COUNT_SPOTS_2, "NUM"  );
        String tableFileName = "results.txt";

        // Input parameters
        //
        String inputDirectory = inputImageFile.getParent();
        String inputFile = inputImageFile.getName();
        String outputDirectory = outputImageDirectoryFile.toString();


        // Workflow
        //
        int iDataSet = addNewDataSetToTable( table, inputFile );

        // Open Image
        //
        ImagePlus inputImp = getInputImage( inputDirectory, inputFile );
        int frame = 1;

        // Segment nuclei
        //
        ImageInt binnedNucleusLabelMask = getBinnedNucleusLabelMask( inputFile, outputDirectory, inputImp, frame );

        // Segment cells
        //
        ImageInt cellLabelMask = getCellLabelMask( inputFile, outputDirectory, inputImp, frame, binnedNucleusLabelMask );

        // Segment spots
        //
        SpotSegmenterSettings spotSegmenterSettings = getSpotSegmenterSettings();
        SpotSegmenter spotSegmenter = new SpotSegmenter( spotSegmenterSettings );

        ArrayList< ImageInt > spotsLabelMasks = new ArrayList<>(  );

        // Spots 1
        //
        spotsLabelMasks.add(
                getSpotsLabelMask(
                    table, inputFile, outputDirectory, iDataSet, inputImp, frame, spotSegmenter, spots1Channel, COUNT_SPOTS_1 )
        );

        // Spots 2
        //
        spotsLabelMasks.add(
                getSpotsLabelMask(
                    table, inputFile, outputDirectory, iDataSet, inputImp, frame, spotSegmenter, spots2Channel, COUNT_SPOTS_2 )
        );

        // Count spots per cell
        //
        spotsLabelMasks.get( 0 ).getImagePlus().show();
        cellLabelMask.getImagePlus().show();

        Objects3DPopulation cellObjectsPopulation = new Objects3DPopulation( cellLabelMask, 0 );

        for ( ImageInt spotsLabelMask : spotsLabelMasks )
        {
            Objects3DPopulation spotObjectsPopulation = new Objects3DPopulation( spotsLabelMask, 0 );
            logService.info( "Analysing " + spotObjectsPopulation.getNbObjects() + " spots..." );

            Objects3DPopulationColocalisation colocalisation = new Objects3DPopulationColocalisation( cellObjectsPopulation, spotObjectsPopulation );

            int numSpotsColocalisingTotal  = 0;
            for ( int iCell = 0; iCell < cellObjectsPopulation.getNbObjects(); ++iCell )
            {
                Object3D cell = cellObjectsPopulation.getObject( iCell );
                int dummy = colocalisation.getColocRaw(0, 0 ); // just for initialisation (due to bug)
                ArrayList< PairColocalisation> pairColocalisations = colocalisation.getObject1ColocalisationPairs( cell );
                numSpotsColocalisingTotal  += pairColocalisations.size();
                logService.info( "Cell " + iCell + " has " + pairColocalisations.size() + " spots" );
                // TODO: store values in a cell-based table
            }
            logService.info( "Total number of colocalising spots: " +  numSpotsColocalisingTotal );
        }

        // Count colocalising spots per cell
        //

        // TODO



        // Save table
        //
        table.writeNewFile(  tableFileName, true );
        logService.info( "Saved table file: " + table.getRootPath() + File.separator + tableFileName);


    }

    private ImagePlus getInputImage( String inputDirectory, String inputFile )
    {
        String inputPath = inputDirectory + File.separator + inputFile;

        logService.info("Opening: " + inputPath );

        ImagePlus inputImp = null;

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
        }

        nx = inputImp.getWidth();
        ny = inputImp.getHeight();
        nz = inputImp.getNSlices();

        return inputImp;
    }

    private int addNewDataSetToTable( TableModel table, String inputFile ) throws Exception
    {
        table.addRow();
        int iDataSet = table.getRowCount() - 1;
        table.setFactorValue( inputFile, iDataSet, DATA_SET_NAME );
        return iDataSet;
    }

    private ImageInt getSpotsLabelMask( TableModel table,
                                       String inputFile,
                                       String outputDirectory,
                                       int iDataSet,
                                       ImagePlus inputImp,
                                       int frame,
                                       SpotSegmenter spotSegmenter,
                                       int spotsChannel,
                                       String spotsColumnName ) throws Exception
    {
        logService.info( "Segmenting spots in channel " + spotsChannel + "..." );

        ImageHandler spotsImage = getByteImageHandler( inputImp, spotsChannel, frame );
        ImageInt spotsLabelMask = spotSegmenter.segment( spotsImage );

        String outputFile = inputFile + "--spotsChannel" + spotsChannel + "-LabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( spotsLabelMask, outputDirectory, outputFile );
        logService.info( "Saved image file: " + outputDirectory + File.separator + outputFile );

        int numSpots = new Objects3DPopulation( spotsLabelMask, 0 ).getNbObjects();
        table.setNumericValue( numSpots, iDataSet, spotsColumnName );
        logService.info( "Number of spots in channel " + spotsChannel + " is " + numSpots );

        return spotsLabelMask;
    }

    private void logCount( ImageInt labelMask, String s )
    {
        //boundaries in watershed image have value 1 and must be ignored as well
        int numCells = labelMask.getUniqueValues().size() - 1;
        logService.info( s + numCells );
    }

    private ImageInt getBinnedNucleusLabelMask( String inputFile, String outputDirectory, ImagePlus inputImp, int frame )
    {
        logService.info("Segmenting nuclei..." );

        ImageHandler binnedNucleusImage = getBinnedByteImageHandler( inputImp, nucleusChannel, binX, binY, binZ, frame );

        NucleusSegmenterSettings nucleusSegmenterSettings = new NucleusSegmenterSettings();
        nucleusSegmenterSettings.smooth = false;
        nucleusSegmenterSettings.minVolume = 10;
        nucleusSegmenterSettings.threshold = 20;
        NucleusSegmenter nucleusSegmenter = new NucleusSegmenter( nucleusSegmenterSettings );

        ImageInt binnedNucleusLabelMask = (ImageInt) nucleusSegmenter.segment( binnedNucleusImage );

        logCount( binnedNucleusLabelMask, "Number of nuclei: " );

        // TODO: bug in resample! it also resamples the original => update maven!!
        //ImageHandler nucleusLabelMask = binnedNucleusLabelMask.resample( nx, ny, nz, ImageProcessor.NEAREST_NEIGHBOR );
        //String outputFile = inputFile + "--nuclei-LabelMask.jpg";
        //OutputImageCreator.saveLabelMaskAsMaximumProjection( nucleusLabelMask, outputDirectory, outputFile );

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

        ImageInt binnedCellLabelMask = (ImageInt) cellSegmenter.segment( binnedCellImage, binnedNucleusLabelMask );
        ImageInt cellLabelMask = binnedCellLabelMask.resample( nx, ny, nz, ImageProcessor.NEAREST_NEIGHBOR );

        logCount( cellLabelMask, "Number of cells: " );

        String outputFile = inputFile + "--cells-LabelMask.jpg";
        OutputImageCreator.saveLabelMaskAsMaximumProjection( cellLabelMask, outputDirectory, outputFile );

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