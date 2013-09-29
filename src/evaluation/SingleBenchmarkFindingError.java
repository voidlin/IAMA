/*
 * 找到自己的匹配结果和标准答案的区别在哪
 */
package evaluation;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.Set;

import matcher.MyMatcherV1;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import util.CleanUp;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SingleBenchmarkFindingError {
	
	MyMatcherV1 mm;
	
	public SingleBenchmarkFindingError(){
		mm = new MyMatcherV1();
	}
	
	public void eva(String testSet) throws AlignmentException, OntowrapException{
		Properties param = new BasicParameters();
		AlignmentParser aparser = new AlignmentParser(0);
		
		URI ont1 = new File("E:/nlpr/NLP_tool/OAEI/benchmarks/101/onto.rdf").toURI();
		URI ont2 = new File("E:/nlpr/NLP_tool/OAEI/benchmarks/" + testSet + "/onto.rdf").toURI();
		URI ref = new File("E:/nlpr/NLP_tool/OAEI/benchmarks/" + testSet + "/refalign.rdf").toURI();
		
		Alignment reference = aparser.parse(ref);
		Properties p = new BasicParameters();
		
	
		mm.init(ont1, ont2);
		mm.align(null, param);
		
		
		PRecEvaluator eva = new PRecEvaluator(reference, mm);
		eva.eval(p);
		
		//找出哪错了
		int inCorrect = 0;
		for ( Cell c1 : reference ) {
		    URI uri1 = c1.getObject2AsURI();
		    Set<Cell> s2 = mm.toURIAlignment().getAlignCells1( c1.getObject1() );
		    if( s2 != null ){
				for( Cell c2 : s2 ) {
				    URI uri2 = c2.getObject2AsURI();	
				    // if (c1.getobject2 == c2.getobject2)
				    if ( uri1.toString().equals(uri2.toString()) ) {
					break;
				    } else{
				    	System.out.println("******");
				    	System.out.println("ans:  " + c1.getObject1AsURI().toString() + "\t" + uri1.toString() + "\nours: "
				    			+ c2.getObject1AsURI().toString() + "\t" + uri2.toString());
				    	System.out.println("******");
				    	inCorrect ++;
				    }
				}
		    } else{
		    	System.out.println("Error: Cannot find alignment for" + c1.getObject1AsURI().toString());
		    }
		}
		
		
		//匹配数
		System.out.println("ans: " + reference.nbCells()+ "\t\t" + "ours: " + mm.toURIAlignment().nbCells()
				+ "\tErrorNum: " + inCorrect);	
		//准确率，召回率，F值
		System.out.println(eva.getPrecision() + "\t" + eva.getRecall() + "\t" + eva.getFmeasure());
		
	}
	
	
	/**
	 * 查看具体的匹配结果
	 * @param s1 Object1的名字
	 * @param s2 Object2的名字
	 */
	public void debug(String s1, String s2){
		mm.debug(s1, s2);
	}
	
	public static void main(String[] args) throws AlignmentException, OntowrapException {
		SingleBenchmarkFindingError s = new SingleBenchmarkFindingError();
		s.debug(CleanUp.clean("book"), CleanUp.clean("collection"));
		s.eva("237");
	}

}
