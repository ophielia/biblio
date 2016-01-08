package meg.biblio.common;

import java.util.List;



public interface CacheService {

	public final static class CodeTag {
		public static final String CustomBarcodes = "cust_barcodes";
	}
	
	public List<String> getValidCacheAsList(String username, String cachetag);
	
	public void clearExpiredCache();


	void saveValueInCache(String username, String cachetag, String name,
			String value, Long validfor);

	void clearUserCacheForTag(String username, String cachetag);

	void deleteValueFromCache(String username, String cachetag, String name,
			String value); 

}
