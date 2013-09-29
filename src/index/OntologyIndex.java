package index;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import util.CleanUp;

import com.hp.hpl.jena.ontology.OntClass;

public class OntologyIndex {

	public static void main(String[] args) {
		Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_36);
//		System.out.println(analyzer);
	}
	
	public static void buildIndex(List<OntClass> ontolist, String indexPath) {
		try {
			
			Directory  dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);// 进行停用词过滤，词干化
//			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);// 进行停用词过滤，词干化
			IndexWriter writer = new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			for (int ontoIndex = 0; ontoIndex < ontolist.size(); ontoIndex++) {
				OntClass ontClass = ontolist.get(ontoIndex);
				// 创建索引单元document
				String docId = String.valueOf(ontoIndex);// 从0开始
				String localName = ontClass.getLocalName();	
				if (localName == null)
					localName = "";
				localName = CleanUp.splitToSpace(localName);
				
				String label = ontClass.getLabel("");
				if (label == null || label == "")
					label = ontClass.getLabel("en");
				if (label == null)
					label = "";
				label = CleanUp.splitToSpace(label);
				
				String comment = ontClass.getComment("en");
				if (comment == null || comment == "")
					comment = ontClass.getComment("");
				if (comment == null)
					comment = "";
				comment = CleanUp.splitToSpace(comment);
				
				// 建立一个document,有四个域:ontid, localname, label, comment
				Document doc = new Document();
				doc.add(new Field("ontid", docId, Field.Store.YES,
						Field.Index.NOT_ANALYZED_NO_NORMS));
				doc.add(new Field("localname", localName, Field.Store.YES,
						Field.Index.ANALYZED));
				doc.add(new Field("label", label, Field.Store.YES,
						Field.Index.ANALYZED));
				doc.add(new Field("comment", comment, Field.Store.YES,
						Field.Index.ANALYZED));
				writer.addDocument(doc);
			}
			if (writer != null)
				writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("创建索引失败");
		}
	}

	// 读取indexPath位置的索引
	public static OntologySearcher buildSearch(String indexPath) {
		try {
			return new OntologySearcher(indexPath);
		} catch (IOException e) {			
//			e.printStackTrace();
			System.out.println("读取索引失败");
		}
		return null;
	}
}
