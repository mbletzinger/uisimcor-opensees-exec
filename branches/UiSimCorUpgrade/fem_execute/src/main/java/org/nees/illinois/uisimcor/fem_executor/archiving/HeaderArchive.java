package org.nees.illinois.uisimcor.fem_executor.archiving;

import java.io.File;

import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write header files for the data archives.
 * @author Michael Bletzinger
 */
public class HeaderArchive {
	/**
	 * Text archiver.
	 */
	private final TextArchive archive;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(HeaderArchive.class);
	/**
	 * True if the columns are forces.
	 */
	private final boolean reactionHeader;
	/**
	 * Substructure configuration.
	 */
	private final SubstructureDao substructCfg;

	/**
	 * @param path
	 *            Path to data archive.
	 * @param substructCfg
	 *            Configuration of the substructure.
	 * @param reactionHeader
	 *            True if the data is reaction forces.
	 */
	public HeaderArchive(final String path, final SubstructureDao substructCfg,
			final boolean reactionHeader) {
		File pathF = new File(path + "_hdr.txt");
		this.archive = new TextArchive(pathF);
		this.substructCfg = substructCfg;
		this.reactionHeader = reactionHeader;
	}

	/**
	 * Write the header.
	 */
	public final void write() {
		String header = "Step";
		String units = "Number";
		for (Integer n : substructCfg.getNodeSequence()) {
			for (DispDof d : substructCfg.getEffectiveDofs(n)) {
				String dof = d.toString();
				if (reactionHeader) {
					dof = dof.replace("D", "F");
					dof = dof.replace("R", "M");
				}
				header += "\t" + n.toString() + "-" + dof;
				units += "\t" + d.units(reactionHeader);
			}
		}
		header += "\n";
		units += "\n";
		archive.write(header);
		archive.write(units);
	}
}
