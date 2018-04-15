package aos.project.common;

import java.io.Serializable;
import java.util.List;

public class ChunkMeta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<Integer> sIdList;
	public int chId;
	public String chunkName;
	public int size;

	public ChunkMeta(String fileName, int chId,List<Integer> sIdList, int size) {
		this.chId = chId;
		this.sIdList= sIdList;
		this.size = size;
		this.chunkName = fileName + "_" + chId;
	}
}
