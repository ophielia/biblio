package meg.biblio.lending.db;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRecordRepository extends JpaRepository<LoanRecordDao, Long> {


    @Query("select r from LoanRecordDao as r where r.client = :client")
    List<LoanRecordDao> findForClient(@Param("client") ClientDao client);
}
