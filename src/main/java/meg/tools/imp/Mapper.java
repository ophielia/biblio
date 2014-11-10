package meg.tools.imp;

import meg.tools.imp.utils.Placeholder;



/**
 * The implementations of the Mapper Interface are responsible for Mapping
 * Placeholder objects to Business Logic objects. Current thinking is that
 * Mapping will be made up of some tasks which can be done automatically (1 to
 * 1, no "programming" logic necessary), and some which will need a little more
 * finesse. Those requiring more finesse will have their own implementation of a
 * Mapper interface.
 * 
 * @author maggie
 * 
 */
public interface Mapper {

	Object mapObject(Placeholder placeholder);

}
