package com.boot.jx.demo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "DemoConroller", description = "API's for Messaging", hidden = true)
@Controller
public class DemoController {
	@Autowired
	AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@Value("${swagger.auth.password}")
	String swaggerAuthPassword;

	@ApiOperation(value = "Try API's", hidden = true)
	@RequestMapping(value = { "/" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String swagger(Model model) {

		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}

		return "swagger-uix";
	}

	@ApiOperation(value = "Page", hidden = true)
	@RequestMapping(value = { "/demo" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String notp(Model model) {

		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}

		return "app-contak";
	}

}