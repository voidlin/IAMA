package matcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import simAlgorithms.EditDistance;
import simAlgorithms.SetSim;
import util.CleanUp;
import util.MaxValue;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory;

/**
 * 取相似度最大的，1:1匹配
 * 设定权重，name > label > comments...
 * @author zyz
 *
 */
public class MyMatcherV1 extends URIAlignment implements AlignmentProcess{
	
	public static JENAOntologyFactory jf = new JENAOntologyFactory();
	
	public JENAOntology o1 = null;
	public JENAOntology o2 = null;
	
	private boolean debug;
	private String debugS1;
	private String debugS2;
	
	//得到待匹配的两个JENAOntology
	public void loadOnt() throws OntowrapException {
		URI ont1 = getOntology1URI();
		URI ont2 = getOntology2URI();
		
		o1 = jf.loadOntology(ont1);
		o2 = jf.loadOntology(ont2);
	}

	
	@Override
	public void align(Alignment arg0, Properties arg1) throws AlignmentException {
		try {
			
			loadOnt();
			
			matchClass();
			
			matchProperty();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (OntowrapException e) {
			e.printStackTrace();
		}
	}
	
	private void matchClass() throws AlignmentException, URISyntaxException{
		
		for(OntClass c1 : o1.getClasses()){
			OntClass maxClass = null;
			double maxSim = 0;
			for(OntClass c2 : o2.getClasses()){
				
				double s = 0;
				List<Double> sims = new ArrayList<Double>();
				
				
				//LocalName similarity
				String s1 = CleanUp.clean(c1.getLocalName());
				String s2 = CleanUp.clean(c2.getLocalName());
				double sn = EditDistance.similarity(s1, s2);
				sims.add(sn * 1.02);
				
				//Label similarity
				String l1 = CleanUp.clean(c1.getLabel(""));
				String l2 = CleanUp.clean(c2.getLabel(""));
				double sl = EditDistance.similarity(l1, l2);
				sims.add(sl * 1.01);
				
				//Comment similarity
				String co1 = CleanUp.clean(c1.getComment("en"));
				String co2 = CleanUp.clean(c2.getComment("en"));
				double sc = EditDistance.similarity(co1, co2);
				sims.add(sc);
				
				//Individual similarity
				Set<String> i1 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> indIt1 = c1.listInstances();
				while(indIt1.hasNext()){
					OntResource or = indIt1.next();
					if(or.getLocalName() == null)
						continue;
					i1.add(or.getLocalName());
				}
				
				Set<String> i2 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> indIt2 = c2.listInstances();
				while(indIt2.hasNext()){
					OntResource or = indIt2.next();
					if(or.getLocalName() == null)
						continue;
					i2.add(or.getLocalName());
				}
				double si = SetSim.similarity(i1, i2);
				sims.add(si);
				
				//SubClass similarity
				Set<String> sub1 = new HashSet<String>();
				ExtendedIterator<OntClass> subIt1 = c1.listSubClasses();
				while(subIt1.hasNext()){
					OntClass oc = subIt1.next();
					if(oc.getLocalName() == null)
						continue;
					sub1.add(oc.getLocalName());
				}
				
				Set<String> sub2 = new HashSet<String>();
				ExtendedIterator<OntClass> subIt2 = c2.listSubClasses();
				while(subIt2.hasNext()){
					OntClass oc = subIt2.next();
					if(oc.getLocalName() == null)
						continue;
					sub2.add(oc.getLocalName());
				}
				
				double ssub = SetSim.similarity(sub1, sub2);
//				sims.add(ssub);
				
				//SuperClass similarity
				Set<String> super1 = new HashSet<String>();
				ExtendedIterator<OntClass> superIt1 = c1.listSuperClasses();
				while(superIt1.hasNext()){
					OntClass oc = superIt1.next();
					if(oc.getLocalName() == null)
						continue;
					super1.add(oc.getLocalName());
				}
				
				Set<String> super2 = new HashSet<String>();
				ExtendedIterator<OntClass> superIt2 = c2.listSuperClasses();
				while(superIt2.hasNext()){
					OntClass oc = superIt2.next();
					if(oc.getLocalName() == null)
						continue;
					super2.add(oc.getLocalName());
				}
				
				double ssuper = SetSim.similarity(super1, super2);
//				sims.add(ssuper);
				
				s = MaxValue.getMaxValue(sims);
				if(s > maxSim){
					maxSim = s;
					maxClass = c2;
				}
				
				//调试用
				if(debug && s1.equals(debugS1) && s2.equals(debugS2)){
					System.out.println("Class: " + debugS1 + "\t" + debugS2);
					System.out.println("LocalName: " + s1 + "\t" + s2);
					System.out.println("Label: " + l1 + "\t" + l2);
					System.out.println("Comment: \n" + co1 + "\n" + co2);
					System.out.println("nameSim: " + sn + "\t" + "labelSim: " + sl + "\t" + "commentSim: " + sc + "\t" + "indiSim: " + si);
					System.out.println("s = " + s + "\t" + "maxSim = " +maxSim + "\tmaxProperty = " + maxClass);
				}
				
			}
			
			if(maxClass != null)
				addAlignCell(new URI(c1.getURI()),new URI(maxClass.getURI()),"=",1);
			
		}
	}
	
	private void matchProperty() throws AlignmentException, URISyntaxException{
		for(OntProperty p1 : o1.getProperties()){
			OntProperty maxProperty = null;
			double maxSim = 0;
			for(OntProperty p2 : o2.getProperties()){
				
				double s = 0;
				List<Double> sims = new ArrayList<Double>();
				
				//LocalName similarity
				String s1 = CleanUp.clean(p1.getLocalName());
				String s2 = CleanUp.clean(p2.getLocalName());
				double sn = EditDistance.similarity(s1, s2);
				sims.add(sn * 1.02);
				
				//Label similarity
				String l1 = CleanUp.clean(p1.getLabel(""));
				String l2 = CleanUp.clean(p2.getLabel(""));
				double sl = EditDistance.similarity(l1, l2);
				sims.add(sl * 1.01);
				
				//Comment similarity
				String co1 = CleanUp.clean(p1.getComment("en"));
				String co2 = CleanUp.clean(p2.getComment("en"));
				double sc = EditDistance.similarity(co1, co2);
				sims.add(sc);
				
				//SubProperty similarity
				Set<String> sub1 = new HashSet<String>();
				ExtendedIterator<? extends OntProperty> subIt1 = p1.listSubProperties();
				while(subIt1.hasNext()){
					OntProperty op = subIt1.next();
					if(op.getLocalName() == null)
						continue;
					sub1.add(op.getLocalName());
				}
				
				Set<String> sub2 = new HashSet<String>();
				ExtendedIterator<? extends OntProperty> subIt2 = p2.listSubProperties();
				while(subIt2.hasNext()){
					OntProperty op = subIt2.next();
					if(op.getLocalName() == null)
						continue;
					sub2.add(op.getLocalName());
				}
				
				double ssub = SetSim.similarity(sub1, sub2);
				
				//SuperProperty similarity
				Set<String> super1 = new HashSet<String>();
				ExtendedIterator<? extends OntProperty> superIt1 = p1.listSuperProperties();
				while(superIt1.hasNext()){
					OntProperty op = superIt1.next();
					if(op.getLocalName() == null)
						continue;
					super1.add(op.getLocalName());
				}
				
				Set<String> super2 = new HashSet<String>();
				ExtendedIterator<? extends OntProperty> superIt2 = p2.listSuperProperties();
				while(superIt2.hasNext()){
					OntProperty op = superIt2.next();
					if(op.getLocalName() == null)
						continue;
					super2.add(op.getLocalName());
				}
				
				double ssuper = SetSim.similarity(super1, super2);
				
				//Domain similarity
				Set<String> dom1 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> domIt1 = p1.listDomain();
				while(domIt1.hasNext()){
					OntResource or = domIt1.next();
					if(or.getLocalName() == null)
						continue;
					dom1.add(or.getLocalName());
				}
				
				Set<String> dom2 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> domIt2 = p2.listDomain();
				while(domIt2.hasNext()){
					OntResource or = domIt2.next();
					if(or.getLocalName() == null)
						continue;
					dom2.add(or.getLocalName());
				}
				
				double sd = SetSim.similarity(dom1, dom2);
				
				//Range similarity
				Set<String> ran1 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> ranIt1 = p1.listRange();
				while(ranIt1.hasNext()){
					OntResource or = ranIt1.next();
					if(or.getLocalName() == null)
						continue;
					ran1.add(or.getLocalName());
				}
				
				Set<String> ran2 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> ranIt2 = p2.listRange();
				while(ranIt2.hasNext()){
					OntResource or = ranIt2.next();
					if(or.getLocalName() == null)
						continue;
					ran2.add(or.getLocalName());
				}
				
				double sr = SetSim.similarity(ran1, ran2);
				
				s = MaxValue.getMaxValue(sims);
				
				if(s > maxSim){
					maxSim = s;
					maxProperty = p2;
				}
				
				//调试用
				if(debug && s1.equals(debugS1) && s2.equals(debugS2)){
					System.out.println("Property: " + debugS1 + "\t" + debugS2);
					System.out.println("LocalName: " + s1 + "\t" + s2);
					System.out.println("Label: " + l1 + "\t" + l2);
					System.out.println("Comment: \n" + co1 + "\n" + co2);
					System.out.println("nameSim: " + sn + "\t" + "labelSim: " + sl + "\t" + "commentSim: " + sc);
					System.out.println("s = " + s + "\t" + "maxSim = " +maxSim + "\tmaxProperty = " + maxProperty);
				}
				
			}
			
			if(maxProperty != null)
				addAlignCell(new URI(p1.getURI()),new URI(maxProperty.getURI()),"=",1);
			
		}
	}
	
	public void debug(String s1, String s2){
		this.debug = true;
		this.debugS1 = s1;
		this.debugS2 = s2;
	}

}
