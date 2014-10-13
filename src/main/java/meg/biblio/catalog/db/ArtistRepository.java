package meg.biblio.catalog.db;
import meg.biblio.catalog.db.dao.ArtistDao;

import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = ArtistDao.class)
public interface ArtistRepository {
}
