package jp.orto.sampleboard;

import orto.servlet.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

public class GetSource extends HttpServlet {
	/**
	 * ファイル名を受け取り、ソースを返します。
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// ファイル名を読み込む
		HTMLInputStream in = new HTMLInputStream(request);
		String fileName = in.readUTF();
		in.close();

		// ファイル名がデータベースに入っているかチェックする。
		boolean isRealFileName = false;
		if(GetData.mtSrc[0].equals(fileName))
			isRealFileName = true;
		for (int i = 0; i < GetData.pbSrc.length; i++) {
			if (GetData.pbSrc[i].equals(fileName))
				isRealFileName = true;
		}

		// ソースファイルを読み込む
		String source = "";
		if (isRealFileName) {
			BufferedReader reader = new BufferedReader
					(new InputStreamReader(getServletContext().getResourceAsStream(fileName),
							"SJIS"));
			String line;
			while ((line = reader.readLine()) != null)
				source += line + "\n";
		}

		// ソースを送信する。
		HTMLOutputStream out = new HTMLOutputStream(response);
		out.writeUTF(source);
		out.send();
	}
}
