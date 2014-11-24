package meg.biblio.catalog.db;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = IgnoredWordsDao.class)
public interface IgnoredWordsRepository {
}
