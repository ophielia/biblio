package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoundWordsRepository extends JpaRepository<FoundWordsDao, Long> {


    @Modifying
    @Query("delete from FoundWordsDao as r where book = :book")
    void deleteWordsForBook(@Param("book") BookDao book);

    @Modifying
    @Query("delete from FoundWordsDao as r where bookdetail = :detail")
    void deleteWordsForBookDetail(@Param("detail") BookDetailDao detail);

    @Query("select r from FoundWordsDao as r where bookdetail = :detail")
    List<FoundWordsDao> findWordsForBookDetail(@Param("detail") BookDetailDao detail);
}
