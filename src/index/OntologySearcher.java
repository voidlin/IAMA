package index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import util.CleanUp;

public class OntologySearcher {
	
	private static int MAXHITS = 10;
	
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
	
	private QueryParser localnameparser = new QueryParser(Version.LUCENE_36, "localname",
			analyzer);
	private QueryParser labelparser = new QueryParser(Version.LUCENE_36, "label",
			analyzer);
	private QueryParser commentparser = new QueryParser(Version.LUCENE_36, "comment",
			analyzer);
	
	public OntologySearcher(String indexPath) throws IOException
	{
		reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
		searcher = new IndexSearcher(reader);
	}
	
	public List<Integer> searchName(String name)
	{
		return searchName(name, MAXHITS);
	}
	
	public List<Integer> searchName(String name, int maxHits)
	{
		name = CleanUp.splitToSpace(name);
		Query query = null;
		try {
			query = localnameparser.parse(name);
		} catch (ParseException e) {
//			e.printStackTrace();
		}
		return search(query, maxHits);
	}	
	
	public List<Integer> searchLabel(String label)
	{
		return searchLabel(label, MAXHITS);
	}
	
	public List<Integer> searchLabel(String label, int maxHits)
	{
		label = CleanUp.splitToSpace(label);
		Query query = null;;
		try {
			query = labelparser.parse(label);
		} catch (ParseException e) {
//			e.printStackTrace();
		}
		return search(query, maxHits);
	}
	
	public List<Integer> searchComment(String comment)
	{
		return searchComment(comment, MAXHITS);
	}
	
	public List<Integer> searchComment(String comment, int maxHits)
	{
		comment = CleanUp.splitToSpace(comment);
		Query query = null;
		try {
			query = commentparser.parse(comment);
		} catch (ParseException e) {
//			e.printStackTrace();
		}
		return search(query, maxHits);
	}
	
	public List<Integer> search(Query query, int maxHits)
	{
		
		List<Integer> results = new ArrayList<Integer>();
		if(query == null)
		{
//			System.out.println("search query is null");
			return results;
		}
		try
		{
			for (ScoreDoc hit : searcher.search(query, maxHits).scoreDocs)
			{
				String value = searcher.doc(hit.doc).get("ontid");
				results.add(Integer.parseInt(value));
			}

		} catch (Exception e)
		{
//			System.out.println("excepiton when parsing query expresstion and search :"
//					+ query);
//			e.printStackTrace();
		}

		return results;
	}
	
	public void close() throws IOException
	{
		searcher.close();
		reader.close();
	}
}
