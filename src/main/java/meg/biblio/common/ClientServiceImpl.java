package meg.biblio.common;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import meg.biblio.catalog.Classifier;
import meg.biblio.common.db.ClientRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.tools.imp.FileConfig;
import meg.tools.imp.MapConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

	@Autowired
	ClientRepository clientRepo;

    @Value("${biblio.defaultclient}")
    private Long defaultkey;

@Autowired
LoginService loginService;




	@Override
	public Long getCurrentClientKey(HttpServletRequest httpServletRequest) {
		// get principal

		// get logindao

		// get client

		// return clientkey

		// returns default coded in properties. For development, or single user systems
		return defaultkey;
	}


	@Override
	public Classifier getClassifierForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		ClientDao client = getClientForKey(clientkey);
		String classifierclass = client.getClassifyimplementation();

		// currently the property value - later to read from db
		Class clazz = Class.forName(classifierclass);
		return (Classifier) clazz.newInstance();
	}

	public FileConfig getFileConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClientDao client = getClientForKey(clientkey);
		String fileconfig = client.getImportfileconfig();
		// no real client key (yet) so just use defaults
		Class clazz = Class.forName(fileconfig);
		return (FileConfig) clazz.newInstance();

	}

	public MapConfig getMapConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClientDao client = getClientForKey(clientkey);
		String mapconfig = client.getImportmapconfig();

		Class clazz = Class.forName(mapconfig);
		return (MapConfig) clazz.newInstance();

	}

	@Override
	public ClientDao getClientForKey(Long key) {
		return clientRepo.findOne(key);
	}

	@Override
	public ClientDao getCurrentClient(HttpServletRequest httpServletRequest) {
		Long clientkey = defaultkey;
		ClientDao client = clientRepo.findOne(clientkey);
		return client;
	}


	@Override
	public ClientDao getCurrentClient(Principal principal) {
		String username = principal.getName();
		ClientDao client = loginService.getClientForUsername(username); 
		
		return client;
	}


	@Override
	public Long getTestClientId() {
		// make strong for live....
		return 1L;
	}

}
