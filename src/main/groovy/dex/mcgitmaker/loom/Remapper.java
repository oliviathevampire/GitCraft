package dex.mcgitmaker.loom;

import com.github.winplay02.GitCraftConfig;
import com.github.winplay02.MappingHelper;
import com.github.winplay02.MiscHelper;
import dex.mcgitmaker.GitCraft;
import dex.mcgitmaker.data.McVersion;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.util.RecordComponentFixVisitor;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class Remapper {
	// From Fabric-loom
	private static final Pattern MC_LV_PATTERN = Pattern.compile("\\$\\$\\d+");

	public static Path remappedPath(McVersion mcVersion, MappingHelper.MappingFlavour mappingFlavour) {
		return GitCraft.REMAPPED.resolve(String.format("%s-%s.jar", mcVersion.version, mappingFlavour.toString()));
	}

	public static Path doRemap(McVersion mcVersion, MappingHelper.MappingFlavour mappingFlavour) throws IOException {
		Path output = remappedPath(mcVersion, mappingFlavour);

		// Based on what Fabric-loom does
		if (!output.toFile().exists() || output.toFile().length() == 22 /* empty jar */) {
			if (output.toFile().exists()) {
				output.toFile().delete();
			}

			TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
					.renameInvalidLocals(true)
					.rebuildSourceFilenames(true)
					.invalidLvNamePattern(MC_LV_PATTERN)
					.inferNameFromSameLvIndex(true)
					.withMappings(mappingFlavour.getMappingsProvider(mcVersion))
					.fixPackageAccess(true)
					.threads(GitCraft.config.remappingThreads);

			TinyRemapper remapper = remapperBuilder.build();
			remapper.readInputs(mcVersion.merged().toPath());

			try (OutputConsumerPath consumer = new OutputConsumerPath.Builder(output).build()) {
				remapper.apply(consumer, remapper.createInputTag());
			}
			remapper.finish();
		}

		return output;
	}
}
