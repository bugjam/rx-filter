package dk.e_dreams.rx_filter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

public class Service {

	/**
	 * This method wraps the "backend call"
	 * The generator emits each chunk of data to onNext and returns the 
	 * continuation key to be used in the next call
	 * 
	 * @param fromKey Continuation key for the first call
	 * @return A stream of Chunks
	 */
	private Flowable<Chunk> getData(int fromKey) {
		return Flowable.generate(
				() -> fromKey,
				(key, emitter) -> {
					if(key>1000) emitter.onComplete();
					emitter.onNext(new Chunk(key));
					return key+1;
				}
			);
	}
	
	public static IntPredicate intervalFilter(int min, int max) {
		return n -> { return n>=min && n<=max; };
	}
	
	/**
	 * This method converts a Chunk into a stream of data
	 * Each data item is wrapped in a ItemAndKeys instance to propagate the continuation keys
	 * 
	 * @param c The Chunk to be streamed
	 * @return A stream of data items
	 */
	private Flowable<ItemAndKeys<Integer>> streamChunk(Chunk c) {
		if(c.size()==0) return Flowable.empty();
		BiFunction<Integer,Integer,ItemAndKeys<Integer>> zipper = new BiFunction<Integer,Integer,ItemAndKeys<Integer>>() {
				@Override
				public ItemAndKeys<Integer> apply(Integer item, Integer i) throws Exception {
					return new ItemAndKeys<Integer>(item, c.previousKey(), i);
				}
			};
		List<Integer> items = c.values();
		Integer lastItem = items.get(items.size()-1);
		// Each item is paired with the current continuation key and its position in the chunk
		// except the last item which needs the continuation key for the next chunk
		return Flowable.fromIterable(items).zipWith(Flowable.range(1, c.size()-1),zipper)
				.concatWith(Flowable.just(new ItemAndKeys<Integer>(lastItem,c.nextKey(),0)));
	}

	/**
	 * This method drives the process of fetching enough data to satisfy the client request
	 * 
	 * @param p Filtering predicate. Data items which do not satisfy this predicate will not be returned.
	 * @param continuationKey Continuation key from previous invocation (optional)
	 * @param size Maximum number of items to return
	 * @return A list of data items, wrapped in a ItemAndKeys item to return the continuation key for the next invocation
	 */
	public ItemAndKeys<List<Integer>> getFilteredData(IntPredicate p, String continuationKey, int size) {
		// Parse the continuation key
		ItemAndKeys<Object> start = new ItemAndKeys<Object>(null);
		start.parseKey(continuationKey);
		int key1 = 0;
		try { key1 = Integer.parseInt(start.key1); } catch(NumberFormatException x) {};
		
		// Construct a stream which repeatedly calls "the backend" until we have enough data
		Flowable<ItemAndKeys<Integer>> data = getData(key1)
				// Merge the data chunks into one stream of data
				.flatMap(c -> streamChunk(c)) 
				// Skip items from the first chunk if they were already returned in the previous call
				.skip(start!=null && start.key2!=null ? start.key2 : 0) 
				// Filter by the given predicate
				.filter(iak -> p.test(iak.item))
				// Return (up to) the requested number of items
				.take(size);
		
		// Strip off the continuation keys to get the real data items
		Flowable<Integer> dataItems = data.map(iak -> iak.item);
		ItemAndKeys<List<Integer>> result = new ItemAndKeys<List<Integer>>(dataItems.toList().blockingGet());
		
		// Grab the continuation key from the last data item and return it to the client
		if(result.item.size()==size) {
			try {
				String lastContinuationKey = data.lastOrError().to((Single<ItemAndKeys<Integer>> iak) -> iak.blockingGet().getKey());
				result.parseKey(lastContinuationKey);
			} catch (NoSuchElementException x ) { /* Ignore */ }
		} else {
			// no continuation key set as we are at the end of the stream
		}
			
		// Phew, all done
		return result;
	}
	
}
