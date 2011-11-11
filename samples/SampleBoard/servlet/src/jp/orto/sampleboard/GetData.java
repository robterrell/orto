package jp.orto.sampleboard;

import orto.servlet.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

public class GetData extends HttpServlet {
	private static SampleInfo[] vec;

	final static String[] mtSrc = {
		"client/jp/orto/monkeytiger/MonkeyTiger.java"};
	final static String[] pbSrc = {
		"client/jp/orto/sampleboard/SampleBoard.java",
		"client/jp/orto/sampleboard/SampleInfo.java",
		"client/jp/orto/sampleboard/SourceInfo.java",
		"servlet/jp/orto/sampleboard/GetData.java",
		"servlet/jp/orto/sampleboard/GetSource.java",
		"servlet/jp/orto/sampleboard/SampleInfo.java",
		"servlet/jp/orto/sampleboard/SourceInfo.java"};

	static {
		vec = new SampleInfo[2];

		// サルトラ算のデータベース作成
		SourceInfo[] sourceVec = { new SourceInfo(mtSrc[0], mtSrc[0]) };
		vec[0] = new SampleInfo(
				"monkey.gif", "サルトラ算", "2002/10/14", "2002/10/14",
				"かわいいサルとトラがあなたのために計算をしてくれます。",
				"http://orto.jp/samples/MonkeyTiger/MonkeyTiger.html",
				sourceVec);

		// プログラム掲示板のデータベース作成
		sourceVec = new SourceInfo[pbSrc.length];
		for (int i = 0; i < pbSrc.length; i++)
			sourceVec[i] = new SourceInfo(pbSrc[i], pbSrc[i]);
		vec[1] = new SampleInfo(
				"snowflake-bbs-b.gif", "プログラム掲示板", "2002/11/11", "2002/11/11",
				"これのことです。", "http://orto.jp/SampleBoard/SampleBoard.html",
				sourceVec);
	}

	/**
	 * プログラム一覧を出力します。
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HTMLOutputStream out = new HTMLOutputStream(response);
		out.writeObject(vec);
		out.send();
	}
}
