/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli.requestmappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class RequestMappingsParser20 {

	public static Collection<RequestMapping> parse(JSONObject obj) {
		obj = obj.getJSONObject("contexts");
		List<RequestMapping> result = new ArrayList<>();
		for (String contextId : obj.keySet()) {
			//Contains 3 different keys now ('dispatcherServlets', 'servletFilters' and 'servlets'.
			// Each with their own kind of data inside. Looks like 'dispatcherServlets' contains stuff similar to what we
			// know from Boot 1.x but in slighly different form. We only parse that stuff for now.
			JSONObject mappings = obj
					.getJSONObject(contextId)
					.getJSONObject("mappings");
			JSONArray rmArray = null;
			if (mappings.has("dispatcherServlets") && mappings.getJSONObject("dispatcherServlets").has("dispatcherServlet")) {
				// Regular Web starter endpoints RMs JMX beans format
				rmArray = mappings.getJSONObject("dispatcherServlets").getJSONArray("dispatcherServlet");
			} else if (mappings.has("dispatcherHandlers") && mappings.getJSONObject("dispatcherHandlers").has("webHandler")) {
				// WebFlux endpoints RMs JMX bean format
				rmArray = mappings.getJSONObject("dispatcherHandlers").getJSONArray("webHandler");
			}
			if (rmArray != null) {
				for (Object e : rmArray) {
					JSONObject rm = (JSONObject)e;
					result.add(new Boot20DispatcherServletMapping(rm));
				}
			}
		}
		return result;
	}

}
