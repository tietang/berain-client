[Berain](https://github.com/fengfei1000/berain)
=======


Berain is a simple high-performance coordination service for distributed applications
 
Features
---------
* HTTP Protocol & JSON
* A simple browsed UI, and the UI support Database or Zookeeper 3.4.x by configuration
* client & server support watchable event for database configuration.



[berain-client](https://github.com/fengfei1000/berain/tree/master/berain-client)
=======

see [berain-server.](https://github.com/fengfei1000/berain/tree/master/berain-server)

Install
------------- 

mvn install

Features
---------
* support berain-server.
* support Zookeeper.

Usage
-------
### maven dependency###
**add repository**

		<repository>
			<id>fengfei-repo</id>
			<name>fengfei Repository </name>
			<url>http://fengfei.googlecode.com/svn/maven-repo/releases</url>
		</repository>

**dependency:**

		<dependency>
			<groupId>fengfei.berain</groupId>
			<artifactId>berain-client</artifactId>
			<version>1.0</version>
		</dependency>

###example1:###

		Properties properties = new Properties();
		InputStream in = ZkBerainClient.class.getClassLoader().getResourceAsStream(
				"zk.properties");
		try {
			properties.load(in);
			ZkBerainClient client = new ZkBerainClient(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
 	

   zk.properties 

    zk.host=127.0.0.1
	zk.namespace=test
	zk.connectionTimeoutMs=6000
	zk.RetryNTimes=3
	zk.sleepMsBetweenRetries=3000

###example2:###

	   CuratorFramework framework = CuratorFrameworkFactory
				.builder()
				.connectString("127.0.0.1")
				.namespace("tx")
				.retryPolicy(new RetryNTimes(2, 3000))
				.connectionTimeoutMs(60000)
				.build();
		ZkBerainClient client = new ZkBerainClient(framework);
		client.start();
		String ppath = "/e1";
		client.create(ppath, "x1");
		for (int i = 0; i < 3; i++) {
			String path = ppath + "/f" + i;
			client.create(path, "x1" + i);
			for (int j = 0; j < 4; j++) {
				String cpath = path + "/h" + j;
				client.create(cpath, "x1" + i + j);
			}
		}
		System.out.println(client.nextChildren(ppath));
		client.create("c1", "x1");
		System.out.println(client.nextChildren("c1"));
		client.copy(ppath, "/a1");
		client.stop();



