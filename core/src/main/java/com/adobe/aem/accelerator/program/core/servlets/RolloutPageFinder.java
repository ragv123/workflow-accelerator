package com.adobe.aem.accelerator.program.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.services.CreateLiveCopyService;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutManager;
import com.google.gson.Gson;

/**
 * The Class RolloutPageFinder.
 */
@Component(service = Servlet.class, property = { "sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/accelerator/page/finder", "sling.servlet.extensions=" + "json" })
@ServiceDescription("Servlet to find page which are either modified or newly created for rollout.")
public class RolloutPageFinder extends SlingSafeMethodsServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5182704908085675193L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RolloutPageFinder.class);

	/** The live rel manager. */
	@Reference
	LiveRelationshipManager liveRelManager;
	/** The create live copy service. */
	@Reference
	private CreateLiveCopyService createLiveCopyService;

	/** The rollout manager. */
	@Reference
	RolloutManager rolloutManager;

	/**
	 * Do get.
	 *
	 * @param request  the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException      Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		final ResourceResolver resolver = request.getResourceResolver();
		List<String> createdPageList = new ArrayList<>();
		List<String> modifiedPageList = new ArrayList<>();
		List<String> countriesList = new ArrayList<>();
		List<String> pagePaths = new ArrayList<>();
		Map<String, List<String>> pageinfoMap = new HashMap<String, List<String>>();
		String[] createdPage = request.getParameterValues("createdPage");
		if (Objects.nonNull(createdPage)) {
			createdPageList.addAll(Arrays.asList(createdPage));
		}
		String[] modifiedPage = request.getParameterValues("modifiedPage");
		if (Objects.nonNull(modifiedPage)) {
			modifiedPageList.addAll(Arrays.asList(modifiedPage));
		}
		String[] countries = request.getParameterValues("countries");
		boolean isDeep = Boolean.parseBoolean(request.getParameter("isDeep"));
		countriesList.addAll(Arrays.asList(countries));
		pagePaths = mergeArrays(createdPageList, modifiedPageList);
		try {
			createLiveCopyService.createLiveCopy(resolver, pagePaths, countriesList, rolloutManager, liveRelManager,
					isDeep);
		} catch (WCMException exception) {
			LOGGER.error("An error has occured", exception);
		}
		pageinfoMap.put("createdPage", createdPageList);
		pageinfoMap.put("modifiedPage", modifiedPageList);
		pageinfoMap.put("countries", countriesList);
		response.getWriter().write(new Gson().toJson(pageinfoMap));
	}

	/**
	 * Merge arrays.
	 *
	 * @param createdPageList  the created page list
	 * @param modifiedPageList the modified page list
	 * @return the list
	 */
	private List<String> mergeArrays(List<String> createdPageList, List<String> modifiedPageList) {
		List<String> pagePaths = Stream.of(createdPageList, modifiedPageList).flatMap(x -> x.stream())
				.collect(Collectors.toList());
		return pagePaths;
	}

}
