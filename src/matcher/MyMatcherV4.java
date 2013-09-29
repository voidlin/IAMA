package matcher;

import index.OntologyIndex;
import index.OntologySearcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
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

public class MyMatcherV4 extends URIAlignment implements AlignmentProcess {
	public static JENAOntologyFactory jf = new JENAOntologyFactory();

	public JENAOntology o1 = null;
	public JENAOntology o2 = null;

	// �õ���ƥ�������JENAOntology
	public void loadOnt() throws OntowrapException {
		URI ont1 = getOntology1URI();
		URI ont2 = getOntology2URI();

		o1 = jf.loadOntology(ont1);
		o2 = jf.loadOntology(ont2);
	}

	@Override
	public void align(Alignment arg0, Properties arg1)
			throws AlignmentException {

		try {
			loadOnt();

			// match class
			if(o1.nbClasses() > 500 && o2.nbClasses() > 500){
				matchClassOfLargeOnt();
			} else{
				matchClass();
			}

			// match Properties
			matchProperty();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (OntowrapException e) {
			e.printStackTrace();
		}
	}

	private void matchClassOfLargeOnt() throws AlignmentException, URISyntaxException, OntowrapException {
		
		//��¼�ڶ��������е������Ϣ�����ڼ���
		List<OntClass> onto2list = new ArrayList<OntClass>();
		for(OntClass onto2 : o2.getClasses()){
			onto2list.add(onto2);
		}
		Date date = new Date();
		String onto2IndexPath ="onto2Index";
		
		//�������Ŀ¼�Ѵ��ڣ���ɾ��
		if(!deleteDirectory(onto2IndexPath)){
//			System.out.println("delete folder not successful");
		}
		
		OntologyIndex.buildIndex(onto2list, onto2IndexPath);//��onto2list�е���Ϣ����������onto2IndexPathĿ¼��
		
		OntologySearcher searcher = OntologyIndex.buildSearch(onto2IndexPath);
		//ʹ��searcher���������԰���localname, label, comment�������ѣ����ص�ID��onto2list�����е�����
		
		
		for (OntClass c1 : o1.getClasses()) {
			OntClass maxClass = null;
			double maxSim = 0;
			
//			System.out.println(c1.getLocalName());
			
			String localname = c1.getLocalName();
			List<Integer> o2NameIDs = searcher.searchName(localname);
//			System.out.println("o2NameIDs: " + o2NameIDs);
			
			String label = c1.getLabel("");
			List<Integer> o2LabelIDs = searcher.searchLabel(label);
//			System.out.println("o2LabelIDs: " + o2LabelIDs);
			
			String comment = c1.getComment("en");
			List<Integer> o2CommentIDs = searcher.searchComment(comment);
//			System.out.println("o2CommentIDs: " + o2CommentIDs);
			
			Set<Integer> all = new HashSet<Integer>();
			all.addAll(o2NameIDs);
			all.addAll(o2LabelIDs);
			all.addAll(o2CommentIDs);
			
			for (int i : all) {	
				OntClass c2 = onto2list.get(i);
				double s = 0;
				List<Double> sims = new ArrayList<Double>();

				// LocalName similarity
				String s1 = CleanUp.clean(c1.getLocalName());
				String s2 = CleanUp.clean(c2.getLocalName());
				double sn = EditDistance.similarity(s1, s2);
				sims.add(sn * 1.02);

				// Label similarity
				String l1 = CleanUp.clean(c1.getLabel(""));
				String l2 = CleanUp.clean(c2.getLabel(""));
				double sl = EditDistance.similarity(l1, l2);
				sims.add(sl * 1.01);

				// Comment similarity
				String co1 = CleanUp.clean(c1.getComment("en"));
				String co2 = CleanUp.clean(c2.getComment("en"));
				double sc = EditDistance.similarity(co1, co2);
				sims.add(sc);

				// Individual similarity
				Set<String> i1 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> indIt1 = c1
						.listInstances();
				while (indIt1.hasNext()) {
					OntResource or = indIt1.next();
					if (or.getLocalName() == null)
						continue;
					i1.add(or.getLocalName());
				}

				Set<String> i2 = new HashSet<String>();
				ExtendedIterator<? extends OntResource> indIt2 = c2
						.listInstances();
				while (indIt2.hasNext()) {
					OntResource or = indIt2.next();
					if (or.getLocalName() == null)
						continue;
					i2.add(or.getLocalName());
				}
				double si = SetSim.similarity(i1, i2);
				sims.add(si);

				s = MaxValue.getMaxValue(sims);
				if (s > maxSim) {
					maxSim = s;
					maxClass = c2;
				}

			}

			if (maxClass != null && maxSim > 0.9) {
				addAlignCell(new URI(c1.getURI()), new URI(maxClass.getURI()),
						"=", 1);
			}
			
		}
		
		try {
			searcher.close();
		} catch (IOException e) {
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
	
	
	private void matchProperty() throws AlignmentException, URISyntaxException {

		for (OntProperty p1 : o1.getProperties()) {
			OntProperty maxProperty = null;
			double maxSim = 0;

			for (OntProperty p2 : o2.getProperties()) {
				double s = 0;
				List<Double> sims = new ArrayList<Double>();

				// LocalName similarity
				String s1 = CleanUp.clean(p1.getLocalName());
				String s2 = CleanUp.clean(p2.getLocalName());
				double sn = EditDistance.similarity(s1, s2);
				sims.add(sn * 1.02);

				// Label similarity
				String l1 = CleanUp.clean(p1.getLabel(""));
				String l2 = CleanUp.clean(p2.getLabel(""));
				double sl = EditDistance.similarity(l1, l2);
				sims.add(sl * 1.01);

				// Comment similarity
				String co1 = CleanUp.clean(p1.getComment("en"));
				String co2 = CleanUp.clean(p2.getComment("en"));
				double sc = EditDistance.similarity(co1, co2);
				sims.add(sc);

				s = MaxValue.getMaxValue(sims);

				if (s > maxSim) {
					maxSim = s;
					maxProperty = p2;
				}
			}

			if (maxProperty != null && maxSim > 0.9) {
				addAlignCell(new URI(p1.getURI()),
						new URI(maxProperty.getURI()), "=", 1);
			}
		}

	}
	
    /**
     * ɾ�������ļ�
     * @param   sPath    ��ɾ���ļ����ļ���
     * @return �����ļ�ɾ���ɹ�����true�����򷵻�false
     */
    private boolean deleteFile(String sPath) {
    	boolean flag;
        flag = false;
        File file = new File(sPath);
        // ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
	
    /**
     * ɾ��Ŀ¼���ļ��У��Լ�Ŀ¼�µ��ļ�
     * @param   sPath ��ɾ��Ŀ¼���ļ�·��
     * @return  Ŀ¼ɾ���ɹ�����true�����򷵻�false
     */
    private boolean deleteDirectory(String sPath) {
    	boolean flag;
    	
        //���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�
        if (!dirFile.exists() || !dirFile.isDirectory()) {
//        	System.out.println("not a folder");
            return false;
        }
        flag = true;
        //ɾ���ļ����µ������ļ�(������Ŀ¼)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //ɾ�����ļ�
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //ɾ����Ŀ¼
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        System.out.println(dirFile.listFiles().length);
        //ɾ����ǰĿ¼
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }
	
	public static void main(String[] args) throws AlignmentException {
		MyMatcherV4 mm4 = new MyMatcherV4();
		URI ont1 = new File("E:/OAEI/LargeBioMed_dataset_oaei2013/oaei2013_NCI_whole_ontology.owl").toURI();
		URI ont2 = new File("E:/OAEI/LargeBioMed_dataset_oaei2013/oaei2013_FMA_whole_ontology.owl").toURI();	
		
		mm4.init(ont1, ont2);
		mm4.align(null, null);
	}
}
