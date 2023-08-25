package dex.mcgitmaker.data;

import com.github.winplay02.RemoteHelper;

import java.io.File;
import java.nio.file.Path;

/**
 * @param url            Download URL
 * @param name           File name
 * @param containingPath
 * @param sha1sum
 */
public record Artifact(String url, String name, Path containingPath, String sha1sum) {
	public Artifact(String url, String name, Path containingPath) {
		this(url, name, containingPath, null);
	}

	public static Artifact ofVirtual(String name) {
		return new Artifact("", name, null);
	}

	public File fetchArtifact() {
		Path path = containingPath().resolve(name);
		ensureArtifactPresence(path);
		return path.toFile();
	}

	void ensureArtifactPresence(Path p) {
		RemoteHelper.downloadToFileWithChecksumIfNotExists(url, p, sha1sum, "artifact", name);
	}

	static String nameFromUrl(String url) {
		if (url == null) {
			return "";
		}
		String[] urlParts = url.split("/");
		return urlParts[urlParts.length - 1];
	}
}
