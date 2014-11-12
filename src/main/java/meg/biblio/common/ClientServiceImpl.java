package meg.biblio.common;

import meg.biblio.catalog.Classifier;
import meg.tools.imp.FileConfig;
import meg.tools.imp.MapConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

    @Value("${biblio.defaultclient}")
    private Long defaultkey;


	@Value("${biblio.import.mapconfig}")
	private String mapconfig;

	@Value("${biblio.import.fileconfig}")
	private String fileconfig;  
	
	@Value("${biblio.classify.implementation}")
	private String classifierclass;  	
    
	@Override
	public Long getCurrentClientKey() {
		// returns default coded in properties. For development, or single user systems
		return defaultkey;
	}

	@Override
	public Classifier getClassifierForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		// currently the property value - later to read from db
		Class clazz = Class.forName(classifierclass);
		return (Classifier) clazz.newInstance();
	}

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
