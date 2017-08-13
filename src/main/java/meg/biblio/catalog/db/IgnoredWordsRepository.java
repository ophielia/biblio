package meg.biblio.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IgnoredWordsRepository extends JpaRepository<IgnoredWordsDao, Long> {

}
