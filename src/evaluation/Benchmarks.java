package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Properties;

import matcher.MyMatcherV1;
import matcher.MyMatcherV2;
import matcher.MyMatcherV3;
import matcher.MyMatcherV4;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class Benchmarks {
	
	/**
	 * 对benchmarks进行测试，给出准确率、召回率、F值
	 * @throws AlignmentException
	 * @throws OntowrapException 
	 * @throws IOException 
	 */
	public void eva() throws AlignmentException, OntowrapException, IOException{
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("E:/nlpr/NLP_tool/OAEI/benchmarkResults/temp.txt"), "utf-8"));
		Properties param = new BasicParameters();
		AlignmentParser aparser = new AlignmentParser(0);
		
		File ont1 = new File("E:/nlpr/NLP_tool/OAEI/benchmarks/101/onto.rdf");
		URI onto1 = ont1.toURI();
		File ont2 = null;
		File ref = null;
		
		File path = new File("E:/nlpr/NLP_tool/OAEI/benchmarks/");
		File[] files = path.listFiles();
		
		//计算调和平均用
		double mean_Precision = 0;
		double mean_Recall = 0;
		double mean_F = 0;
		int count = 0;
		
		
		for(File f:files){
			System.out.println(f.getName());
			bw.write(f.getName() + "\n");
			for(File ff:f.listFiles()){
				if(ff.getName().equals("onto.rdf")){
					ont2 = ff;
				}
				if(ff.getName().equals("refalign.rdf")){
					ref = ff;
				}
			}

			URI onto2 = ont2.toURI();
			URI refa = ref.toURI();
			Alignment reference = aparser.parse(refa);

			AlignmentProcess mm = new MyMatcherV4();
			
			mm.init(onto1, onto2);
			mm.align(null, param);
			Properties p = new BasicParameters();
			PRecEvaluator eva = new PRecEvaluator(reference, mm);
			eva.eval(p);		  
			System.out.println(eva.getPrecision()+"\t"+eva.getRecall()+"\t"+eva.getFmeasure());
			bw.write(eva.getPrecision()+"\t"+eva.getRecall()+"\t"+eva.getFmeasure() + "\n");
			
			count ++;
			mean_Precision += eva.getPrecision();
			mean_Recall += eva.getRecall();
			mean_F += eva.getFmeasure();
		}
		bw.close();
		
		mean_Precision = mean_Precision / count;
		mean_Recall = mean_Recall / count;
		mean_F = mean_F / count;
		System.out.println("mean:\n" + mean_Precision + "\t" + mean_Recall + "\t" + mean_F);

	}
	
	public static void main(String[] args) throws AlignmentException, OntowrapException, IOException {
		Benchmarks bm = new Benchmarks();
		bm.eva();
	}

}
