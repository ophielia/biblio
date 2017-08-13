package meg.biblio.common;

import java.util.List;


public interface CacheService {

    public final static class CodeTag {
        public static final String CustomBarcodes = "cust_barcodes";
        public static final String ClassBarcodes = "class_barcodes";
    }

    public List<String> getValidCacheAsList(String username, String cachetag);

    public List<String> getValidCacheAsList(String username, String cachetag, String name);

    public void clearExpiredCache();


    void saveValueInCache(String username, String cachetag, String name,
                          String value, Long validforminutes);

    void clearUserCacheForTag(String username, String cachetag);

    void deleteValueFromCache(String username, String cachetag, String name,
                              String value);

    public void replaceValuesInCache(String username, String cachetag,
                                     String name, List<String> values, Long minutesvalid);

    public List<Long> getValidCacheAsListofLongs(String username, String cachetag, String name);

}
