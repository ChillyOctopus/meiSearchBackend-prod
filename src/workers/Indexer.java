package workers;

import serverCode.HttpServerProxy;
import serverCode.Requests.ReqAddMusic;
import serverCode.Responses.ResAddMusic;
import serverCode.Services.BASE_SERVICE;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;

/**
 * Indexes MEI files stored in a PostgreSQL database by sending them to an HTTP proxy server.
 * <p>
 * Expected arguments:
 * 1. Proxy server host
 * 2. Proxy server port
 * </p>
 * Example usage:
 * {@code java Indexer "localhost" "8080"}
 * This class is used for the script 'indexDatabase.sh'
 */
public class Indexer {

    private static final String CHAR_SET = "UTF-8";
    private static final String JDBC_URL = System.getenv("DB_HOST")+System.getenv("DB_PORT");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASS");
    private static final String POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND = "PostgreSQL JDBC Driver not found.";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.printf(
                    "Found %d args. Correct usage is: Indexer 'host' 'port'%n",
                    args.length
            );
            return;
        }

        String host = args[0];
        String port = args[1];

        if (!loadPostgresDriver()) return;

        HttpServerProxy proxy = new HttpServerProxy(host, port);
        processAndIndexFiles(proxy);
    }

    static boolean loadPostgresDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println(POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND);
            e.printStackTrace();
            return false;
        }
    }

    private static void processAndIndexFiles(HttpServerProxy proxy) {
        String sql = "SELECT file_id, file_name, file_content FROM public.\"meiFiles\"";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String fileId = rs.getString("file_id");
                String fileName = rs.getString("file_name");
                String fileContent = rs.getString("file_content");

                System.out.println("Processing file: " + fileName);

                ReqAddMusic request = new ReqAddMusic(fileName, fileContent, fileId);
                ResAddMusic response = proxy.addMusic(request);

                if (response.isSuccess()) {
                    System.out.println("Indexed");
                } else {
                    System.out.println("Failed");
                    System.out.println("Reason / Exception: " + response.message);
                    System.out.println("Elastic: " + response.getElasticResponse());
                }
            }
        } catch (Exception e) {
            System.out.println("General error in 'processAndIndexFiles: "+e.getMessage());
        }
    }

    public static void downloadAllFilesToLocalHost() {
        String sql = "SELECT file_id, file_name, file_content FROM public.\"meiFiles\"";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String fileName = rs.getString("file_name");
                String fileContent = rs.getString("file_content");

                System.out.println("Processing file: " + fileName);
                File saveMe = new File("/home/jisteven/jacob/job/specialCollections/meiFiles/"+fileName);
                BASE_SERVICE.writeString(fileContent, new FileOutputStream(saveMe));
            }
        } catch (Exception e) {
            System.out.println("General error in 'downloadAllFilesToLocalHost: "+e.getMessage());
        }

    }

    public static String getFileByName(String fileName) {
        String sql = "SELECT file_content FROM public.\"meiFiles\" WHERE file_name = '" + fileName + "'";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("file_content");
            }
        } catch (SQLException e) {
            System.out.println("SQL error in 'getFileByName': "+e.getMessage());
        }
        return null;
    }

}
