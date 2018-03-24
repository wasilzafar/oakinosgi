package com.wzee.oak;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class Starter {

	public static void main(String[] args) {
		System.setProperty("felix.fileinstall.dir", "config");
		 Properties props = new Properties();
		 ResourceProvider rp = new ResourceProvider(Thread.currentThread().getContextClassLoader());
		 Starter launcher = new Starter(props, rp);
		 boolean launched = launcher.launch();
		 System.out.println("Framwork launch complete : " + launched);

	}
	static final String MEMORY_LAUNCHER_PROPERTIES = "defaultFramework-Memory.properties";
	static boolean LAUNCH_RESULT = false;
	static final String LAUNCHER_PREFIX = "memory://";
	protected Framework framework;
	protected FrameworkFactory factory;
	ResourceProvider resourceProvider = null;
	Properties constructProperties = null;
	
	public Starter(Properties props, ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.constructProperties = props;
        
	}
	

	protected void setFramework(Framework framework) {
		this.framework = framework;
	}


	protected void start(FrameworkFactory factory, Map configProperties) {
		framework = factory.newFramework(configProperties);
		try {
			framework.init();
			framework.start();
		} catch (BundleException e) {
			LoggerDelegate.error(e.getMessage(), e);
		}
	}

	protected FrameworkFactory getFrameworkFactoryNoServiceLoader() throws Exception {
		URL url = resourceProvider.getResource(
				"META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		if (url != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));
			try {
				for (String s = br.readLine(); s != null; s = br.readLine()) {
					s = s.trim();
					if ((s.length() > 0) && (s.charAt(0) != '#')) {
						return (FrameworkFactory) Class.forName(s)
								.newInstance();
					}
				}
			} finally {
				if (br != null)
					br.close();
			}
		}
		throw new Exception("Could not find framework factory.");
	}
	
	protected FrameworkFactory getFrameworkFactory(){
		ServiceLoader<FrameworkFactory> factoryLoader =	ServiceLoader.load(FrameworkFactory.class);
		Iterator<FrameworkFactory> factoryItr = factoryLoader.iterator();
		factory =  factoryItr.next();
		return factory;
		
	}
		
	public Bundle installBundle(InputStream bundleInputStream, BundleContext context, String bundleLocation) {
		Bundle bundleInstalled = null;
		if(bundleInputStream != null)
		try {
			bundleInstalled = context.installBundle(bundleLocation, bundleInputStream);
		} catch (BundleException e) {
			LoggerDelegate.error(e.getMessage(), e);
		}
		return bundleInstalled;
	}

	public void startBundle(Bundle bundle) {
		try {
			bundle.start();
			LMSLogManager.LOGGER.log(Level.INFO,"Bundle state :: "+getState(bundle.getState()));
			printBundleInfo(bundle);
		} catch (BundleException e) {
			LoggerDelegate.error(e.getMessage(), e);
		}
	}
	
	public String getState(int state) {
		switch (state) {
		case Bundle.ACTIVE:
			return "ACTIVE";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		default:
			break;
		}
		return "NULL";
	}
	
	void printBundleInfo(Bundle bndle) {
		LoggerDelegate.info("Activated bundle :: "+bndle.getSymbolicName(), null);	
		}
	
	public boolean checkMemoryRequirement() {
		return false;
	}

	
	Properties loadFrameworkProperties() {
		URL propURL;
		Properties props = new Properties();
		propURL = resourceProvider.getResource("bundles/"+MEMORY_LAUNCHER_PROPERTIES);
		try {
			props.load(propURL.openStream());
			System.out.println("Loaded properties : "+props.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	public void loadBundles() {
		List installedBndList = new ArrayList();

		Iterator<String> bundleResources = resourceProvider.getChildren("bundles/");
		installBundles(bundleResources, installedBndList);

		LMSLogManager.LOGGER.log(Level.INFO, "Total bundle count : " + installedBndList.size());
		LMSLogManager.LOGGER.log(Level.INFO, "Printing bundle state info ");
		for (Iterator iterator = installedBndList.iterator(); iterator.hasNext();) {
			Bundle bundle = (Bundle) iterator.next();
			LMSLogManager.LOGGER.log(Level.INFO,
					bundle.getSymbolicName() + " is in state : " + getState(bundle.getState()));
		}

		for (Iterator iterator = installedBndList.iterator(); iterator.hasNext();) {
			Bundle bundle = (Bundle) iterator.next();
			LMSLogManager.LOGGER.log(Level.INFO,
					bundle.getSymbolicName() + " being started with state : " + getState(bundle.getState()));
			if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
				if (!isFragment(bundle)) {
					LMSLogManager.LOGGER.log(Level.INFO,
							"Startng bundle : " + bundle.getSymbolicName() + " with ID " + bundle.getBundleId());
					startBundle(bundle);
				}
			}

		}

	}

	private void installBundles(Iterator<String> bundleResources, List installedBndList) {
		for (Iterator bIterator = bundleResources; bIterator.hasNext();) {
			Bundle bundledInstalled = null;
			String bundleEntry = (String) bIterator.next();
			if (isBundle(bundleEntry)) {
				URL bundleURL = resourceProvider.getResource(bundleEntry);
				LMSLogManager.LOGGER.log(Level.INFO, " URL : " + bundleURL.toString());
				try {
					bundledInstalled = installBundle(bundleURL.openConnection().getInputStream(),
							framework.getBundleContext(), bundleEntry);
					installedBndList.add(bundledInstalled);
				} catch (IOException e) {
					LoggerDelegate.error(e.getMessage(), e);
				}
			}

		}

	}

	private boolean isFragment(Bundle bundledInstalled) {
		Dictionary<?, ?> headerMap = bundledInstalled.getHeaders();
		return headerMap.get(Constants.FRAGMENT_HOST) != null;
	}

	public static boolean isBundle(final String path) {
		for (String extension : new String[] { ".jar", ".war" }) {
			if (path.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	public boolean launch() {
		Properties defaultProperties = loadFrameworkProperties();
		defaultProperties.putAll(constructProperties);
		setLogger(defaultProperties);
		this.constructProperties = defaultProperties;
		String jVersion = System.getProperty("java.version");
		if (jVersion.startsWith("1.7") || jVersion.startsWith("1.8"))
			factory = (FrameworkFactory) getFrameworkFactory();
		else {
			try {
				factory = (FrameworkFactory) getFrameworkFactoryNoServiceLoader();
			} catch (Exception e) {

			}
		}
		LMSLogManager.LOGGER.log(Level.INFO, "URL : " + defaultProperties.toString());
		if (factory != null)
			start(factory, defaultProperties);
		loadBundles();
		registerShutdwonHook(framework);
		return LAUNCH_RESULT = true;

	}

	private void registerShutdwonHook(Framework framework) {
		final Framework frmWrk = framework;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Trying to stop framework ....");
				try {
					frmWrk.stop();
				} catch (BundleException e) {
					System.out.println("Error while stopping framework");
					e.printStackTrace();
				}
			}
		});

	}

	private void setLogger(Properties defaultProperties) {
		LoggerDelegate felixLogger = null;
		String home = defaultProperties.getProperty("felix.cache.rootdir");
		LMSLogManager manager = new LMSLogManager(home);
		manager.setUpLogger();
		try {
			felixLogger = new LoggerDelegate(manager,home);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		defaultProperties.put("felix.log.logger", felixLogger);

	}

	


}
