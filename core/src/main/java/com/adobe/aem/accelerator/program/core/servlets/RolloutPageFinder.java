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
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.accelerator.program.core.beans.RolloutCountryBean;
import com.adobe.aem.accelerator.program.core.services.CreateLiveCopyService;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.day.cq.wcm.msm.api.RolloutManager;
import com.google.gson.Gson;

/**
 * The Class RolloutPageFinder.
 */
@Component(service = Servlet.class, property = { "sling.servlet.methods=" + HttpConstants.METHOD_POST,
		"sling.servlet.paths=" + "/bin/accelerator/page/finder", "sling.servlet.extensions=" + "json" })
@ServiceDescription("Servlet to find page which are either modified or newly created for rollout.")
public class RolloutPageFinder extends SlingAllMethodsServlet {

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
	protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		final ResourceResolver resolver = request.getResourceResolver();
		List<String> createdPageList = new ArrayList<>();
		List<String> modifiedPageList = new ArrayList<>();
		List<String> countriesList = new ArrayList<>();
		List<String> pagePaths = new ArrayList<>();
		Map<String, List<String>> pageinfoMap = new HashMap<String, List<String>>();
		String[] createdPage = null;
		try {
			JSONObject data = new JSONObject(request.getParameter("data"));
			if (!data.isNull("createdPage")) {
				JSONArray createdPagesArray = data.getJSONArray("createdPage");
				createdPage = getValues(createdPagesArray);
				if (Objects.nonNull(createdPage)) {
					createdPageList.addAll(Arrays.asList(createdPage));
				}
			}

			String[] modifiedPage = null;
			if (!data.isNull("modifiedPage")) {
				JSONArray modifiedPagesArray = data.getJSONArray("modifiedPage");
				if (Objects.nonNull(modifiedPagesArray)) {
					modifiedPage = getValues(modifiedPagesArray);
					modifiedPageList.addAll(Arrays.asList(modifiedPage));
				}
			}
			boolean isDeep = data.getBoolean("isDeep");
			String[] countries = null;
			if (!data.isNull("countries")) {
				JSONArray countriesArray = data.getJSONArray("countries");
				countries = getValues(countriesArray);
				if (Objects.nonNull(countries)) {
					countriesList.addAll(Arrays.asList(countries));
				}
			}
			pagePaths = mergeArrays(createdPageList, modifiedPageList);
			if (Objects.nonNull(countries) && (Objects.nonNull(modifiedPage) || Objects.nonNull(createdPage))) {
				createLiveCopyService.createLiveCopy(resolver, pagePaths, countriesList, rolloutManager, liveRelManager,
						isDeep);
			} else {
				rolloutCountries(resolver, data);
			}
		} catch (WCMException | JSONException exception) {
			LOGGER.error("An error has occured", exception);
		}
		pageinfoMap.put("createdPage", createdPageList);
		pageinfoMap.put("modifiedPage", modifiedPageList);
		pageinfoMap.put("countries", countriesList);
		response.getWriter().write(new Gson().toJson(pageinfoMap));
	}

	/**
	 * Rollout countries.
	 *
	 * @param resolver the resolver
	 * @param data     the data
	 * @throws JSONException the JSON exception
	 * @throws WCMException  the WCM exception
	 */
	private void rolloutCountries(final ResourceResolver resolver, JSONObject data) throws JSONException, WCMException {
		String templatePath = data.getString("templatePath");
		String siteRootPath = data.getString("siteRootPath");
		JSONArray jsonArray = data.getJSONArray("newCountryDetails");
		int len = jsonArray.length();
		List<RolloutCountryBean> rolloutCountryBean = new ArrayList<>();
		for (int j = 0; j < len; j++) {
			RolloutCountryBean bean = new RolloutCountryBean();
			List<String> languages = new ArrayList<>();
			JSONObject json = jsonArray.getJSONObject(j);
			bean.setName(json.getString("name"));
			bean.setTitle(json.getString("title"));
			JSONArray configs = json.getJSONArray("rolloutConfigs");
			JSONArray language = json.getJSONArray("languages");
			String[] rolloutconfigs = getValues(configs);
			bean.setRolloutConfigs(rolloutconfigs);
			languages.addAll(Arrays.asList(getValues(language)));
			bean.setLanguages(languages);
			rolloutCountryBean.add(bean);
		}
		createLiveCopyService.rolloutCountries(resolver, rolloutCountryBean, rolloutManager, liveRelManager,
				siteRootPath, templatePath);
	}

	/**
	 * Gets the values.
	 *
	 * @param configs the configs
	 * @return the values
	 * @throws JSONException the JSON exception
	 */
	private String[] getValues(JSONArray configs) throws JSONException {
		String[] stringArray = null;
		if (Objects.nonNull(configs)) {
			stringArray = new String[configs.length()];
			for (int i = 0; i < configs.length(); i++) {
				stringArray[i] = configs.getString(i);
			}
		}
		return stringArray;
	}

	/**
	 * Merge arrays.
	 *
	 * @param createdPageList  the created page list
	 * @param modifiedPageList the modified page list
	 * @return the list
	 */
	private List<String> mergeArrays(List<String> createdPageList, List<String> modifiedPageList) {
		List<String> pagePaths = new ArrayList<>();
		if (Objects.nonNull(modifiedPageList) && Objects.nonNull(createdPageList)) {
			pagePaths = Stream.of(createdPageList, modifiedPageList).flatMap(x -> x.stream())
					.collect(Collectors.toList());
		} else if (Objects.nonNull(modifiedPageList)) {
			pagePaths = createdPageList;
		} else if (Objects.nonNull(createdPageList)) {
			pagePaths = modifiedPageList;
		}
		return pagePaths;
	}

}
