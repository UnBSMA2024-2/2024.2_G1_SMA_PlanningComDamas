package puzzle;

import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.types.cms.CreationInfo;

/**
 *  Main for starting the example programmatically.
 *  
 *  To start the example via this Main.java Jadex platform 
 *  as well as examples must be in classpath.
 */
public class Main 
{
	/**
	 *  Start a platform and the example.
	 */
	public static void main(String[] args) 
	{	
		IExternalAccess platform = Starter.createPlatform(PlatformConfigurationHandler.getDefault()).get();
		CreationInfo ci = new CreationInfo().setFilename("puzzle/SokratesAgent.class");
		CreationInfo ci2 = new CreationInfo().setFilename("puzzle/SokratesAgent.class");
		platform.createComponent(ci).get();
		platform.createComponent(ci2).get();
	}
}
