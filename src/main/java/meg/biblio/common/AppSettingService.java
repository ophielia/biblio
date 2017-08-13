package meg.biblio.common;


public interface AppSettingService {

    Long getSettingAsLong(String key);

    String getSettingAsString(String key);

    Boolean getSettingAsBoolean(String key);

    Integer getSettingAsInteger(String key);
}
