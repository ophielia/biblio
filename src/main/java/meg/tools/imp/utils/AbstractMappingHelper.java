package meg.tools.imp.utils;


public abstract class AbstractMappingHelper implements MappingHelper {

	public AbstractMappingHelper() {
		// empty no-arg constructor
	}

	/**
	 * Instantiates an Object with a public, no-arg constructor. To instantiate
	 * an Object with a constructor with arguments, override this method in the
	 * specific helper class.
	 */
	public Object instantiateObject(String classname) {
		// get class name
		if (classname == null) {
			// no helper class declared - throw error
			// TODO add error handling here
		}

		try {
			// get class
			Class cls = Class.forName(classname);

			// create destination class
			Object destination = cls.newInstance();

			// return destination class
			return destination;
		} catch (Throwable e) {
			// TODO add error handling here
		}

		// error - return null
		return null;
	}

}
