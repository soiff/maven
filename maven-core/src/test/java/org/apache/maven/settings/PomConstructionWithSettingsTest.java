package org.apache.maven.settings;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Profile;
import org.apache.maven.project.DefaultProjectBuilder;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.harness.PomTestWrapper;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PomConstructionWithSettingsTest     
	extends PlexusTestCase
{
    private static String BASE_DIR = "src/test";

    private static String BASE_POM_DIR = BASE_DIR + "/resources-settings";

    private DefaultProjectBuilder projectBuilder;

    private File testDirectory;

    protected void setUp()
        throws Exception
    {
        testDirectory = new File( getBasedir(), BASE_POM_DIR );
        projectBuilder = (DefaultProjectBuilder) lookup( ProjectBuilder.class );
    }

    @Override
    protected void tearDown() throws Exception {
            projectBuilder = null;

            super.tearDown();
    }
    
    public void testSettingsNoPom() throws Exception
    {
    	PomTestWrapper pom = buildPom( "settings-no-pom" );
    	assertEquals( "local-profile-prop-value", pom.getValue( "properties/local-profile-prop" ) );
    }
    
    /**MNG-4107 */
    public void testPomAndSettingsInterpolation() throws Exception
    {
    	PomTestWrapper pom = buildPom( "test-pom-and-settings-interpolation" );
    	assertEquals("applied", pom.getValue( "properties/settingsProfile" ) );
    	assertEquals("applied", pom.getValue( "properties/pomProfile" ) );
    	assertEquals("settings", pom.getValue( "properties/pomVsSettings" ) );
    	assertEquals("settings", pom.getValue( "properties/pomVsSettingsInterpolated" ) );
    }    
    
    /**MNG-4107 */
    public void testRepositories() throws Exception
    {
    	PomTestWrapper pom = buildPom( "repositories" );
    	assertEquals("maven-core-it-0", pom.getValue( "repositories[1]/id" ));
    }       

    private PomTestWrapper buildPom( String pomPath )
        throws Exception
	{
	    File pomFile = new File( testDirectory + File.separator + pomPath , "pom.xml" );
	    File settingsFile = new File( testDirectory + File.separator + pomPath, "settings.xml" );	    
	    Settings settings = readSettingsFile(settingsFile);
	    	    
        ProjectBuildingRequest config = new DefaultProjectBuildingRequest();
	    
	    for ( org.apache.maven.settings.Profile rawProfile : settings.getProfiles() )
	    {
	        Profile profile = SettingsUtils.convertFromSettingsProfile( rawProfile );
	        config.addProfile( profile );
	    }    
	    
        String localRepoUrl = System.getProperty( "maven.repo.local", System.getProperty( "user.home" ) + "/.m2/repository" );
        localRepoUrl = "file://" + localRepoUrl;
        config.setLocalRepository( new DefaultArtifactRepository( "local", localRepoUrl, new DefaultRepositoryLayout() ) );
        config.setActiveProfileIds( settings.getActiveProfiles() );
        
        return new PomTestWrapper( pomFile, projectBuilder.build( pomFile, config ) );        
	}  
    
    private static Settings readSettingsFile(File settingsFile) 
    	throws IOException, XmlPullParserException
    {
        Settings settings = null;

        Reader reader = null;

        try
        {
            reader = ReaderFactory.newXmlReader( settingsFile );

            SettingsXpp3Reader modelReader = new SettingsXpp3Reader();

            settings = modelReader.read( reader );
        }
        finally
        {
            IOUtil.close( reader );
        }

        return settings;    	
    }
}