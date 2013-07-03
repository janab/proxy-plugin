package com.crawljax.plugins.proxy;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.crawltests.SimpleSiteCrawl;

public class IntegrationTest {

	private ProxyJSInjector jsInjector;
	private ProxyRequestBuffer requestBuffer;

	@Before
	public void setup() {

		SimpleSiteCrawl simpleSiteCrawl = new SimpleSiteCrawl() {
			@Override
			protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				return super.newCrawlConfigurationBuilder().setProxyConfig(
				        ProxyConfiguration.manualProxyOn("127.0.0.1", 8084));
			}
		};

		WebScarabWrapper proxy = new WebScarabWrapper();
		proxy.addPlugin(new ProxyCacheDisabler());

		proxy.addPlugin(new ProxyEventlessTagsInjector());

		jsInjector = new ProxyJSInjector();
		proxy.addPlugin(jsInjector);

		requestBuffer = new ProxyRequestBuffer(true);
		proxy.addPlugin(requestBuffer);

		simpleSiteCrawl.crawlWith(proxy);
	}

	@Test
	public void requestBufferSizeMatchers() {
		assertThat(requestBuffer.getCompleteBuffer(), anyOf(hasSize(11), hasSize(10)));
	}
}
