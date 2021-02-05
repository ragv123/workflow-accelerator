package com.adobe.aem.accelerator.program.core.workflows;

import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.services.CreateLiveCopyService;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutManager;

@Component(service = WorkflowProcess.class, property = {
		Constants.SERVICE_DESCRIPTION + "Create live copies",
		Constants.SERVICE_VENDOR + "Adobe", "process.label" + "Create live copies" })
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

	/**
	 * Execute.
	 *
	 * @param workItem         the work item
	 * @param workflowSession  the workflow session
	 * @param paramMetaDataMap the param meta data map
	 */
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap paramMetaDataMap) {
		LOGGER.debug("inside execute method");
		ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
		// get the payload page from the workflow data
		WorkflowData workflowData = workItem.getWorkflowData();
		String payload = workflowData.getPayload().toString();
		if (Objects.nonNull(resolver)) {
			String language = getLanguageCodeFromResource(resolver.getResource(payload));
			LOGGER.info("language : {}", language);
			createLiveCopyService.createLiveCopy(resolver, payload, rolloutManager, liveRelManager, language);
		}
	}

	private String getLanguageCodeFromResource(Resource resource) {
		return "en";
	}
}
