package meg.biblio.common;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.BarcodeSheet;

import java.util.Locale;


public interface BarcodeService {

    public final static class CodeType {
        public static final String BOOK = "B";
        public static final String PERSON = "A";
    }

    public final static String codecountlkup = "codecount";


    BarcodeSheet assembleBarcodeSheetForClass(Long classId, Long clientid, Locale locale);

    BarcodeSheet assembleBarcodeSheetForBooks(int count, int startcode,
                                              int offset, Long clientkey, Locale locale);

    BarcodeSheet assembleBarcodeSheetForBooksFromCache(String username,
                                                       int offset, Long clientkey, Locale locale);

    String getBookBarcodeForClientid(ClientDao client, String clientbookid);


    BarcodeSheet assembleBarcodeSheetForClassFromCache(Long classId, String username,
                                                       Long clientid, int offset, Locale locale);


}
