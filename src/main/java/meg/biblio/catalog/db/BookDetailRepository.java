package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.BookDetailDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetailDao, Long> {

}
