package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.ClassificationDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassificationRepository extends JpaRepository<ClassificationDao, Long> {

    public List<ClassificationDao> findByClientidAndLanguage(Long clientid, String language);


}
