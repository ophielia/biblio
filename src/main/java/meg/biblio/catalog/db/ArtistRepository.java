package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.ArtistDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistDao, Long> {
}
