package com.adobe.aem.accelerator.program.core.workflows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.beans.RolloutCountryBean;
import com.adobe.aem.accelerator.program.core.services.CreateLiveCopyService;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutManager;

/**
 * The Class CreateLiveCopyWorkflow.
 */
@Component(service = WorkflowProcess.class, property = { Constants.SERVICE_DESCRIPTION + "=Create live copies",
		Constants.SERVICE_VENDOR + "=Adobe", "process.label=Create live copies" })
public class CreateLiveCopyWorkflow implements WorkflowProcess {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateLiveCopyWorkflow.class);

	/** The rollout manager. */
	@Reference
	private RolloutManager rolloutManager;

	/** The live rel manager. */
	@Reference
	private LiveRelationshipManager liveRelManager;

	/** The create live copy service. */
	@Reference
	private CreateLiveCopyService createLiveCopyService;

	/** The resource resolver factory. */
	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	/** The resolver. */
	private ResourceResolver resolver;

	/** The rollout country bean. */
	List<RolloutCountryBean> rolloutCountryBean;

	/**
	 * Execute.
	 *
	 * @param workItem         the work item
	 * @param workflowSession  the workflow session
	 * @param paramMetaDataMap the param meta data map
	 */
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap paramMetaDataMap) {
		LOGGER.debug("inside execute method");
		resolver = workflowSession.adaptTo(ResourceResolver.class);
		// get the payload page from the workflow data
		WorkflowData workflowData = workItem.getWorkflowData();
		String payload = workflowData.getPayload().toString();
		if (Objects.nonNull(resolver)) {
			try {
				getData(payload);
				Resource payloadRes = resolver
						.getResource(payload + "/jcr:content/root/responsivegrid/rolloutcountries");
				ValueMap vmap = payloadRes.adaptTo(ValueMap.class);
				String templatePath = vmap.get("templatePath", StringUtils.EMPTY);
				String siteRootPath = vmap.get("siteRootPath", StringUtils.EMPTY);
				createLiveCopyService.rolloutCountries(resolver, rolloutCountryBean, rolloutManager, liveRelManager,
						siteRootPath, templatePath);
			} catch (WCMException e) {
				LOGGER.error("error", e);
			}
		}
	}

	/**
	 * Gets the data.
	 *
	 * @param payload the payload
	 * @return the data
	 */
	private List<RolloutCountryBean> getData(String payload) {
		final String path = payload + "/jcr:content/root/responsivegrid/rolloutcountries/countryDetails";
		final Resource rolloutCountriesResource = resolver.getResource(path);
		rolloutCountryBean = new ArrayList<>();
		if (rolloutCountriesResource.hasChildren()) {
			Iterator<Resource> res = rolloutCountriesResource.listChildren();
			while (res.hasNext()) {
				RolloutCountryBean bean = new RolloutCountryBean();
				List<String> languages = new ArrayList<>();
				Resource childRes = res.next();
				Iterator<Resource> languageRes = childRes.listChildren();
				while (languageRes.hasNext()) {
					Resource lang = languageRes.next();
					Iterator<Resource> langRes = lang.listChildren();
					while (langRes.hasNext()) {
						Resource language = langRes.next();
						ValueMap valueMap = language.adaptTo(ValueMap.class);
						languages.add(valueMap.get("language", StringUtils.EMPTY));
					}
				}
				ValueMap vmap = childRes.adaptTo(ValueMap.class);

				bean.setTitle(vmap.get("title", StringUtils.EMPTY));
				bean.setName(vmap.get("name", StringUtils.EMPTY));
				bean.setLanguages(languages);
				bean.setRolloutConfigs(vmap.get("cq:rolloutConfigs", String[].class));
				rolloutCountryBean.add(bean);
			}
		}
		return rolloutCountryBean;
	}

}