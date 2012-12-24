package com.crawljax.plugins.proxy;

import java.io.IOException;
import java.util.UUID;

import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.Helper;

public class ProxyEventlessTagsInjector extends ProxyPlugin {

	@Override
	public String getPluginName() {
		return "ProxyEventlessTagsInjector";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}

	/**
	 * The actual WebScarab plugin.
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
		 * Handle the request and response. This plugin only handles the response: If the response
		 * contains HTML and is a complete document, e.g. contains a <head> tag, it adds the
		 * configured Javascript content to it.
		 * 
		 * @param request
		 *            The incoming request.
		 * @throws IOException
		 *             On read write error.
		 * @return The new response.
		 */
		public Response fetchResponse(Request request) throws IOException {

			// insert JS into the response:
			// fetch the response
			Response response = this.client.fetchResponse(request);
			String attrName = "requestforproxyid";

			if (response == null) {
				return null;
			}

			// parse the response body
			try {
				String contentType = response.getHeader("Content-Type");
				if (contentType == null) {
					return response;
				}
				if (contentType.contains("text/html")) {
					// parse the content
					String domStr = new String(response.getContent());
					Document dom = Helper.getDocument(domStr);

					// try to get the head tag
					NodeList nodes = dom.getElementsByTagName("script");
					for (int i = 0; i < nodes.getLength(); i++) {
						injectNode((Element) nodes.item(i), attrName);
					}
					nodes = dom.getElementsByTagName("img");
					for (int i = 0; i < nodes.getLength(); i++) {
						injectNode((Element) nodes.item(i), attrName);
					}

					response.setContent(Helper.getDocumentToByteArray(dom));

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return response;
		}

	}

	/**
	 * Inject an element in a node.
	 * 
	 * @param e
	 *            The element.
	 * @param attrName
	 *            The attribute name.
	 */
	public static void injectNode(Element e, String attrName) {
		NamedNodeMap attributes = e.getAttributes();
		if (attributes != null) {
			Node src = attributes.getNamedItem("src");
			if (src != null) {
				String attrValue = UUID.randomUUID().toString();
				String newSrc = src.getTextContent();
				if (src.getTextContent().indexOf("?") > 0) {
					newSrc += "&";
				} else {
					newSrc += "?";
				}
				newSrc += attrName + "=" + attrValue;
				System.out.println("Setting src to: " + newSrc);
				src.setTextContent(newSrc);
				e.setAttribute(attrName, attrValue);

			}
		}
	}

}
