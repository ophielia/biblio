package meg.tools.imp;

import meg.tools.imp.utils.DefaultMapper;


public class MapperFactory {

	public static Mapper getMapper(MapConfig mapconfig) {

		// currently as default, the SocGenMapper. Later, the Mapper Helper
		// class will be read out of the MapConfig object.
		// TODO - make generic
		return new DefaultMapper(mapconfig);
	}

}
