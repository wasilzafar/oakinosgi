package com.wzee.oak;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class ResourceProvider {
	
	private final ClassLoader classLoader;

    public ResourceProvider(ClassLoader classLoader) {
        this.classLoader = (classLoader != null)
                ? classLoader
                : this.getClass().getClassLoader();
    }
    
    public InputStream getResourceAsStream(String path) {
        URL res = this.getResource(path);
        if (res != null) {
            try {
                return res.openStream();
            } catch (IOException ioe) {
                // ignore this one
            }
        }

        // no resource
        return null;

    }
	
    public URL getResource(String path) {
        // ensure path
        if (path == null || path.length() == 0) {
            return null;
        }

        // remove leading slash
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }

        return (this.classLoader != null) ? this.classLoader.getResource(path) : null;
    }

	public Iterator<String> getChildren(String path) {
		List<String> children;

		// Guard against extra trailing slashes
		if (path.endsWith("/") && path.length() > 1) {
			path = path.substring(0, path.length() - 1);
		}

		URL url = this.classLoader.getResource(path);
		if (url != null) {
			Pattern pathPattern = Pattern.compile("^" + path + "/[^/]+/?$");

			children = new ArrayList<String>();
			try {
				URLConnection conn = url.openConnection();
				if (conn instanceof JarURLConnection) {
					JarFile jar = ((JarURLConnection) conn).getJarFile();
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						String entry = entries.nextElement().getName();
						if (pathPattern.matcher(entry).matches()) {
							children.add(entry);
							System.out.println("Entry added : "+entry);
						}
					}
				}
			} catch (IOException ioe) {
				// ignore for now
			}
		} else {
			children = Collections.emptyList();
		}

		return children.iterator();
	}

}
