import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SearchEng
 */
public class SearchEng extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public SearchEng() {
        super(); 
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String searchOption=request.getParameter("search");
		String query = request.getParameter("query");
		System.out.println("hi I'm here"+query);
		SearchDocuments searchDocuments = new SearchDocuments();
		try {
			System.out.println("before method call");
			double nf[] = new double[25054];					// an array to store document norms
			Map<Integer, Map<String,Double>> documentTermMatrix = new HashMap<Integer, Map<String,Double>>(); // Document Term Matrix
			
			ServletContext application = getServletConfig().getServletContext();
			documentTermMatrix= (HashMap<Integer, Map<String,Double>>) application.getAttribute("documentTermMatrix");
			nf= (double[]) application.getAttribute("nf");
			double[] pageRanks = (double[]) application.getAttribute("pageRanks");
			
			LinkedHashMap<Integer, String> finalResults = searchDocuments.getResults(query, searchOption, documentTermMatrix, nf, pageRanks);
			
			request.setAttribute("finalResults", finalResults);
			RequestDispatcher rd=request.getRequestDispatcher("/JSP/searchresults.jsp");
			rd.forward(request,response);
			
			} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
