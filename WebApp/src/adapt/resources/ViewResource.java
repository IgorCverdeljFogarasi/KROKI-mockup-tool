package adapt.resources;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import adapt.application.AdaptApplication;
import adapt.utils.EntityClass;
import adapt.utils.EntityCreator;
import adapt.utils.Settings;
import adapt.utils.XMLAttribute;
import adapt.utils.XMLManyToOneAttribute;
import adapt.utils.XMLResource;
import freemarker.template.Configuration;

public class ViewResource extends Resource {

	Map<String, Object> dataModel = new TreeMap<String, Object>();

	XMLResource resource;
	
	XMLResource childResource;
	EntityCreator creator;

	public ViewResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(true);
		Variant variant = new Variant();
		variant.setMediaType(MediaType.TEXT_HTML);
		getVariants().add(variant);
	}

	public Representation getTemplateRepresentation(String templateName,
			Map<String, Object> dataModel, MediaType mt) {
		// The template representation is based on Freemarker.
		return new TemplateRepresentation(templateName, getFmcConfiguration(),
				dataModel, mt);
	}

	public Representation getHTMLTemplateRepresentation(String templateName,
			Map<String, Object> dataModel) {
			TemplateRepresentation represntation = new TemplateRepresentation(templateName, getFmcConfiguration(),
					dataModel, MediaType.TEXT_HTML);
			Encoding enc = new Encoding("UTF-8");
			List<Encoding> encodings = new ArrayList<Encoding>();
			encodings.add(enc);
			represntation.setCharacterSet(CharacterSet.UTF_8);
			represntation.setEncodings(encodings);
		// The template representation is based on Freemarker.
		return represntation; 
	}

	private Configuration getFmcConfiguration() {
		final AdaptApplication application = (AdaptApplication) getApplication();
		return application.getFmc();
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public void handleGet() {
		AdaptApplication application = (AdaptApplication) getApplication();
		String resName	= (String)getRequest().getAttributes().get("resName");
		String cresName = (String)getRequest().getAttributes().get("cresName");
		creator = new EntityCreator(application);
		String query = "";
		
		if(resName != null) {
			resource = application.getXMLResource(resName);
			String formType = resource.getForms().get(0).getName();
			query = "FROM " + resource.getName();
			prepareContent(query, resource);
		}

		if(cresName != null) {//child forma
			String presName = (String)getRequest().getAttributes().get("presName");
			String pid = (String)getRequest().getAttributes().get("pid");
			dataModel.put("pid", pid);
			dataModel.put("presName", presName);
			System.out.println("[PRESNAME] " + presName + "\n[PID] " + pid);
			resource = application.getXMLResource(cresName);
			XMLResource parentResource = application.getXMLResource(presName);
			
			if(resource != null) {
				for (XMLManyToOneAttribute mattr : resource.getManyToOneAttributes()) {
					if(mattr.getType().equals(presName)) {
						query = "FROM " + resource.getName() + " o WHERE o." + mattr.getName() + ".id = " + pid;
						prepareContent(query, resource);
					}
				}
			}
		}
		dataModel.put("resource", resource);
		super.handleGet();
	}

	@SuppressWarnings("unchecked")
	public void prepareContent(String query, XMLResource resource) {
		System.out.println("[PREPARE CONTENT] query = " + query);
		AdaptApplication application = (AdaptApplication) getApplication();
		EntityManager em =application.getEmf().createEntityManager();
		if (resource != null) {
			//smestimo resurs u data model
			dataModel.put("resource", resource);
			//smestimo i listu sa ostalim resursima u data model
			//za moguce koristenje na nekim formama
			ArrayList<XMLResource> XMLresources = application.XMLResources;
			dataModel.put("XMLresources", XMLresources);
			//pokupimo labele atributa resursa koje sluze kao zaglavlja tabele
			ArrayList<String> headers = new ArrayList<String>();
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			//iz baze se ucitaju svi entiteti koji pripadaju trezenom resursu
			Query q = em.createQuery(query);
			ArrayList<Object> ress = (ArrayList<Object>) q.getResultList();
			tx.commit();
			em.close();
			if(ress != null) {
				ArrayList<EntityClass> entities;
				try {
					//objekte koje nam vrati upit pomocu klase EntityCreator
					//pretvorimo u objekte klase EntityClass
					//i smestimo u data model
					entities = creator.getEntities(ress, headers, resource);
					if(!entities.isEmpty()) {
						Map<String, String> childMap = new LinkedHashMap<String, String>();

						for(int j=0; j<entities.size(); j++) {
							EntityClass ecl = entities.get(j);
							String Id = creator.getEntityPropertyValue(ecl, "id");
							String name = "";
							for (XMLAttribute attr : resource.getRepresentativeAttributes()) {
								name += creator.getEntityPropertyValue(ecl, attr.getName()) + ", ";
							}

							if(!name.equals("")) {
								name = name.substring(0, name.length()-2);
							}

							childMap.put(Id, name);
						}
						dataModel.put("mainFormHeaders", headers);
						dataModel.put("entities", entities);
						dataModel.put("childMap", childMap);
					}else {
						dataModel.put("msg", "No entries in the database for requested resource!");
					}
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			prepareAdd(resource);
		}
	}

	public void prepareAdd(XMLResource resource) {
		AdaptApplication app = (AdaptApplication) getApplication();
		EntityManager em = app.getEmf().createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		/*
		 * Child elements map.
		 * Map key is element label, and values are map with entity id as key and value is name if child class has attribute called 'name',
		 * else id attribute is used.
		 * This map is used to generate content for zoom combo boxes
		 */
		LinkedHashMap<String, Map<String, String>> childFormMap = new LinkedHashMap<String, Map<String,String>>();
		//get all child attributes from database and generate EntityClass object for each
		for(int i=0; i<resource.getManyToOneAttributes().size(); i++) {
			XMLManyToOneAttribute mattr = resource.getManyToOneAttributes().get(i);
			XMLResource ress = app.getXMLResource(mattr.getType());
			ArrayList<Object> objects = (ArrayList<Object>) em.createQuery("FROM " + mattr.getType()).getResultList();
			ArrayList<EntityClass> entities;
			try {
				entities = creator.getEntities(objects, null, null);
				Map<String, String> childMap = new LinkedHashMap<String, String>();
				if(!mattr.getMandatory()) {
					childMap.put("null", "-- None --");
				}
				for(int j=0; j<entities.size(); j++) {
					EntityClass ecl = entities.get(j);
					String Id = creator.getEntityPropertyValue(ecl, "id");
					String name = "";

					for (XMLAttribute attr : ress.getRepresentativeAttributes()) {
						name += creator.getEntityPropertyValue(ecl, attr.getName()) + ", ";
					}

					if(!name.equals("")) {
						name = name.substring(0, name.length()-2);
					}else {
						name = Id;
					}

					childMap.put(Id, name);
				}
				childFormMap.put(mattr.getLabel(), childMap);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		tx.commit();
		em.close();
		dataModel.put("childFormMap", childFormMap);
	}

	@Override
	public void handlePost() {
		handleGet();
	}

	public Map<String, Object> getDataModel() {
		return dataModel;
	}

	public void setDataModel(Map<String, Object> dataModel) {
		this.dataModel = dataModel;
	}

	public XMLResource getResource() {
		return resource;
	}

	public void setResource(XMLResource resource) {
		this.resource = resource;
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		dataModel.put("title", Settings.APP_TITLE);
		return getHTMLTemplateRepresentation("formTemplate.html", dataModel);
	}
}
