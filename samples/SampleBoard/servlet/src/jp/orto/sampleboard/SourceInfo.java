package jp.orto.sampleboard;

import java.io.*;

/**
 * プログラム内の１ソースの情報
 */
public class SourceInfo implements Serializable {
	private String sourceName;
	private String sourceUrl;

	public String getSourceName() {
		return sourceName;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public SourceInfo(String sourceName, String sourceUrl) {
		this.sourceName = sourceName;
		this.sourceUrl = sourceUrl;
	}
}
