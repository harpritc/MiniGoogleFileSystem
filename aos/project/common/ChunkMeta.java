package aos.project.common;

import java.io.Serializable;

public class ChunkMeta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int sId;
	public int chId;
	public String chunkName;
	public int size;

	public ChunkMeta(String fileName, int chId, int sId, int size) {
		this.chId = chId;
		this.sId = sId;
		this.size = size;
		this.chunkName = fileName + "_" + chId;
	}
}
