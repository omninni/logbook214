/**
 * 
 */
package logbook.server.web;

import logbook.config.AppConfig;
import logbook.gui.ApplicationMain;
import logbook.internal.LoggerHolder;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Webサーバです
 *
 */
public class WebServer {

    private static final LoggerHolder LOG = new LoggerHolder(WebServer.class);

    private static Server server;

    private static String host;
    private static int port;

    public static void start(int port) {
        try {
            QueuedThreadPool threadpool = new QueuedThreadPool();
            threadpool.setMinThreads(2);

            server = new Server(threadpool);
            updateSetting();
            setConnector();

            ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(QueryHandler.class, "/master");
            servletHandler.addServletWithMapping(QueryHandler.class, "/query");
            servletHandler.addServletWithMapping(QueryHandler.class, "/battle");
            servletHandler.addServletWithMapping(QueryHandler.class, "/remodeldb");
            servletHandler.addServletWithMapping(QueryHandler.class, "/counter");

            server.setHandler(servletHandler);

            server.start();
        } catch (Exception e) {
            LOG.get().fatal("Webサーバーの起動に失敗しました", e);
            throw new RuntimeException(e);
        }
    }

    public static void restart() {
        try {
            if (server == null) {
                return;
            }
            if (updateSetting()) {
                server.stop();
                setConnector();
                server.start();
                ApplicationMain.logPrint("Webサーバーを再起動しました");
            }
        } catch (Exception e) {
            LOG.get().fatal("Webサーバーの起動に失敗しました", e);
            throw new RuntimeException(e);
        }
    }

    public static void end() {
        try {
            if (server != null) {
                server.stop();
                server.join();
                server = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AppConfigの設定をローカルにコピーします。その際、更新があったか判定します。
     * @return 更新があった
     */
    private static boolean updateSetting() {
        String newHost = null;
        if (AppConfig.get().isCloseWebOutsidePort()) {
            newHost = "localhost";
        }
        int newPort = AppConfig.get().getListenPort() + 1;

        if (StringUtils.equals(newHost, host) && (newPort == port)) {
            return false;
        }

        host = newHost;
        port = newPort;
        return true;
    }

    private static void setConnector() {
        ServerConnector connector = new ServerConnector(server, 1, 1);
        connector.setPort(port);
        connector.setHost(host);
        server.setConnectors(new Connector[] { connector });
    }

}
