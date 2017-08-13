package meg.biblio.common.db;

import meg.biblio.common.db.dao.ImportBookDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportBookRepository extends JpaRepository<ImportBookDao, Long> {
}
