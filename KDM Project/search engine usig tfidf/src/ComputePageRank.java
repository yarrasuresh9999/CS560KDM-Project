import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

public class ComputePageRank {
	
	private int totalDoc = 25054;
	private double prob[] = new double[totalDoc];
	private double newProb[] = new double[totalDoc]; 
	
	public double[] computePageRank() throws CorruptIndexException, IOException 
	{
		double c = 0.8;
		LinkAnalysis linkAnalysis = new LinkAnalysis();

		//finding Z
		double z[] = new double[totalDoc];
		
		for(int i=0; i< totalDoc; i++) {
			
			if(linkAnalysis.getLinks(i).length==0) 
				 z[i]=(double)(1.0/25054.0);             // if there are no links from i(i.e. entire colm are zeros in M) then z[i]=1/n
			else
				  z[i]=0;								 // else we place 0 
		}
		
		for(int i=0; i< totalDoc; i++) {
			
				prob[i]=(double)(1.0/25054.0);             // initializing the probability matrix
		}
		
		int iterations;

		for(iterations = 0; iterations < 100; iterations++)
		{ 
			for(int i=0;i<totalDoc;i++)
				{
					double m[] = new double[totalDoc];   // new m matrix for this iteration
					
					int[] citations = linkAnalysis.getCitations(i);    // to build a row we want citations in that row; 
					
																	   //because in M matrix a row has a value in its citations colmn only*/
					for(int citation : citations) {                     
						m[citation] = (double)(1.0/linkAnalysis.getLinks(citation).length); // to find value of a row in citations colmn, it is 1/total no. of links
					}
					
					double rowSum = 0;										// rowSum will have total sum of elements in a row multiplied with colmn of prob matrix
					
					for(int j=0;j<totalDoc;j++)
					{
						m[j] = c*(m[j]+z[j])+(1-c)*((double)1.0/25054.0);   // finding one element in matrix
						
						rowSum+= m[j] * prob[j];					// matrix multiplication of M* and P
					}
					
					newProb[i] = rowSum;
				}
					if(match()) break;
					
					for(int i=0; i< totalDoc; i++)
					{
						prob [i] = newProb[i];
					}
		}
		
		System.out.println("iterations :"+(iterations-1));
		double max = Double.MIN_VALUE;  // Finding doc which has max page rank value 
		int maxIndex = 0;					
		for (int i = 0; i < totalDoc; i++)
			{
			if(prob[i] > max)
				{
					max = prob[i];
					maxIndex = i;
				}
			}
		for (int i = 0; i < totalDoc; i++)
			{
				prob [i] = prob[i]/max;
			}	
		System.out.println("max value is"+max+"index is "+maxIndex);
		return prob;
	}
		
	public boolean match () {
		boolean check = true;
		double[] difference = new double[totalDoc];
			for (int i = 0; i < totalDoc; i++)
				{
					difference[i] = Math.abs(prob[i]-newProb[i]);
					if(difference[i] > 0.0000001) 
					{ 
						  check=false;
					      break; 
					}
				}
			return check;
		}
}