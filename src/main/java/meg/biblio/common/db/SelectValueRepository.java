package meg.biblio.common.db;
import java.util.List;

import meg.biblio.common.db.dao.SelectValueDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = SelectValueDao.class)
public interface SelectValueRepository {
    public final static String FINDBY_KEYLANGACTIVE = "select v "
    		+ "from SelectValueDao v inner join v.selectkey k "
    		+ "where  k.lookup = :key and v.active = true and v.languagekey=:language";
	
	@Query(FINDBY_KEYLANGACTIVE)
	List<SelectValueDao> findByKeyLanguageDisplay(@Param("key")String key,@Param("language") String language, Sort sort);
	
	@Query("select v from SelectValueDao v inner join v.selectkey k where  k.lookup = :key and v.value = :value and  v.active = true and v.languagekey=:language")
	SelectValueDao findByKeyValueLanguage(@Param("key")String key,@Param("value")String value,@Param("language") String language);
	
}
