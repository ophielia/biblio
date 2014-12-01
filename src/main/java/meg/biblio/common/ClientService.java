package meg.biblio.common;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.Classifier;
import meg.biblio.common.db.dao.ClientDao;
import meg.tools.imp.FileConfig;
import meg.tools.imp.MapConfig;

public interface ClientService {

	public Long getCurrentClientKey();

	public Classifier getClassifierForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException;

	public FileConfig getFileConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException;
	
	public MapConfig getMapConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException;

	public ClientDao getCurrentClient(HttpServletRequest httpServletRequest);

	ClientDao getClientForKey(Long key);
}
