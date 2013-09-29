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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory;

/**
 * 取相似度最大的，1:1匹配
 * 设定权重，name > label > comments...
 * 加入match property with individual
 * @author zyz
 *
 */
public class MyMatcherV3 extends URIAlignment implements AlignmentProcess{
	
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
			
			// match class
			matchClass();
			
			//match Properties
			matchProperty();
			matchPropertyWithIndividuals();
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
				
				s = MaxValue.getMaxValue(sims);
				if(s > maxSim){
					maxSim = s;
					maxClass = c2;
				}
				
			}
			
			if(maxClass != null && maxSim > 0.9){
				addAlignCell(new URI(c1.getURI()),new URI(maxClass.getURI()),"=",1);
			}
			
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
				
				
				s = MaxValue.getMaxValue(sims);
				
				if(s > maxSim){
					maxSim = s;
					maxProperty = p2;
				}
			}
			
			if(maxProperty != null && maxSim > 0.9){
				addAlignCell(new URI(p1.getURI()),new URI(maxProperty.getURI()),"=",1);
			}
		}
		
	}
	
	private void matchPropertyWithIndividuals() throws AlignmentException, URISyntaxException{
		OntModel om1 = o1.getOntology();
		OntModel om2 = o2.getOntology();
		
		for(Individual i1 : o1.getIndividuals()){
			if(i1.getLocalName() == null)	
				continue;
			for(Individual i2 : o2.getIndividuals()){
				if(i2.getLocalName() == null)
					continue;
				
				//找到两个本体中相同的individual
				if(i1.getLocalName().equals(i2.getLocalName())){
//					System.out.println(i1.getLocalName());
					StmtIterator sit1 = om1.listStatements(i1, (Property) null, (RDFNode) null);
					StmtIterator sit2 = om2.listStatements(i2, (Property) null, (RDFNode) null);
					
					Set<Statement> sits1 = sit1.toSet();
					Set<Statement> sits2 = sit2.toSet();
					
					for(Statement st1 : sits1){
						RDFNode obj1 = st1.getObject();
						for(Statement st2 :sits2){
							RDFNode obj2 = st2.getObject();
							
							//Literal
							if(obj1.isLiteral() && obj2.isLiteral() &&
									obj1.asLiteral().toString().equals(obj2.asLiteral().toString())){
//								System.out.println("literal: " + st1.getPredicate().getURI() + " == " + st2.getPredicate().getURI());
								addAlignCell(new URI(st1.getPredicate().getURI()), new URI(st2.getPredicate().getURI()));
							}
							
							//Resource
							//如果object是resource，那么要列出这个resource的三元组，查看是否有匹配的部分
							if(obj1.isResource() && obj2.isResource()){
								
								StmtIterator iter1 = obj1.asResource().listProperties();
								StmtIterator iter2 = obj2.asResource().listProperties();
								
								Set<Statement> ss1 = iter1.toSet();
								Set<Statement> ss2 = iter2.toSet();
//								
								for(Statement s1 : ss1){
									for(Statement s2 : ss2){
										
										if(s1.getObject().isLiteral() && s2.getObject().isLiteral()){
//											System.out.println(
//													s1.getPredicate().getLocalName() + " " + s1.getObject().asLiteral().toString() + " " 
//											+ s2.getPredicate().getLocalName() + " " + s2.getObject().asLiteral().toString());
											if(s1.getObject().asLiteral().toString().equals(s2.getObject().asLiteral().toString())){
												addAlignCell(new URI(s1.getPredicate().getURI()), new URI(s2.getPredicate().getURI()));
												addAlignCell(new URI(st1.getPredicate().getURI()), new URI(st2.getPredicate().getURI()));
//												System.out
//														.println(st1.getSubject().getLocalName());
//												System.out
//														.println("resource:        " + st1.getPredicate().getLocalName() + " == " + st2.getPredicate().getLocalName());
//												System.out
//														.println("resource detail: " +
//																	s1.getPredicate().getLocalName() + " == " + s2.getPredicate().getLocalName());
											}
										}
									}
								}
							}
						}
						
					}
					
				}
			}
		}
	}
	
}
