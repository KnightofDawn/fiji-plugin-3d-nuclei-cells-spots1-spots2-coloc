package de.embl.cba.coloc3d.table;

import automic.table.TableModel;

public abstract class Tables
{
    public void createAnalysisTable( String outputDirectory )
    {
        TableModel table = new TableModel( outputDirectory );
        /*
        table.addFileColumns( 'Raw.Stack', 'IMG' )
        table.addFileColumns( 'All.Nuclei.3D', 'ROI' )
        os.mkdir( os.path.join( _analysisPath, 'All.Nuclei.3D' ) )
        table.addFileColumns( 'Single.Nucleus.3D', 'ROI' )
        os.mkdir( os.path.join( _analysisPath, 'Single.Nucleus.3D' ) )
        table.addFileColumns( 'Spots.3D', 'ROI' )
        os.mkdir( os.path.join( _analysisPath, 'Spots.3D' ) )
        table.addFileColumns( 'Spots.Mask', 'IMG' )
        os.mkdir( os.path.join( _analysisPath, 'Spots.Mask' ) )
        table.addValueColumn( 'Nuclei.Count', 'NUM' )
        table.addValueColumn( 'Spot.Count', 'NUM' )
        */
    }

}
