package meg.biblio.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

    @Value("${biblio.defaultclient}")
    private Long defaultkey;
	
	@Override
	public Long getCurrentClientKey() {
		// returns default coded in properties. For development, or single user systems
		return defaultkey;
	}

}
