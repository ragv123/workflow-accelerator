package com.adobe.aem.accelerator.program.core.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.services.CreateLiveCopyService;
import com.adobe.aem.accelerator.program.core.services.config.CreateLiveCopyServiceConfig;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.api.commands.WCMCommand;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.MSMNameConstants;
import com.day.cq.wcm.msm.api.RolloutManager;
import com.day.cq.wcm.msm.api.RolloutManager.RolloutParams;

/**
 * The Class CreateLiveCopyServiceImpl.
 */
@Component(immediate = true, service = CreateLiveCopyService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = CreateLiveCopyServiceConfig.class)
public class CreateLiveCopyServiceImpl implements CreateLiveCopyService {

	/** The rollout manager. */
	@Reference
	RolloutManager rolloutManager;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateLiveCopyServiceImpl.class);
	/** The Constant CMD_LIVE_COPY. */
	private static final String CMD_LIVE_COPY = "createLiveCopy";
	/** The Constant CMD. */
	private static final String CMD = "cmd";
	/** The Constant WCM_COMMAND_ENDPOINT. */
	private static final String WCM_COMMAND_ENDPOINT = "/bin/wcmcommand";
	/** The Constant CHARSET. */
	private static final String CHARSET = "_charset_";
	/** The request response factory. */
	@Reference
	private RequestResponseFactory requestResponseFactory;
	/** The request processor. */
	@Reference
	private SlingRequestProcessor requestProcessor;

	/** The page manager. */
	private PageManager pageManager;

	/**
	 * This method accepts the required parameters to create a live copy and calls
	 * the WCM Command Servlet to create live copy.
	 *
	 * @param resolver       the resolver
	 * @param pagePaths      the page paths
	 * @param countries      the countries
	 * @param rolloutManager the rollout manager
	 * @param liveRelManager the live rel manager
	 * @param isDeep         the is deep
	 * @throws WCMException the WCM exception
	 */
	@Override
	public void createLiveCopy(ResourceResolver resolver, List<String> pagePaths, List<String> countries,
			RolloutManager rolloutManager, LiveRelationshipManager liveRelManager, boolean isDeep) throws WCMException {
		pageManager = resolver.adaptTo(PageManager.class);

		rolloutLiveCopies(rolloutManager, pagePaths, countries, isDeep);

	}

	/**
	 * Creates the live copy.
	 *
	 * @param resourceResolver the resource resolver
	 * @param srcPath          the src path
	 * @param destPath         the dest path
	 * @param rolloutConfigs   the rollout configs
	 */
	public void createLiveCopy(ResourceResolver resourceResolver, String srcPath, String destPath,
			String[] rolloutConfigs) {
		try {
			Page page = pageManager.getPage(srcPath);
			String pageTitle = page.getTitle();
			String pageLabel = page.getName();
			Map<String, Object> params = new HashMap<>();
			params.put(CHARSET, StandardCharsets.UTF_8);
			params.put(CMD, CMD_LIVE_COPY);
			params.put(WCMCommand.SRC_PATH_PARAM, srcPath);
			params.put(WCMCommand.DEST_PATH_PARAM, destPath);
			params.put(WCMCommand.PAGE_TITLE_PARAM, pageTitle);
			params.put(WCMCommand.PAGE_LABEL_PARAM, pageLabel);
			params.put(MSMNameConstants.PN_ROLLOUT_CONFIGS, rolloutConfigs);
			HttpServletRequest req = requestResponseFactory.createRequest("POST", WCM_COMMAND_ENDPOINT, params);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			HttpServletResponse response = requestResponseFactory.createResponse(out);
			requestProcessor.processRequest(req, response, resourceResolver);
		} catch (ServletException | IOException e) {
			LOGGER.error("An error occurred while creating live copy", e);
		}
	}

	/**
	 * Gets the root path.
	 *
	 * @param pagePath the page path
	 * @return the root path
	 */
	public static String getRootPath(final String pagePath) {
		return Text.getAbsoluteParent(pagePath, 2);
	}

	/**
	 * Rollout live copies.
	 *
	 * @param rolloutManager the rollout manager
	 * @param pagePaths      the page paths
	 * @param countries      the countries
	 * @param isDeep         the is deep
	 * @throws WCMException the WCM exception
	 */
	public void rolloutLiveCopies(RolloutManager rolloutManager, List<String> pagePaths, List<String> countries,
			boolean isDeep) throws WCMException {
		for (String path : pagePaths) {
			final Page blueprintPage = pageManager.getPage(path);
			String[] targets = targetPaths(path, countries);
			rollout(rolloutManager, isDeep, blueprintPage, targets);
		}
	}

	/**
	 * Rollout.
	 *
	 * @param rolloutManager the rollout manager
	 * @param isDeep         the is deep
	 * @param blueprintPage  the blueprint page
	 * @param targets        the targets
	 * @throws WCMException the WCM exception
	 */
	private void rollout(RolloutManager rolloutManager, boolean isDeep, final Page blueprintPage, String[] targets)
			throws WCMException {
		LOGGER.debug("inside rolloutLiveCopies method");
		final RolloutParams rolloutParams = new RolloutParams();
		rolloutParams.isDeep = isDeep;
		rolloutParams.master = blueprintPage;
		rolloutParams.targets = targets;
		rolloutParams.reset = false;
		rolloutParams.trigger = RolloutManager.Trigger.ROLLOUT;
		rolloutManager.rollout(rolloutParams);
	}

	/**
	 * Target paths.
	 *
	 * @param path      the path
	 * @param countries the countries
	 * @return the string[]
	 */
	private String[] targetPaths(String path, List<String> countries) {
		List<String> list = new ArrayList<String>();
		for (String country : countries) {
			String rootPath = getRootPath(path);
			String page = path.replace(rootPath, StringUtils.EMPTY);
			String pagePath = country + page;
			list.add(pagePath);
		}
		String[] targets = list.toArray(new String[0]);
		return targets;
	}

}
