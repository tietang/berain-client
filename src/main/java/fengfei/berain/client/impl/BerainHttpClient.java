package fengfei.berain.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import fengfei.berain.client.BerainClient;
import fengfei.berain.client.BerainEntry;
import fengfei.berain.client.BerainWatchedEvent;
import fengfei.berain.client.EventType;
import fengfei.berain.client.Wather;

public class BerainHttpClient implements Runnable, BerainClient {

	public final static String COOKIE_USERNAME = "berain_user";
	public final static String COOKIE_PASSWORD = "berain_pwd";
	public final String ROOT_PATH = "/";
	public final String SEPARATOR = "/";
	//
	private static WatchedService watchedService = WatchedService.get();
	private String clientId;
	public WatchedContainer watched = new WatchedContainer();
	private static Queue<DefaultHttpClient> httpClients = new ConcurrentLinkedQueue<>();
	String baseurl;
	private String username;
	private String password;
	private boolean isLogon = false;
	ObjectMapper mapper = new ObjectMapper();

	public BerainHttpClient(String baseurl, String username, String password) {
		super();
		this.baseurl = baseurl;
		this.username = username;
		this.password = password;
		clientId = UUID.randomUUID().toString();
		watchedService.addBerainClient(this);
	}

	public void start() {
		watchedService.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				BerainHttpClient.this.stop();
			}
		});
	}

	public void stop() {
		removeAllListener();
		watchedService.stop();
	}

	public void login() {
		login(username, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#login()
	 */
	@Override
	public void login(String username, String password) {
		DefaultHttpClient httpclient = getHttpClient();
		try {
			List<Cookie> cookies = httpclient.getCookieStore().getCookies();
			if (cookies.isEmpty()) {
				System.out.println("None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					System.out.println("- " + cookies.get(i).toString());
				}
			}
			HttpPost httpost = new HttpPost(baseurl + "/logon");
			System.out.println("executing request " + httpost.getURI());
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("username", username));
			nvps.add(new BasicNameValuePair("password", password));
			nvps.add(new BasicNameValuePair("remember", "1"));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpost, responseHandler);
			isLogon = Boolean.parseBoolean(responseBody);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnHttpClient(httpclient);
		}
	}

	private static DefaultHttpClient getHttpClient() {
		DefaultHttpClient httpclient = httpClients.poll();
		if (httpclient == null) {
			httpclient = new DefaultHttpClient();
		}
		return httpclient;
	}

	private static void returnHttpClient(DefaultHttpClient httpclient) {
		httpClients.offer(httpclient);
	}

	public static void main(String[] args) throws Exception {
		BerainHttpClient client = new BerainHttpClient(
				"http://127.0.0.1:8021/",
				"admin",
				"passowrd");
		client.start();
		// client.login();
		client.addWatchable("/berain/w1", EventType.DataChanged, new Wather() {

			@Override
			public void call(BerainWatchedEvent event) {
				System.out.println("DataChanged:================ " + event);
			}
		});
		client.addWatchable("/berain/w1", EventType.ChildrenChanged, new Wather() {

			@Override
			public void call(BerainWatchedEvent event) {
				System.out.println("ChildrenChanged:================ " + event);
			}
		});
		List<BerainEntry> s = client.nextChildren("/berain");
		System.out.println(s);
	}

	// --------------------------write-----------------------------//
	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#update(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean update(String path, String value) {
		try {
			BerainResult<Boolean> br = httpRequest(
					null,
					"/berain/update",
					"path",
					path,
					"value",
					value);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	public <T, E> BerainResult<T> httpRequest(
			Class<? extends BerainResult<T>> clazz,
			String httpPath,
			String... params) throws Exception {
		DefaultHttpClient httpclient = getHttpClient();
		BerainResult<T> br = null;
		HttpGet httpget = null;
		try {
			URIBuilder builder = new URIBuilder(baseurl);
			builder.setPath(httpPath);
			if (params != null && params.length % 2 == 0) {
				for (int i = 0; i < params.length; i++) {
					builder.setParameter(params[i++], params[i]);
				}
			}
			httpget = new HttpGet(builder.build());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);
			if (clazz == null) {
				br = mapper.readValue(responseBody, BerainResult.class);
			} else {
				br = mapper.readValue(responseBody, clazz);
			}
		} finally {
			httpget.releaseConnection();
			returnHttpClient(httpclient);
		}
		return br;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#create(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean create(String path, String value) {
		try {
			BerainResult<Boolean> br = httpRequest(
					null,
					"/berain/create",
					"path",
					path,
					"value",
					value);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#delete(java.lang.String)
	 */
	@Override
	public boolean delete(String path) {
		try {
			BerainResult<Boolean> br = httpRequest(null, "/berain/delete", "path", path);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#copy(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean copy(String originalPath, String newPath) {
		try {
			BerainResult<Boolean> br = httpRequest(
					null,
					"/berain/copy",
					"originalPath",
					originalPath,
					"newPath",
					newPath);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	// --------------------------read-----------------------------//
	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#nextChildren(java.lang.String)
	 */
	@Override
	public List<BerainEntry> nextChildren(String parentId) {
		try {
			BerainResult<List<BerainEntry>> br = httpRequest(
					BerainEntryResults.class,
					"/berain/nextChildren",
					"parentId",
					parentId);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#get(java.lang.String)
	 */
	@Override
	public String get(String path) {
		try {
			BerainResult<String> br = httpRequest(null, "/berain/get", "path", path);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#getFull(java.lang.String)
	 */
	@Override
	public BerainEntry getFull(String path) {
		try {
			BerainResult<BerainEntry> br = httpRequest(
					BerainEntryResult.class,
					"/berain/getFull",
					"path",
					path);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private static class BerainEntryResult extends BerainResult<BerainEntry> {
	}

	private static class BerainEntryResults extends BerainResult<List<BerainEntry>> {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String path) {
		try {
			BerainResult<Boolean> br = httpRequest(null, "/berain/getFull", "path", path);
			return br.data;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	// --------------------------Event-----------------------------//
	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#addWatchable(java.lang.String,
	 * int, fengfei.berain.client.Wather)
	 */
	// @Override
	// public void addWatchable(String path, int type, Wather wather) {
	// try {
	// BerainResult<Boolean> br = httpRequest(
	// null,
	// "/berain/addWatchable",
	// "clientId",
	// clientId,
	// "path",
	// path,
	// "type",
	// String.valueOf(type));
	// watched.addWatchedEvent(new BerainWatchedEvent(
	// EventType.fromInt(type),
	// path,
	// wather));
	// } catch (Throwable e) {
	// e.printStackTrace();
	// }
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#addWatchable(java.lang.String,
	 * fengfei.berain.client.EventType, fengfei.berain.client.Wather)
	 */
	@Override
	public void addWatchable(String path, EventType type, Wather wather) {
		try {
			BerainResult<Boolean> br = httpRequest(
					null,
					"/berain/addWatchable",
					"clientId",
					clientId,
					"path",
					path,
					"type",
					String.valueOf(type.getIntValue()));
			watched.addWatchedEvent(new BerainWatchedEvent(type, path, wather));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addChildrenChangedWatcher(String path, Wather wather) throws Exception {
		addWatchable(path, EventType.ChildrenChanged, wather);
	}

	@Override
	public void addNodeChangedWatcher(String path, Wather wather) throws Exception {
		addWatchable(path, EventType.DataChanged, wather);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#removeWatchable(java.lang.String,
	 * int)
	 */
	@Override
	public void removeWatchable(String path, int type) {
		try {
			BerainResult<Boolean> br = httpRequest(
					null,
					"/berain/removeWatchable",
					"clientId",
					clientId,
					"path",
					path,
					"type",
					String.valueOf(type));
			watched.removeWatchedEvent(new BerainWatchedEvent(EventType.fromInt(type), path));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void removeRemoteWatchedEvent(String path, int type) {
		try {
			BerainResult<Boolean> br = httpRequest(
					null,
					"/berain/removeWatchedEvent",
					"clientId",
					clientId,
					"path",
					path,
					"type",
					String.valueOf(type));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#listChangedNodes()
	 */
	@Override
	public Map<String, List<BerainWatchedEvent>> listChangedNodes() {
		DefaultHttpClient httpclient = getHttpClient();
		try {
			HttpPost httpost = new HttpPost(baseurl + "/berain/listChangedNodes");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Map<String, Set<BerainWatchedEvent>> watchedEvents = watched.getWatchedEvents();
			for (Entry<String, Set<BerainWatchedEvent>> entry : watchedEvents.entrySet()) {
				Set<BerainWatchedEvent> ents = entry.getValue();
				for (BerainWatchedEvent event : ents) {
					nvps.add(new BasicNameValuePair("paths", event.getPath()));
					nvps.add(new BasicNameValuePair("types", String.valueOf(event
							.getEventType()
							.getIntValue())));
				}
			}
			nvps.add(new BasicNameValuePair("clientId", clientId));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpost, responseHandler);
			BerainResult<Map<String, List<BerainWatchedEvent>>> br = mapper.readValue(
					responseBody,
					WatchedEventResult.class);
			return br.data;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnHttpClient(httpclient);
		}
		// try {
		//
		//
		//
		// BerainResult<Map<String, List<WatchedEvent>>> br = httpRequest(
		// WatchedEventResult.class,
		// "/berain/listChangedNodes",
		// "clientId",
		// clientId);
		//
		// return br.data;
		//
		// } catch (Throwable e) {
		// e.printStackTrace();
		// }
		return null;
	}

	public static class WatchedEventResult
			extends
			BerainResult<Map<String, List<BerainWatchedEvent>>> {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fengfei.berain.client.BerainClient#removeAllListener()
	 */
	@Override
	public void removeAllListener() {
		try {
			try {
				BerainResult<Boolean> br = httpRequest(
						null,
						"/berain/removeAllListener",
						"clientId",
						clientId);
				watched.clearAllWatchedEvent();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Map<String, Set<BerainWatchedEvent>> all = watched.getWatchedEvents();
			Map<String, List<BerainWatchedEvent>> watchedEvents = this.listChangedNodes();
			if (watchedEvents == null) {
				return;
			}
			for (Entry<String, List<BerainWatchedEvent>> entry : watchedEvents.entrySet()) {
				String path = entry.getKey();
				List<BerainWatchedEvent> events = entry.getValue();
				Set<BerainWatchedEvent> ableEvents = all.get(path);
				if (ableEvents == null) {
					continue;
				}
				for (BerainWatchedEvent event : events) {
					for (BerainWatchedEvent ableEvent : ableEvents) {
						// System.out.printf(
						// " %s ==  %s\n",
						// event.toString(),
						// ableEvent.toString());
						if (ableEvent.equals(event)) {
							// BerainEntry data = this.getFull(path);
							Wather wather = ableEvent.getWather();
							// sync
							wather.call(ableEvent);
							// async
							// wather.setData(data);
							// watchedService.execute(wather);
							// remove
							removeRemoteWatchedEvent(event.getPath(), event
									.getEventType()
									.getIntValue());
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
