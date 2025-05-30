package workers;

import serverCode.HttpServerProxy;
import serverCode.Requests.ReqAddMusic;
import serverCode.Responses.ResAddMusic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Indexes MEI files stored in a PostgreSQL database by sending them to an HTTP proxy server.
 * <p>
 * Expected arguments:
 * 1. JDBC URL
 * 2. Database user
 * 3. Database password
 * 4. Proxy server host
 * 5. Proxy server port
 * </p>
 * Example usage:
 * {@code java Indexer "jdbc:postgresql://localhost:5432/mydb" "user" "pass" "localhost" "8080"}
 * This class is used for the script 'indexDatabase.sh'
 */
public class Indexer {

    private static final String CHAR_SET = "UTF-8";

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.printf(
                    "Found %d args. Correct usage is: Indexer 'jdbcUrl' 'dbUser' 'dbPassword' 'host' 'port'%n",
                    args.length
            );
            return;
        }

        String jdbcUrl = args[0];
        String dbUser = args[1];
        String dbPassword = args[2];
        String host = args[3];
        String port = args[4];

        if (!loadPostgresDriver()) return;

        HttpServerProxy proxy = new HttpServerProxy(host, port);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            processAndIndexFiles(connection, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean loadPostgresDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found.");
            e.printStackTrace();
            return false;
        }
    }

    private static void processAndIndexFiles(Connection connection, HttpServerProxy proxy) throws Exception {
        String sql = "SELECT file_id, file_name, file_content FROM public.\"meiFiles\"";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
        }
    }

    public static String getFileById(Connection connection, String fileId) throws Exception {
        String sql = "SELECT file_content FROM public.\"meiFiles\" WHERE file_id = '" + fileId + "'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("file_content");
            }
        }
        return null;
    }

    public static String getFileByName(Connection connection, String fileName) throws Exception {
        String sql = "SELECT file_content FROM public.\"meiFiles\" WHERE file_name = '" + fileName + "'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("file_content");
            }
        }
        return null;
    }

}
