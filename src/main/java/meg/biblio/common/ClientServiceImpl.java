package meg.biblio.common;

import java.security.Principal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import meg.biblio.catalog.Classifier;
import meg.biblio.common.db.ClientRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.tools.imp.FileConfig;
import meg.tools.imp.MapConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

	@Autowired
	ClientRepository clientRepo;

	@Autowired
	LoginService loginService;

	@Autowired
	AppSettingService settingService;

	@PersistenceContext
	private EntityManager entityManager;



	@Override
	public Classifier getClassifierForClient(Long clientkey)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		ClientDao client = getClientForKey(clientkey);
		String classifierclass = client.getClassifyimplementation();

		// currently the property value - later to read from db
		Class clazz = Class.forName(classifierclass);
		return (Classifier) clazz.newInstance();
	}

	public FileConfig getFileConfigForClient(Long clientkey)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		ClientDao client = getClientForKey(clientkey);
		String fileconfig = client.getImportfileconfig();
		// no real client key (yet) so just use defaults
		Class clazz = Class.forName(fileconfig);
		return (FileConfig) clazz.newInstance();

	}

	public MapConfig getMapConfigForClient(Long clientkey)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
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

	@Override
	public Long getAndIncrementLastBookNr(Long clientkey) {
		// get client
		ClientDao client = getClientForKey(clientkey);
		// read lastbooknr
		Long maxbook = client.getLastBookNr();
		// update client
		Query query = entityManager
				.createQuery("Update ClientDao c set lastBookNr = lastBookNr + 1");
		int deletedCount = query.executeUpdate();

		return maxbook;
	}

}
