package meg.tools.imp;

import java.util.List;

import meg.tools.imp.utils.FieldMapping;

public interface MapConfig {

	String getDestinationClassName();

	List<FieldMapping> getMappings();

	String getHelperClassName();

}
