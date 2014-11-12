package meg.biblio.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import meg.tools.imp.MapConfig;
import meg.tools.imp.FileConfig;

@Component
public class ImportConfigManager {

	
    @Value("${biblio.import.mapconfig}")
    private String mapconfig;

    @Value("${biblio.import.fileconfig}")
    private String fileconfig;
	
	
	public FileConfig getFileConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// no real client key (yet) so just use defaults
		Class clazz = Class.forName(fileconfig);
		return (FileConfig) clazz.newInstance();

	}
	
	public MapConfig getMapConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class clazz = Class.forName(mapconfig);
		return (MapConfig) clazz.newInstance();

	}


}
