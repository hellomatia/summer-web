package server.core;

import server.handler.RequestHandler;
import server.handler.annotation.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HandlerScanner {
    private static final Logger logger = LoggerFactory.getLogger(HandlerScanner.class);
    private final String basePackage;

    public HandlerScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<RequestHandler> scanForHandlers() {
        List<RequestHandler> handlers = new ArrayList<>();
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    scanFileSystem(new File(resource.getFile()), basePackage, handlers);
                } else if (resource.getProtocol().equals("jar")) {
                    scanJar(resource, handlers);
                }
            }
        } catch (IOException e) {
            logger.error("Error scanning for handlers", e);
        }
        return handlers;
    }

    private void scanFileSystem(File directory, String packageName, List<RequestHandler> handlers) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    scanFileSystem(file, packageName + "." + file.getName(), handlers);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    processClass(className, handlers);
                }
            }
        }
    }

    private void scanJar(URL resource, List<RequestHandler> handlers) throws IOException {
        String jarPath = URLDecoder.decode(resource.getPath().substring(5, resource.getPath().indexOf("!")), "UTF-8");
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(basePackage.replace('.', '/')) && entryName.endsWith(".class")) {
                    String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    processClass(className, handlers);
                }
            }
        }
    }

    private void processClass(String className, List<RequestHandler> handlers) {
        try {
            Class<?> clazz = Class.forName(className);
            if (isHandlerClass(clazz)) {
                RequestHandler handler = (RequestHandler) clazz.getDeclaredConstructor().newInstance();
                handlers.add(handler);
            }
        } catch (ReflectiveOperationException e) {
            logger.error("Error processing class: " + className, e);
        }
    }

    private boolean isHandlerClass(Class<?> clazz) {
        return RequestHandler.class.isAssignableFrom(clazz)
                && !clazz.isInterface()
                && clazz.isAnnotationPresent(Handler.class);
    }
}
