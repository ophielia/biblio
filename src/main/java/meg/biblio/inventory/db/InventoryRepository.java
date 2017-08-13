package meg.biblio.inventory.db;

import meg.biblio.inventory.db.dao.InventoryDao;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryDao, Long> {

    @Query("select r from InventoryDao as r where r.enddate is null and r.clientid = :clientid")
    InventoryDao getCurrentInventoryForClient(@Param("clientid") Long clientid);

    @Query("select r from InventoryDao as r where r.clientid = :clientid")
    List<InventoryDao> getInventoryListForClient(@Param("clientid") Long clientid, Sort sort);

    @Query("select r from InventoryDao as r where r.clientid = :clientid and r.enddate is not null")
    List<InventoryDao> getPreviousInventoriesForClient(@Param("clientid") Long clientid, Sort sort);

}
