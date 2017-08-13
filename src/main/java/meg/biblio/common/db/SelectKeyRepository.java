package meg.biblio.common.db;

import meg.biblio.common.db.dao.SelectKeyDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectKeyRepository extends JpaRepository<SelectKeyDao, Long> {
}
