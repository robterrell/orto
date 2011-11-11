package jp.orto.sampleboard;

import java.io.*;

/**
 * １プログラムの情報
 */
public class SampleInfo implements Serializable {
	// 以下５つは表示用
	private String imgIcon;
	private String programName;
	private String registerDay;
	private String updateDay;
	private String explain;
	// 「起動」のURL
	private String launchUrl;
	// ソース情報の一覧（SourceInfoのVector）
	private SourceInfo[] sourceVec;

	public String getLaunchUrl() {
		return launchUrl;
	}

	public SourceInfo[] getSourceVec() {
		return sourceVec;
	}

	public SampleInfo(String imgIcon, String programName, String registerDay, String updateDay,
					  String explain, String launchUrl, SourceInfo[] sourceVec) {
		this.imgIcon = imgIcon;
		this.programName = programName;
		this.registerDay = registerDay;
		this.updateDay = updateDay;
		this.explain = explain;
		this.launchUrl = launchUrl;
		this.sourceVec = sourceVec;
	}
}
