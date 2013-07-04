package com.crawljax.plugins.proxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.model.StoreException;
import org.owasp.webscarab.plugin.Framework;
import org.owasp.webscarab.plugin.proxy.Proxy;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.plugin.PreCrawlingPlugin;

public class WebScarabProxyPlugin implements PreCrawlingPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebScarabProxyPlugin.class);

	/**
	 * List of proxy plugins that should be added to the proxy before it is started.
	 */
	private List<ProxyPlugin> plugins = new ArrayList<ProxyPlugin>();

	/**
	 * The WebScarab HTTP proxy object this class is a wrapper for.
	 */
	private Proxy proxy;

	public WebScarabProxyPlugin() {
		// Disable the logging to console of the web scrabber.
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) {
		try {
			startProxy(config.getProxyConfiguration());
			LOGGER.info("WebScarab proxy started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the HTTP proxy on the specified port. Also starts the request buffer plugin.
	 * 
	 * @param config
	 *            ProxyConfiguration object.
	 * @throws IOException
	 *             When error reading writing.
	 * @throws StoreException
	 *             When error storing preference.
	 */
	private void startProxy(ProxyConfiguration config) throws IOException, StoreException {

		Framework framework = new Framework();

		/* set listening port before creating the object to avoid warnings */
		Preferences.setPreference("Proxy.listeners",
		        config.getHostname() + ":" + config.getPort());

		this.proxy = new Proxy(framework);

		/* add the plugins to the proxy */
		for (ProxyPlugin p : plugins) {
			proxy.addPlugin(p);
		}

		// framework.setSession("BlackHole", null, "");
		framework.setSession("FileSystem", new File("conversationRecords"), "");

		/* start the proxy */
		this.proxy.run();
	}

	/**
	 * Add a plugin to the proxy. IMPORTANT: call this before the proxy is actually started.
	 * 
	 * @param plugin
	 *            The plugin to add.
	 */
	public void addPlugin(ProxyPlugin plugin) {
		plugins.add(plugin);
	}

}
