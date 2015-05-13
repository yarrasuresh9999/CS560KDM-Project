import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import Jama.Matrix;

public class AuthoritiesHubs {
	
	LinkAnalysis linkAnalysis = new LinkAnalysis();
	ArrayList<Integer> baseSet = new ArrayList<Integer>();               // list having baseSet values
	ArrayList<Integer> rootSet = new ArrayList<Integer>();				 // list having rootSet values
	HashMap<Integer, String> returnResults = new LinkedHashMap<Integer, String>(); // hashMap which stores top 10 authorities and top 10 hubs

	public Matrix computeAdjacencyMatrix(List<Entry<Integer, Double>> resultsOfQuery) throws CorruptIndexException, IOException {
		int count = 0;
		Long starttimeB=System.nanoTime();
		for(Map.Entry<Integer, Double> entry : resultsOfQuery) 
		{
			if(count<10) 	
			{
				rootSet.add(entry.getKey());									 // all top k results are added to root set
			
			if(!(baseSet.contains(entry.getKey()))) baseSet.add(entry.getKey());								 // all top k results are added to base set
	
			int[] links = linkAnalysis.getLinks(entry.getKey());
			for(int link:links)
				{
					if(!(baseSet.contains(link))) baseSet.add(link);             // all links of results are added to base set
				}

			int[] citations = linkAnalysis.getCitations(entry.getKey());
			for(int citation:citations)
				{
					if(!(baseSet.contains(citation))) baseSet.add(citation);      // all citations of results are added to base set
				}
			
			count++;
			}
			else 
				break;	
		}
		
		System.out.println("Base Set Calc in "+(double)(System.nanoTime()-starttimeB)/1000000+" milli seconds");
		System.out.println("baseSet size is"+baseSet.size());			

		Matrix adjacencyMatrix = new Matrix(baseSet.size(), baseSet.size());      // new adjacency Matrix
		
		for(int i=0; i<rootSet.size(); i++)										  // adding values to adjacency matrix
		{
			int result=rootSet.get(i);
			
			int[] links = linkAnalysis.getLinks(result);
			for(int link:links)
			{
					  adjacencyMatrix.set(baseSet.indexOf(result),baseSet.indexOf(link),1);		// if there is a link from row(page) i to colmn(page) index then corresponding matrix value is 1
			}
			
			int[] citations = linkAnalysis.getCitations(result);
			for(int citation:citations)
			{
					adjacencyMatrix.set(baseSet.indexOf(citation),baseSet.indexOf(result),1);
			}
		}
		return adjacencyMatrix;
	}
	
	public LinkedHashMap<Integer, String> computeAuthorityMatrix(List<Entry<Integer, Double>> resultsOfQuery) throws Exception {
		System.out.println("Calculating A/H ");
		
		Long starttimeA=System.nanoTime();
		Matrix adjacencyMatrix = computeAdjacencyMatrix(resultsOfQuery);
		System.out.println("Adjacency Matrix caluculated in "+(double)(System.nanoTime()-starttimeA)/1000000+" milli seconds");
		Matrix adjMatrixTranspose = adjacencyMatrix.transpose(); // transpose of adjacency matrix
		
		//creating authorities and hubs
		
		 Matrix auth = new Matrix(baseSet.size(), 1,1);            // new authority matrix
		 Matrix hub = new Matrix(baseSet.size(), 1, 1);			 // new hub matrix
		 System.out.println("hub"+hub.get(0, 0));

		 Matrix newAuth = new Matrix(baseSet.size(),1);
		 Matrix newHub = new Matrix(baseSet.size(), 1);
		 int iterations = 0;
		do
		 {	 
			 newAuth=auth;
		     auth = adjMatrixTranspose.times(hub);
			 double normF = auth.normF();
			 auth = auth.times((double)1/normF);
			
			 newHub=hub;
			 hub = adjacencyMatrix.times(auth);
			 double norm = hub.normF();
			 hub = hub.times((double)1/norm);
			 iterations++;
		 }
		while(match(auth, newAuth) || match(hub, newHub));
		System.out.println("TOTAL Iterations : "+iterations);
		
		HashMap<Integer,Double> authhashMap = matrixToHashMap(auth);
		List<Entry<Integer, Double>> sortAuthValuesResults = sortHashMapByValues(authhashMap); //sorting hash map
		System.out.println("Top authorities are ");
		topResults(sortAuthValuesResults);
		 
		HashMap<Integer,Double> hubHashMap = matrixToHashMap(hub);
	    List<Entry<Integer, Double>> sortHubValuesResults = sortHashMapByValues(hubHashMap); //sorting hashmap
	    System.out.println("Top hubs are ");
	    topResults(sortHubValuesResults);
	    System.out.println(" size "+returnResults.size());
	    return (LinkedHashMap<Integer, String>) returnResults;
		}
	
	private boolean match(Matrix oldMatrix, Matrix newMatrix)    // match function helps to know upto which point convergence takes place
	{
		if(oldMatrix.equals(null)||newMatrix.equals(null)) return true;
		Matrix difference = oldMatrix.minus(newMatrix);
		boolean check = false;
		for(int r=0; r<difference.getRowDimension(); r++)
		 {
			if(Math.abs(difference.get(r, 0)) > 0.00002) 
			{
				check=true;
				break;
			}
		 }
			return check;
	}
	
	public void topResults(List<Entry<Integer, Double>> sortAuthValuesResults) throws Exception {
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		int count = 1;
		for(Map.Entry<Integer, Double> entry:sortAuthValuesResults)    // Displaying top 10 authorities
		{
			if(count<11) 
				{ 
					String d_url = r.document(entry.getKey()).getFieldable("path").stringValue().replace("%%", "/");
					returnResults.put(entry.getKey(),d_url);
					System.out.println(count+" -> "+entry.getKey()+" value : "+entry.getValue()); 
					count++;
				}
			else break;
		}
	}

	public HashMap<Integer, Double> matrixToHashMap(Matrix matrix) {
		 
		 HashMap<Integer,Double> hashMap = new HashMap<Integer,Double>();  
		 for(int i=0;i<baseSet.size();i++)								      //Converting matrix to hashmap									
			 {
			   hashMap.put(baseSet.get(i), matrix.get(i, 0));
			 }
		 return hashMap;
	}
	
	public List<Entry<Integer, Double>> sortHashMapByValues(HashMap<Integer, Double> passedMap) // code to sort results
	{
        Set<Entry<Integer, Double>> set = passedMap.entrySet();
        List<Entry<Integer, Double>> list = new ArrayList<Entry<Integer, Double>>(set);
       
        Collections.sort( list, new Comparator<Map.Entry<Integer, Double>>()
        		{
            		public int compare( Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2 )
            			{
            				return (o2.getValue()).compareTo( o1.getValue() );
            			}
        		} 
        );
      return list;
	}
	
}