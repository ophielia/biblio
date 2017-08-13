package meg.biblio.common;

import meg.biblio.catalog.Classifier;
import meg.biblio.common.db.dao.ClientDao;
import meg.tools.imp.FileConfig;
import meg.tools.imp.MapConfig;

import java.security.Principal;
import java.util.List;

public interface ClientService {


    public Classifier getClassifierForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    public FileConfig getFileConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    public MapConfig getMapConfigForClient(Long clientkey) throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    public ClientDao getCurrentClient(Principal principal);

    ClientDao getClientForKey(Long key);

    public Long getTestClientId();


    public List<ClientDao> getAllClients();
}
