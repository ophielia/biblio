package meg.biblio.common.db;

import meg.biblio.common.db.dao.ClientDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientDao, Long> {
}
