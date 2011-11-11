SampleBoard のインストール方法

===============================================================================

実行するには、以下のものが必要です。
１．Java2 SDK 1.4（1.3 では動きません）
２．サーブレットエンジン
　　　Tomcat 4.0, Resin 2.1 で動作を確認しています。

インストールするには、SampleBoard.war を適切なフォルダにコピーし、
サーブレットエンジンを再起動してください。

Tomcat 4.0 も Resin 2.1 も インストール先の webapps フォルダにコピーすればOKです。

http://localhost:8080/SampleBoard/SampleBoard.html
を開けば、実行できます。

Apache と連携している場合は、/SampleBoard 以下を
サーブレットにまわす設定もしてください。

  Resin 2.1 の場合は、
    <Location /SampleBoard/*>
      SetHandler caucho-request
    </Location>
  を Apache の conf/httpd.conf に追加してください。

　Tomcat 4.0 の方法は未確認です。

================================================================================

コンパイル済みのが付属していますが、ソースからコンパイルするには、
　ant ( http://jakarta.apache.org/ant/ )
がさらに必要です。ant はコンパイル時に利用する便利なツールです。

ant は、http://www.ingrid.org/jajakarta/ant/ にて日本語訳が読めます。
また、http://jakarta.apache.org/builds/jakarta-ant/release/ から
最新版がダウンロードできます。

クライアント側をコンパイルするには、
この readme.txt があるフォルダで、コマンドラインから ant を実行してください。
build.xml が実行され、SampleBoard.html が作られます。

サーバー側をコンパイルして、SampleBoard.war を作成するには、
servlet フォルダに移動して、ant を実行してください。

===============================================================================

謝辞：
http://roko.lolipop.jp/　　フリー素材　Ｒｏｋｏ　さん
http://www.tiara.cc/~1999love/r_s/　　Rocket & Submarine　さん
から素材を利用させていただきました。

ありがとうございます。
