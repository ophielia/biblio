package meg.biblio.catalog.db;
import java.util.List;

import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SubjectDao.class)
public interface SubjectRepository {
	
	@Query("select r from SubjectDao as r where lower(trim(r.listing)) = :text")
	List<SubjectDao> findSubjectByText(@Param("text") String text);	
	
}
