package evaluation;

import java.io.File;
import java.net.URI;
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
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class Anatomy {

	public void eva() throws AlignmentException, OntowrapException{
		Properties param = new BasicParameters();
		AlignmentParser aparser = new AlignmentParser(0);
		
		URI ont1 = new File("E:/OAEI/anatomy/mouse.owl").toURI();
		URI ont2 = new File("E:/OAEI/anatomy/human.owl").toURI();
		URI ref = new File("E:/OAEI/anatomy/reference.rdf").toURI();
		
		Alignment reference = aparser.parse(ref);
		
		Properties p = new BasicParameters();
		
//		ProtoMatcherV2 mm = new ProtoMatcherV2();
//		ProtoMatcherV1 mm = new ProtoMatcherV1();
//		MyHeavyMatcher1 mm = new MyHeavyMatcher1();
		AlignmentProcess mm = new MyMatcherV4();
		mm.init(ont1, ont2);
		mm.align(null, param);
		PRecEvaluator eva = new PRecEvaluator(reference, mm);
		eva.eval(p);		
		System.out.println(eva.getPrecision()+"\t"+eva.getRecall()+"\t"+eva.getFmeasure());

		}
	
	public static void main(String[] args) throws AlignmentException, OntowrapException {
		Anatomy at = new Anatomy();
		long begin_ms = System.currentTimeMillis();
		at.eva();
		System.out.println("Time used: " + (System.currentTimeMillis() - begin_ms) + " ms");
	}

}
