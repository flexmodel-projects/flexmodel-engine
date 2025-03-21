package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.StringHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class maps a type to names.  Associations may be marked with a capacity. Calling the get()
 * method with a type and actual size n will return  the associated name with smallest capacity >= n,
 * if available and an unmarked default type otherwise.
 * Eg, setting
 * <pre>
 * 	names.put( type,        "Text" );
 * 	names.put( type,   255, "VARCHAR($l)" );
 * 	names.put( type, 65534, "LONGVARCHAR($l)" );
 * </pre>
 * will give you back the following:
 * <pre>
 *  names.get( type )         // --> "Text" (default)
 *  names.get( type,    100 ) // --> "VARCHAR(100)" (100 is in [0:255])
 *  names.get( type,   1000 ) // --> "LONGVARCHAR(1000)" (1000 is in [256:65534])
 *  names.get( type, 100000 ) // --> "Text" (default)
 * </pre>
 * On the other hand, simply putting
 * <pre>
 * 	names.put( type, "VARCHAR($l)" );
 * </pre>
 * would result in
 * <pre>
 *  names.get( type )        // --> "VARCHAR($l)" (will cause trouble)
 *  names.get( type, 100 )   // --> "VARCHAR(100)"
 *  names.get( type, 10000 ) // --> "VARCHAR(10000)"
 * </pre>
 *
 * @author cjbi@outlook.com
 */
final class TypeNames {
  /**
   * Holds default type mappings for a typeCode.  This is the non-sized mapping
   */
  private final Map<Integer, String> defaults = new HashMap<Integer, String>();

  /**
   * Holds the weighted mappings for a typeCode.  The nested map is a TreeMap to sort its contents
   * based on the name (the weighting) to ensure proper iteration ordering during {@link #get(int, long, int, int)}
   */
  private final Map<Integer, Map<Long, String>> weighted = new HashMap<Integer, Map<Long, String>>();

  /**
   * get default type name for specified type
   *
   * @param typeCode the type name
   * @return the default type name associated with specified name
   * @throws DialectException Indicates that no registrations were made for that typeCode
   */
  public String get(final int typeCode) throws DialectException {
    final Integer integer = Integer.valueOf(typeCode);
    final String result = defaults.get(integer);
    if (result == null) {
      throw new DialectException("No Dialect mapping for JDBC type: " + typeCode);
    }
    return result;
  }

  /**
   * get type name for specified type and size
   *
   * @param typeCode  the type name
   * @param size      the SQL length
   * @param scale     the SQL scale
   * @param precision the SQL precision
   * @return the associated name with smallest capacity >= size, if available and the default type name otherwise
   * @throws DialectException Indicates that no registrations were made for that typeCode
   */
  public String get(int typeCode, long size, int precision, int scale) throws DialectException {
    final Integer integer = Integer.valueOf(typeCode);
    final Map<Long, String> map = weighted.get(integer);
    if (map != null && map.size() > 0) {
      // iterate entries ordered by capacity to find first fit
      for (Map.Entry<Long, String> entry : map.entrySet()) {
        if (size <= entry.getKey()) {
          return replace(entry.getValue(), size, precision, scale);
        }
      }
    }

    // if we get here one of 2 things happened:
    //		1) There was no weighted registration for that typeCode
    //		2) There was no weighting whose max capacity was big enough to contain size
    return replace(get(typeCode), size, precision, scale);
  }

  private static String replace(String type, long size, int precision, int scale) {
    type = StringHelper.replaceOnce(type, "$s", Integer.toString(scale));
    type = StringHelper.replaceOnce(type, "$l", Long.toString(size));
    return StringHelper.replaceOnce(type, "$p", Integer.toString(precision));
  }

  /**
   * Register a weighted typeCode mapping
   *
   * @param typeCode the JDBC type code
   * @param capacity The capacity for this weighting
   * @param value    The mapping (type name)
   */
  public void put(int typeCode, long capacity, String value) {
    final Integer integer = Integer.valueOf(typeCode);
    Map<Long, String> map = weighted.get(integer);
    if (map == null) {
      // add new ordered map
      map = new TreeMap<Long, String>();
      weighted.put(integer, map);
    }
    map.put(capacity, value);
  }

  /**
   * Register a default (non-weighted) typeCode mapping
   *
   * @param typeCode the type name
   * @param value    The mapping (type name)
   */
  public void put(int typeCode, String value) {
    final Integer integer = Integer.valueOf(typeCode);
    defaults.put(integer, value);
  }

  /**
   * Check whether or not the provided typeName exists.
   *
   * @param typeName the type name.
   * @return true if the given string has been registered as a type.
   */
  public boolean containsTypeName(final String typeName) {
    return this.defaults.containsValue(typeName);
  }
}
