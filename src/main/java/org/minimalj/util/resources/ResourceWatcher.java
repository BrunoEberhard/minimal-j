package org.minimalj.util.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Watches classpath directory entries for changes to {@code .properties}
 * files and invokes a callback so caches of localized resources can be
 * invalidated. Used in development to see changes without an application
 * restart.
 */
class ResourceWatcher {
	private static final Logger logger = Logger.getLogger(ResourceWatcher.class.getName());

	private static boolean started = false;

	static synchronized void start(Runnable onPropertiesChanged) {
		if (started) {
			return;
		}
		started = true;

		WatchService watchService;
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not create WatchService - resource reload disabled", e);
			return;
		}

		int registered = 0;
		for (String entry : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
			File file = new File(entry);
			if (file.isDirectory()) {
				try {
					registerRecursive(file.toPath(), watchService);
					registered++;
				} catch (IOException e) {
					logger.log(Level.WARNING, "Could not watch " + file, e);
				}
			}
		}
		if (registered == 0) {
			logger.fine("No classpath directories to watch for resource reload");
			return;
		}

		Thread thread = new Thread(() -> watchLoop(watchService, onPropertiesChanged), "minimal-j Resource Watcher");
		thread.setDaemon(true);
		thread.start();
		logger.fine("Resource watcher started on " + registered + " classpath director" + (registered == 1 ? "y" : "ies"));
	}

	private static void registerRecursive(Path root, WatchService watchService) throws IOException {
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void watchLoop(WatchService watchService, Runnable onPropertiesChanged) {
		while (true) {
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			Path watchedDir = (Path) key.watchable();
			boolean propertiesChanged = false;
			for (WatchEvent<?> event : key.pollEvents()) {
				if (!(event.context() instanceof Path)) {
					continue;
				}
				Path relative = (Path) event.context();
				Path resolved = watchedDir.resolve(relative);
				if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(resolved)) {
					try {
						registerRecursive(resolved, watchService);
					} catch (IOException e) {
						logger.log(Level.FINE, "Could not register new directory " + resolved, e);
					}
				}
				if (relative.toString().endsWith(".properties")) {
					propertiesChanged = true;
				}
			}
			if (propertiesChanged) {
				try {
					onPropertiesChanged.run();
				} catch (RuntimeException e) {
					logger.log(Level.WARNING, "Resource invalidation failed", e);
				}
			}
			key.reset();
		}
	}
}
