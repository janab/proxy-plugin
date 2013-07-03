package com.crawljax.plugins.proxy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.bsf.util.IOUtils;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.crawljax.util.DomUtils;

/**
 * The ProxyJSInjector proxy plugin injects Javascript into the response if it is text/html content
 * and contains a <head> tag.
 * 
 * @author corpaul
 */
public class ProxyJSInjector extends ProxyPlugin {

	private String jsFile;

	public ProxyJSInjector() {
	}

	/**
	 * Constructor with config parameter.
	 * 
	 * @param filename
	 *            The file to inject.
	 */
	public ProxyJSInjector(String filename) {
		jsFile = filename;
	}

	@Override
	public String getPluginName() {
		return "ProxyJSInjector";
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
		@Override
		public Response fetchResponse(Request request) throws IOException {

			// insert JS into the response:
			// fetch the response
			Response response = this.client.fetchResponse(request);

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
					Document dom = DomUtils.asDocument(domStr);

					// try to get the head tag
					NodeList nodes = dom.getElementsByTagName("body");
					if (nodes.getLength() == 0) {
						nodes = dom.getElementsByTagName("BODY");
					}

					// no head section found
					if (nodes.getLength() == 0) {

						System.out.println("ProxyJSInjector: HTML received but"
						        + " either not valid or a snippet");
					}
					// head section found: inject the JS
					else if (nodes.getLength() == 1) {
						System.out.println("ProxyJSInjector: HTML received, insert JS");
						Element injectedJs = createInjectedJsNode(dom, jsFile);

						nodes.item(0).appendChild(injectedJs);
						// update the response with the content with the
						// injected JS
						response.setContent(DomUtils.getDocumentToByteArray(dom));
					}
					// should not happen
					else {
						System.out.println("More than one head element in HTML");
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return response;
		}

	}

	/**
	 * Creates a <script> node and adds it to the dom node.
	 * 
	 * @param dom
	 *            the node to add the script node to
	 * @param jsFile
	 *            The file to inject.
	 * @return the update node
	 * @throws IOException
	 *             On read write error.
	 */
	public Element createInjectedJsNode(Document dom, String jsFile) throws IOException {
		// create the node
		Element el = dom.createElement("script");
		el.setAttribute("type", "text/javascript");
		String js = IOUtils.getStringFromReader(new FileReader(new File(jsFile)));
		// add the javascript to the node
		el.appendChild(dom.createTextNode(js));
		return el;
	}

}
