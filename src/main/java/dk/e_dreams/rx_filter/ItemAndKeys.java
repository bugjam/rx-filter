package dk.e_dreams.rx_filter;

/**
 * This class holds a data item together with a continuation key.
 * The key has two parts: The first part stores the "backend" continuation key.
 * The second part contains the number of "used" items in the latest chunk.
 * @author Tore
 *
 * @param <T> Type of the data item
 */
public class ItemAndKeys<T> {

	public static String SEPARATOR = "#";
	public T item;
	public String key1;
	public Integer key2;

	public ItemAndKeys(T i) {
		item = i;
	}

	public ItemAndKeys(T i, String k1, Integer k2) {
		item = i;
		key1 = k1;
		key2 = k2;
	}

	public String getKey() {
		String key = SEPARATOR;
		if (key1 != null)
			key = key1 + key;
		if (key2 != null)
			key = key + key2;
		return key.equals(SEPARATOR) ? null : key;
	}

	public void parseKey(String key) {
		int sep = key.indexOf(SEPARATOR);
		if (sep>-1) {
			key1 = key.substring(0, sep);
			try {
				key2 = new Integer(key.substring(sep+1));
			} catch (NumberFormatException x) {
				key2 = null;
			}
		}
	}
}
