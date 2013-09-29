package evaluation;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import matcher.MyMatcherV2;
import matcher.MyMatcherV3;
import matcher.MyMatcherV4;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * ≤‚ ‘conference
 * @author zyz
 *
 */
public class Conference {
	
	public static void main(String[] args) throws AlignmentException {
		List<String> testList = new ArrayList<String>();
		
		testList.add("cmt@=@conference");
		testList.add("cmt@=@confOf");
		testList.add("cmt@=@edas");
		testList.add("cmt@=@ekaw");
		testList.add("cmt@=@iasted");
		testList.add("cmt@=@sigkdd");
		testList.add("conference@=@confOf");
		testList.add("conference@=@edas");
		testList.add("conference@=@ekaw");
		testList.add("conference@=@iasted");
		testList.add("conference@=@sigkdd");
		testList.add("confOf@=@edas");
		testList.add("confOf@=@ekaw");
		testList.add("confOf@=@iasted");
		testList.add("confOf@=@sigkdd");
		testList.add("edas@=@ekaw");
		testList.add("edas@=@iasted");
		testList.add("edas@=@sigkdd");
		testList.add("ekaw@=@iasted");
		testList.add("ekaw@=@sigkdd");
		testList.add("iasted@=@sigkdd");

		AlignmentParser aparser = new AlignmentParser(0);
		Properties param = new BasicParameters();
		
		double mean_Precision = 0;
		double mean_Recall = 0;
		double mean_F = 0;
		int count = 0;
		
		for(String s : testList){
			
			
			File ont1 = new File("e:/oaei/conference/" + s.split("@=@")[0] + ".owl");
			File ont2 = new File("e:/oaei/conference/" + s.split("@=@")[1] + ".owl");
			File reference = new File("e:/oaei/conference-reference-alignment/" 
						+ s.split("@=@")[0] + "-" + s.split("@=@")[1] + ".rdf");
			
			URI onto1 = ont1.toURI();
			URI onto2 = ont2.toURI();
			URI refe = reference.toURI();
			
			Alignment ref = aparser.parse(refe);
			

			AlignmentProcess mm = new MyMatcherV4();
			mm.init(onto1, onto2);
			mm.align(null, param);
			Properties p = new BasicParameters();
			PRecEvaluator eva = new PRecEvaluator(ref, mm);
			eva.eval(p);		  
			System.out.println(s.split("@=@")[0] + "-" + s.split("@=@")[1] + ":\n"
					+ eva.getPrecision()+"\t"+eva.getRecall()+"\t"+eva.getFmeasure());
			
			count ++;
			mean_Precision += (1 / eva.getPrecision());
			mean_Recall += ( 1 / eva.getRecall());
			mean_F += ( 1 / eva.getFmeasure());
//			System.out.println(eva.getPrecision() + "\t" + eva.getRecall() + "\t" + eva.getFmeasure());
			
		}
		
		mean_Precision = count / mean_Precision;
		mean_Recall = count / mean_Recall;
		mean_F = count / mean_F;
		System.out.println("harmonic_mean:\n" + mean_Precision + "\t" + mean_Recall + "\t" + mean_F);
		
		
	}

}
