package edu.cmu.ri.airboat.cosmUpload.cosm;

import java.awt.Color;
import java.util.HashMap;

import edu.cmu.ri.airboat.cosmUpload.cosm.httpClient.HttpClient;
import edu.cmu.ri.airboat.cosmUpload.cosm.httpClient.HttpMethod;
import edu.cmu.ri.airboat.cosmUpload.cosm.httpClient.HttpRequest;
import edu.cmu.ri.airboat.cosmUpload.cosm.httpClient.HttpResponse;
import edu.cmu.ri.airboat.cosmUpload.cosm.httpClient.SocketClient;



public class Cosm {

	/**
	 * HttpClient which will send HttpRequests to <a href="http://cosm.com"
	 * >Cosm</a>
	 */
	private HttpClient client;

	/**
	 * API key for your user account on Cosm
	 */
	private String API_KEY;

	/**
	 * Constructor
	 * 
	 * @param APIKEY
	 */
	public Cosm(String APIKEY) {
		super();
		this.API_KEY = APIKEY;
		this.client = new SocketClient("www.cosm.com");
	}

	/**
	 * Gets a Feed by Feed ID
	 * 
	 * @param feedID
	 *            Id of the Cosm feed to retrieve
	 * @return Feed which corresponds to the id provided as the parameter
	 * @throws CosmException
	 *             If something goes wrong.
	 */
	public Feed getFeed(String feedID) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ feedID + ".xml");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		HttpResponse g = client.send(hr);

		if (g.getHeaderItem("Status").equals("HTTP/1.1 200 OK")) {
			return CosmFactory.toFeed(this, g.getBody());
		} else {
			throw new CosmException(g.getHeaderItem("Status"));
		}
	}
	
	public Feed getFeed(int feedID) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ feedID + ".xml");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		HttpResponse g = client.send(hr);

		if (g.getHeaderItem("Status").equals("HTTP/1.1 200 OK")) {
			return CosmFactory.toFeed(this, g.getBody());
		} else {
			throw new CosmException(g.getHeaderItem("Status"));
		}
	}

	/**
	 * Creates a new feed from the feed provide. The feed provide should have no
	 * ID, and after this method is called is usless, to make chanegs to the new
	 * feed methods should be invoked on the return object.
	 * 
	 * @param f
	 *            Feed to create, This Feed Should have no ID field and atleast
	 *            should have its title field filled in. This feed is not 'live'
	 *            any attempt to change this object will be ignored.
	 * @return Representation of the feed from cosm, this is a 'live' Feed
	 *         and method can invoked which will change the state of the online
	 *         feed.
	 * @throws CosmException
	 *             If something goes wrong.
	 */
	public Feed createFeed(Feed f) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds.xml");
		hr.setMethod(HttpMethod.POST);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		hr.setBody(f.toXML());
		//System.out.println(hr.getHttpCommand());
		HttpResponse g = client.send(hr);

		if (g.getHeaderItem("Status").equals("HTTP/1.1 201 Created")) {

			String[] a = g.getHeaderItem("Location").split("/");
			Feed n = this.getFeed(Integer.parseInt(a[a.length - 1]));
			f = n;
			return n;
		} else {
			throw new CosmException(g.getHeaderItem("Status"));
		}
	}

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and update the Feed from there, All changes will
	 * be made to the online Feed.
	 * 
	 * @param id
	 * @param s
	 * @return
	 * @throws CosmException
	 */
	public boolean updateFeed(String id, String s) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ id + ".xml");
		hr.setMethod(HttpMethod.PUT);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		hr.setBody(s);
		HttpResponse g = client.send(hr);

		if (g.getHeaderItem("Status").equals("HTTP/1.1 200 OK")) {
			return true;
		} else {
			throw new CosmException(g.getHeaderItem("Status"));
		}
	}

	/**
	 * Delete a Feed specified by the feed id. If any Feed object exists that is
	 * a representation of the item to be deleted, they will no longer work and
	 * will throw errors if method are invoked on them.
	 * 
	 * @param feed
	 *            If of the feed to delete
	 * @return HttpResponse
	 */
	public HttpResponse deleteFeed(int feed) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ feed + ".xml");
		hr.setMethod(HttpMethod.DELETE);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		return client.send(hr);
	}
	
	/**
	 * Get all the feed of a certain user on COSM
	 * 
	 * @param username
	 *            Username whom the feeds belongs to on COSM
	 * @return HashMap of Feed names and ids
	 */
	public HashMap<String, String> getAllFeeds(String username) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds?per_page=1000&user="+username+"&content=summary");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		
		String result = client.send(hr).getBody();
	
		HashMap<String, String> feeds = new HashMap<String, String>();
		
		while(result.indexOf("{\"title\":\"") > -1)
		{
			result = result.substring(result.indexOf("{\"title\":\"")+10);
			String name = result.substring(0, result.indexOf("\","));
			result = result.substring(result.indexOf(",\"id\":")+6);
			while(result.charAt(0) == '\"')
				result = result.substring(result.indexOf(",\"id\":\"")+6);

			String id = result.substring(0,result.indexOf(','));
			feeds.put(name, id);
		}
		return feeds;
	}

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and create Datastreams from there, All changes
	 * will be made to the online Feed.
	 * 
	 * @param id
	 * @param s
	 * @return
	 * @throws CosmException
	 */
	public boolean createDatastream(String id, String s) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ id + "/datastreams.xml");
		hr.setMethod(HttpMethod.POST);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		
		hr.setBody(s);
		HttpResponse g = client.send(hr);

		if (g.getHeaderItem("Status").equals("HTTP/1.1 201 Created")) {
			return true;
		} else {
			throw new CosmException(g.getHeaderItem("Status"));
		}
	}

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and delete Datastreams from there, All changes
	 * will be made to the online Feed.
	 * 
	 * @param id
	 * @param datastream
	 * @return
	 */
	public HttpResponse deleteDatastream(String id, String datastream) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ id + "/datastreams/" + datastream + ".xml");
		hr.setMethod(HttpMethod.DELETE);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		return client.send(hr);
	}

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and update Datastreams from there, All changes
	 * will be made to the online Feed.
	 * 
	 * @param feed
	 * @param string
	 * @param s
	 * @return
	 */
	public HttpResponse updateDatastream(String feed, String string, String s) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ feed + "/datastreams/" + string + ".xml");
		hr.setMethod(HttpMethod.PUT);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		hr.setBody(s);
		//System.out.println(hr.getHttpCommand());
		return client.send(hr);
	}
	
	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and update Datastreams from there, All changes
	 * will be made to the online Feed.
	 * 
	 * @param feed
	 * @param string
	 * @param s
	 * @return
	 */
	public HttpResponse updateDatastream(String feed, String string, String s, String timeStamp) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ feed + "/datastreams/" + string + "/datapoints.xml");
		hr.setMethod(HttpMethod.POST);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		hr.setBody(s);
		//System.out.println(hr.getHttpCommand());
		return client.send(hr);
	}
	

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and get Datastreams from there.
	 * 
	 * @param feed
	 * @param datastream
	 * @return
	 */
	public HttpResponse getDatastream(String feed, String datastream) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/"
				+ feed + "/datastreams/" + datastream + ".xml");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		return client.send(hr);
	}

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and access Datastream history from there.
	 * 
	 * @param feed
	 * @param datastream
	 * @return
	 */
	public Double[] getDatastreamHistory(String feed, String datastream) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/" + feed
				+ "/datastreams/" + datastream + ".csv?" +
						"duration=6hours" +
						"&interval=0");
		
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey",  this.API_KEY);
		 
		String str = client.send(hr).getBody();
		
		str = str.replaceAll("\n", ",");
		
		String[] arr = str.split(",");
		Double[] arr1 = new Double[arr.length/2];
		for (int i = 0; i < arr.length/2; i++) {
			arr1[i] = Double.parseDouble(arr[(i*2)+1]);
		}

		return arr1;

	}

	/**
	 * This Method is not intended to be used by Users, instead get the Feed
	 * object using getFeed() and access Datastream archive from there.
	 * 
	 * @param feed
	 * @param datastream
	 * @return
	 */
	public String[] getDatastreamArchive(String feed, String datastream) {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/feeds/" + feed
				+ "/datastreams/" + datastream + "/archive.csv");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		String str = client.send(hr).getBody();
		return str.split("\n");

	}

	/**
	 * Creates a Trigger on cosm from the object provided.
	 * 
	 * @param t
	 * @return
	 * @throws CosmException
	 */
	public String createTrigger(Trigger t) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/triggers");
		hr.setMethod(HttpMethod.POST);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		hr.setBody(t.toString());
		HttpResponse h = client.send(hr);
		if (h.getHeaderItem("Status").equals("HTTP/1.1 201 Created")) {
			return h.getHeaderItem("Location");
		} else {
			throw new CosmException(h.getHeaderItem("Status"));
		}

	}
	
	/**
	 * Gets a Trigger from cosm specified by the parameter
	 * 
	 * @param id id of the Trigger to get
	 */
	public Trigger getTrigger(int id) throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/triggers/"+id+".xml");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		HttpResponse h = client.send(hr);
		
		return CosmFactory.toTrigger(h.getBody())[0];

	}
	
	/**
	 * Gets all the Triggers owned by the authenticating user
	 * 
	 * @param id id of the Trigger to get
	 */
	public Trigger[] getTriggers() throws CosmException {
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/triggers/");
		hr.setMethod(HttpMethod.GET);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		HttpResponse h = client.send(hr);
		
		return CosmFactory.toTrigger(h.getBody());

	}
	
	/**
	 * Deletes a Trigger from cosm
	 * @param id id of the trigger to delete
	 * @return
	 */
	public HttpResponse deleteTrigger(int id){
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/triggers/"+id);
		hr.setMethod(HttpMethod.DELETE);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		return client.send(hr);
		
	}
	
	/**
	 * Updates a Trigger on cosm
	 * @param id id of the triggerto update
	 * @param t Trigger object of the new trigger
	 * @return
	 */
	public HttpResponse updateTrigger(int id,Trigger t){
		HttpRequest hr = new HttpRequest("http://api.cosm.com/v2/triggers/"+id);
		hr.setMethod(HttpMethod.PUT);
		hr.addHeaderItem("X-ApiKey", this.API_KEY);
		hr.setBody(t.toString());
		return client.send(hr);
		
	}

	/**
	 * Gets a Cosm graph of the datastream
	 * 
	 * @param id
	 *            ID of feed the datastream belongs to.
	 * @param id2
	 *            ID of the stream to graph
	 * @param width
	 *            Width of the image
	 * @param height
	 *            Height of the image
	 * @param c
	 *            Color of the line
	 * @return String which can be used to form a URL Object.
	 */
	public String showGraph(String id, String id2, int width, int height,
			Color c) {
		String hexRed = Integer.toHexString(c.getRed()).toString();
		String hexGreen = Integer.toHexString(c.getGreen()).toString();
		String hexBlue = Integer.toHexString(c.getBlue()).toString();
		if (hexRed.length() == 1) {
			hexRed = "0" + hexRed;
		}

		if (hexGreen.length() == 1) {
			hexGreen = "0" + hexGreen;
		}
		if (hexBlue.length() == 1) {
			hexBlue = "0" + hexBlue;
		}
		String hex = (hexRed + hexGreen + hexBlue).toUpperCase();

		return "http://api.cosm.com/v2/feeds/" + id + "/datastreams/"
				+ id2 + ".png?w=" + width + "&h=" + height + "&c="
				+ hex + "&g=true";

	}

	/**
	 * @return the client
	 */
	public HttpClient getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(HttpClient client) {
		this.client = client;
	}

}
