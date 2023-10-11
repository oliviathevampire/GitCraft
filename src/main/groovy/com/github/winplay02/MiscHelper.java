package com.github.winplay02;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Stream;

public class MiscHelper {
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void println(String formatString, Object... args) {
		System.out.println(String.format(formatString, args));
	}

	public static void panic(String formatString, Object... args) {
		throw new RuntimeException(String.format(formatString, args));
	}

	public static void copyLargeDir(Path source, Path target) {
		try (Stream<Path> walk = Files.walk(source)) {
			for (Path path : (Iterable<? extends Path>) walk::iterator) {
				Path resultPath = target.resolve(source.relativize(path).toString());
				if (Files.isDirectory(path) && Files.notExists(resultPath)) {
					Files.createDirectories(resultPath);
				} else if (Files.isRegularFile(path)) {
					Files.copy(path, resultPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void deleteDirectory(Path directory) throws IOException {
		try (Stream<Path> walk = Files.walk(directory)) {
			walk.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(java.io.File::delete);
		}
	}

	public interface ExceptionInsensitiveRunnable {
		void run() throws Exception;
	}

	public static void executeTimedStep(String message, ExceptionInsensitiveRunnable runnable) {
		println(message);
		long timeStart = System.nanoTime();
		try {
			runnable.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		long timeEnd = System.nanoTime();
		long delta = timeEnd - timeStart;
		Duration deltaDuration = Duration.ofNanos(delta);
		println("\tFinished: %dm %02ds", deltaDuration.toMinutes(), deltaDuration.toSecondsPart());
	}

	public static void checkFabricLoaderVersion() {
		try {
			SemanticVersion actualFabricLoaderVersion = SemanticVersion.parse(FabricLoaderImpl.VERSION);
			SemanticVersion minRequiredVersion = SemanticVersion.parse(GitCraftConfig.MIN_SUPPORTED_FABRIC_LOADER);
			if (actualFabricLoaderVersion.compareTo((Version) minRequiredVersion) < 0) {
				panic("Fabric loader is out of date. Min required version: %s, Actual provided version: %s", GitCraftConfig.MIN_SUPPORTED_FABRIC_LOADER, FabricLoaderImpl.VERSION);
			}
		} catch (VersionParsingException e) {
			throw new RuntimeException(e);
		}
	}
}
