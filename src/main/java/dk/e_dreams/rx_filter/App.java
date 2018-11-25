package dk.e_dreams.rx_filter;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Service s = new Service();
    	String ckey = "";
    	int goal = 100;
    	while(goal>0) {
    		ItemAndKeys<List<Integer>> result = s.getFilteredData(Service.intervalFilter(0, 500), ckey, 30);
    		System.out.println(String.format("Got %d results: ",result.item.size()));
    		for(Integer i : result.item) {
    			System.out.print(i+" ");
    		}
    		System.out.println("\nContinuation key: "+result.getKey());
    		ckey = result.getKey();
    		if(ckey!=null)
    			goal -= result.item.size();
    		else
    			goal = 0;
    	}
     }
}
