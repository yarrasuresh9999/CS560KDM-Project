import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class FirstServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
		
		  super.init(config);
		  ServletContext application = getServletConfig().getServletContext();  
			try
			{
				
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			
			double nf[] = new double[r.maxDoc()];					// an array to store document norms
			Map<Integer, Map<String,Double>> documentTermMatrix = new HashMap<Integer, Map<String,Double>>(); // Document Term Matrix
			Map<String,Double> idf = new HashMap<String,Double>();  // map to store idf of terms
			
			TermEnum t = r.terms();
	 
					System.out.println("building repository using TF/IDF.. "); 
					while(t.next())												  //Building Document term matrix using TF/IDF weights												
					{					
						Term te = new Term("contents", t.term().text());
						TermDocs td = r.termDocs(te);							  // retrieves all the documents with a term
						
						while(td.next()) 
							{	
									if(documentTermMatrix.containsKey(td.doc()))   // if that document already exists in the document term matrix
										{
											documentTermMatrix.get(td.doc()).put(t.term().text(), (double)td.freq());
										}
									else										    // if that document doesn't exists in matrix, build new entry in map with that doc id
										{	
											Map<String,Double> termsInDocument = new HashMap<String,Double>();
											termsInDocument.put(t.term().text(), (double)td.freq());
											documentTermMatrix.put(td.doc(),termsInDocument);
										}
							}	
						idf.put(te.text(), Math.log((double)r.maxDoc()/r.docFreq(te))/(Math.log(2))); //Computing IDF of a term
					 }
				
					 for(int doc=1;doc<r.maxDoc();doc++) 
					 {
						 double maxFrequencyOfDocument = Collections.max(documentTermMatrix.get(doc).values()); // finding max frequency in a document
						 double normalizedTfIDF=0;
						 	for(String key : documentTermMatrix.get(doc).keySet()) 
						 	{  
						 		double tfIdf=(documentTermMatrix.get(doc).get(key)/maxFrequencyOfDocument)*(idf.get(key)); // Calculating TF*IDF of a term in document
						 		documentTermMatrix.get(doc).put(key, tfIdf);			// Replacing term weight with it's new normalized weight
						 		normalizedTfIDF+=Math.pow(tfIdf, 2);
						 	}
						 nf[doc]=1/Math.pow(normalizedTfIDF, 0.5);                    // building document norms
					 }
					 ComputePageRank pageRank = new ComputePageRank();
					 double[] pageRanks = pageRank.computePageRank();
					 application.setAttribute("documentTermMatrix", documentTermMatrix);
					 application.setAttribute("nf", nf);
					 application.setAttribute("pageRanks", pageRanks);
				}
			catch(IOException e)
			{
				e.printStackTrace();
			}
	}
	
	 public void service(HttpServletRequest request, HttpServletResponse response)
			    throws ServletException, IOException
			{
		 /*
			System.out.println("in the method");
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			Scanner scanner = new Scanner(System.in);
			
			double nf[] = new double[r.maxDoc()];					// an array to store document norms
			Map<Integer, Map<String,Double>> documentTermMatrix = new HashMap<Integer, Map<String,Double>>(); // Document Term Matrix
			Map<String,Double> idf = new HashMap<String,Double>();  // map to store idf of terms
			
			TermEnum t = r.terms();
	 
					System.out.println("building repository using TF/IDF.. "); 
					while(t.next())												  //Building Document term matrix using TF/IDF weights												
					{					
						Term te = new Term("contents", t.term().text());
						TermDocs td = r.termDocs(te);							  // retrieves all the documents with a term
						//int NumOfDocHavingTerm=0;
						
						while(td.next()) 
							{	//NumOfDocHavingTerm++;
									if(documentTermMatrix.containsKey(td.doc()))   // if that document already exists in the document term matrix
										{
											documentTermMatrix.get(td.doc()).put(t.term().text(), (double)td.freq());
										}
									else										    // if that document doesn't exists in matrix, build new entry in map with that doc id
										{	
											Map<String,Double> termsInDocument = new HashMap<String,Double>();
											termsInDocument.put(t.term().text(), (double)td.freq());
											documentTermMatrix.put(td.doc(),termsInDocument);
										}
							}	
							// if(NumOfDocHavingTerm!=0) 
						idf.put(te.text(), Math.log((double)r.maxDoc()/r.docFreq(te))/(Math.log(2))); //Computing IDF of a term
					 }
				
					 for(int doc=1;doc<r.maxDoc();doc++) 
					 {
						 double maxFrequencyOfDocument = Collections.max(documentTermMatrix.get(doc).values()); // finding max frequency in a document
						 double normalizedTfIDF=0;
						 	for(String key : documentTermMatrix.get(doc).keySet()) 
						 	{  
						 		double tfIdf=(documentTermMatrix.get(doc).get(key)/maxFrequencyOfDocument)*(idf.get(key)); // Calculating TF*IDF of a term in document
						 		documentTermMatrix.get(doc).put(key, tfIdf);			// Replacing term weight with it's new normalized weight
						 		normalizedTfIDF+=Math.pow(tfIdf, 2);
						 	}
						 nf[doc]=1/Math.pow(normalizedTfIDF, 0.5);                    // building document norms
					 }
					 
					 HttpSession s = request.getSession(true);
					 System.out.println("set session");
					 s.setAttribute("documentTermMatrix", documentTermMatrix);
					 s.setAttribute("nf", nf);
			*/}
}