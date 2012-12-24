package com.crawljax.plugins.proxy;

import java.io.IOException;

import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;

/**
 * This class is a plugin for the WebScarab proxy. It removes headers from the request to make it
 * appear as if it is the first time the page is requested. This is done to make sure the server
 * does not return a 304 Not Modified message (assumes the page is in the browser cache). When 304
 * messages are returned the proxy cannot inject Javascript.
 * 
 * @author corpaul
 * 
 *         TODO: all, can also use existing plugin in WebScarab
 */
public class ProxyCacheDisabler extends ProxyPlugin {

	/**
	 * Constructor.
	 */
	public ProxyCacheDisabler() {

	}

	/**
	 * Returns the plugin name.
	 * 
	 * @return The name.
	 */
	public String getPluginName() {
		return new String("Cache Disabler");
	}

	/**
	 * Get a new proxy plugin.
	 * 
	 * @param in
	 *            The HTTPClient for the plugin.
	 * @return The plugin.
	 */
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}

	/**
	 * The actual Webscarab plugin.
	 * 
	 * @author Cor Paul
	 */
	private class Plugin implements HTTPClient {

		/**
		 * The HTTPClient incoming.
		 */
		private HTTPClient client;

		/**
		 * Constructor.
		 * 
		 * @param in
		 *            HTTPClient
		 */
		public Plugin(HTTPClient in) {
			this.client = in;
		}

		/**
		 * Modify response.
		 * 
		 * @param request
		 *            The incoming request.
		 * @throws IOException
		 *             Thrown on read or write error.
		 * @return The new, modified response.
		 */
		public Response fetchResponse(Request request) throws IOException {
			disableCache(request);
			// response currently unused in this plugin since we only care
			// about outgoing requests
			Response response = this.client.fetchResponse(request);
			return response;
		}

		/**
		 * Make sure that the request does not indicate a cached version in any way.
		 * 
		 * @param request
		 *            The incoming request.
		 */
		public void disableCache(Request request) {
			// break cache control:
			// the server may only return a 304 code if the request
			// contains a IMS header
			request.deleteHeader("If-Modified-Since");
			// make sure the browser cache is not used by removing
			// the INM header
			request.deleteHeader("If-None-Match");
			// ETag may also be used for caching
			request.deleteHeader("ETag");

		}

	}

}
