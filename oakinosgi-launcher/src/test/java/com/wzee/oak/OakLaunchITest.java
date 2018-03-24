package com.wzee.oak;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
@Ignore
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OakLaunchITest
{
	public static final String OAK_VERSION = "1.8.2";
	public static final String JACKRABBIT_VERSION = "2.17.1";
	public static final String CONFIG_DIRECTORY = "config";
	public static final String OAK_HOME = "target/OakInOSGI-Home/oak-home";
	
	 @Configuration
	    public Option[] config() throws MalformedURLException {
	 
	        return options(
	       		bootDelegationPackage("sun.*"),
	       		frameworkProperty("org.osgi.service.http.port").value("6666"),
                frameworkProperty("repository.home").value(OAK_HOME),
	            mavenBundle("org.apache.felix", "org.apache.felix.http.servlet-api", "1.1.2"),
	            mavenBundle("org.apache.felix","org.apache.felix.http.jetty","3.4.8"),
	            mavenBundle("org.apache.felix", "org.apache.felix.scr", "2.0.12"),
                mavenBundle("org.osgi", "org.osgi.dto", "1.0.0"),
                mavenBundle( "org.apache.felix", "org.apache.felix.configadmin", "1.8.16" ),
                mavenBundle( "org.apache.felix", "org.apache.felix.fileinstall", "3.2.6" ),
                mavenBundle( "org.ops4j.pax.logging", "pax-logging-api", "1.7.2" ),

                mavenBundle( "javax.jcr", "jcr","2.0"),
                mavenBundle( "com.google.guava", "guava","15.0"),
                mavenBundle( "commons-codec", "commons-codec","1.10"),
                mavenBundle( "commons-io", "commons-io","2.6"),
                mavenBundle( "org.apache.jackrabbit", "jackrabbit-api",JACKRABBIT_VERSION),
                mavenBundle( "org.apache.jackrabbit", "jackrabbit-jcr-commons",JACKRABBIT_VERSION),
                mavenBundle( "org.apache.jackrabbit", "jackrabbit-data",JACKRABBIT_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-api",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-commons",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-core",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-store-composite",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-store-document",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-segment-tar",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-jcr",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-lucene",OAK_VERSION),
                mavenBundle( "org.apache.tika", "tika-core","1.17"),
                mavenBundle( "org.apache.jackrabbit", "oak-blob",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-core-spi",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-store-spi",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-query-spi",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-security-spi",OAK_VERSION),
                mavenBundle( "org.apache.jackrabbit", "oak-blob-plugins",OAK_VERSION),
                mavenBundle( "io.dropwizard.metrics", "metrics-core","3.1.0"),
                systemProperties(new SystemPropertyOption("felix.fileinstall.dir").value(getConfigDir())),
	            junitBundles()
	            );
	    }
	 
	 private String getConfigDir(){
	        return new File(new File(new File("src", "test"), "resources"), "config").getAbsolutePath();
	    }
	 

    @Inject
    BundleContext bundleContext = null;

    /**
     * You will get a list of bundles installed by default
     * plus your testcase, wrapped into a bundle called pax-exam-probe
     */
    @Test
    public void listBundles()
    {
        for( Bundle b : bundleContext.getBundles() )
        {
            System.out.println( "Bundle " + b.getBundleId() + " : " + b.getSymbolicName() );
        }

    }
    
    
    @Inject
    private BundleContext context;

    @Test
    public void bundleStates() {
        for (Bundle bundle : context.getBundles()) {
            assertEquals(
                String.format("Bundle %s not active. have a look at the logs", bundle.toString()), 
                Bundle.ACTIVE, bundle.getState());
        }
    }
    
    @Test
    public void listServices() throws InvalidSyntaxException {
        for (ServiceReference reference
                : context.getAllServiceReferences(null, null)) {
            System.out.println(reference);
        }
    }

    @Inject
    private NodeStore store;

    @Test
    public void testNodeStore() {
        System.out.println(store);
        System.out.println(store.getRoot());
    }

    @Inject
    private Repository repository;

    @Test
    public void testRepository() throws RepositoryException {
        System.out.println(repository);
        System.out.println(repository.getDescriptor(Repository.REP_NAME_DESC));
    }
    
    void delete(File f) throws IOException {
    	  if (f.isDirectory()) {
    	    for (File c : f.listFiles())
    	      delete(c);
    	  }
    	  if (!f.delete())
    	    throw new FileNotFoundException("Failed to delete file: " + f);
    	}
}
