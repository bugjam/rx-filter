package dk.e_dreams.rx_filter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This class serves as "the backend" in our experiment
 * It represents a chunk of data (=20 random integers) and a continuation key for getting the next chunk
 * @author Tore
 *
 */
public class Chunk {

	private Integer[] values;
	private int key;
	private static Random random = new Random();
	private static int nextKey = 0;
	
	public Chunk(int withKey) {
		values = new Integer[20];
		for(int i=0;i<20;i++) {
			values[i] = random.nextInt(2001)-1000;
		}
		key = withKey;
	}
	
	public Chunk() {
		this(nextKey++);
	}
	
	public int get(int i) {
		return values[i];
	}
	
	public int size() {
		return values.length;
	}
	
	public List<Integer> values() {
		return Arrays.asList(values);
	}
	
	/**
	 * @return The key used to get the current chunk
	 */
	public String previousKey() {
		return String.valueOf(key);
	}
	
	/**
	 * @return The key needed to get the next chunk
	 */
	public String nextKey() {
		return String.valueOf(key+1);
	}
}
