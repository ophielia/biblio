package meg.tools.imp.utils;


public interface MappingHelper {

	Object instantiateObject(String objectname);

	void doManualMapping(Object mapped, Placeholder placeholder);

}
