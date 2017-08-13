package meg.biblio.common.db;

import meg.biblio.common.db.dao.AppSettingDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSettingDao, Long> {

    @Query("select r from AppSettingDao as r where r.key= :key")
    AppSettingDao findByKey(@Param("key") String key);
}
