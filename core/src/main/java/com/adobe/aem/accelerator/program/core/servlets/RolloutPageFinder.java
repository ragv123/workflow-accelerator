package com.adobe.aem.accelerator.program.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.adobe.aem.accelerator.program.core.beans.RolloutPageInfo;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;
import com.google.gson.Gson;

@Component(service = Servlet.class, property = { "sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/accelerator/page/finder", "sling.servlet.extensions=" + "json" })
@ServiceDescription("Servlet to find page which are either modified or newly created for rollout.")
public class RolloutPageFinder extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 1L;

	@Reference
	LiveRelationshipManager relationMgr;

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		List<String> createdPageList = new ArrayList<>();
		List<String> modifiedPageList = new ArrayList<>();
		List<String> countriesList = new ArrayList<>();
		Map<String, List<String>> pageinfoMap = new HashMap<String, List<String>>();

		String[] createdPage = request.getParameterValues("createdPage");
		createdPageList.addAll(Arrays.asList(createdPage));
		String[] modifiedPage = request.getParameterValues("modifiedPage");
		modifiedPageList.addAll(Arrays.asList(modifiedPage));
		String[] countries = request.getParameterValues("countries");
		countriesList.addAll(Arrays.asList(countries));

		pageinfoMap.put("createdPage", createdPageList);
		pageinfoMap.put("modifiedPage", modifiedPageList);
		pageinfoMap.put("countries", countriesList);
		response.getWriter().write(new Gson().toJson(pageinfoMap));
	}

}
