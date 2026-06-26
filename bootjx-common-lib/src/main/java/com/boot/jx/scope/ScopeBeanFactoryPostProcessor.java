package com.boot.jx.scope;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.boot.jx.AppConstants;
import com.boot.jx.scope.tnt.TenantScope;
import com.boot.jx.scope.vendor.VendorScope;

public class ScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
		// System.out.println("ScopeBeanFactoryPostProcessor:postProcessBeanFactory");
		factory.registerScope("tenant", new TenantScope());
		factory.registerScope("thread", ThreadScope.SCOPE);
		factory.registerScope(AppConstants.Scopes.VENDOR, new VendorScope());
	}
}