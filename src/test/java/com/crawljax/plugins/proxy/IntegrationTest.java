package com.crawljax.plugins.proxy;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.crawltests.SimpleSiteCrawl;

public class IntegrationTest {

	private JSInjectorProxyAddon jsInjector;
	private RequestBufferProxyAddon requestBuffer;

	@Before
	public void setup() throws FileNotFoundException {

		SimpleSiteCrawl simpleSiteCrawl = new SimpleSiteCrawl() {
			@Override
			protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				return super.newCrawlConfigurationBuilder().setProxyConfig(
				        ProxyConfiguration.manualProxyOn("localhost", 8084));
			}
		};

		WebScarabProxyPlugin proxy = new WebScarabProxyPlugin();
		proxy.addPlugin(new CacheDisablerProxyAddon());

		proxy.addPlugin(new EventlessTagsInjectorProxyAddon());

		jsInjector = new JSInjectorProxyAddon(new File("foo.js"));
		proxy.addPlugin(jsInjector);

		requestBuffer = new RequestBufferProxyAddon(true);
		proxy.addPlugin(requestBuffer);

		simpleSiteCrawl.crawlWith(proxy);
	}

	@Test
	public void requestBufferSizeMatchers() {
		assertThat(requestBuffer.getCompleteBuffer(), anyOf(hasSize(11), hasSize(10)));
	}
}
