import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Main {


    private static final String LISTTABLES = "select DISTINCT(\"TABLE_NAME\") from SYSTEM.CATALOG";
    private static final String URL = "url";
    private static final String QUERY = "query";
    private static final String UPDATE = "update";
    private static final String KERBEROS = "kerberos";

    private static void P(String s) {
        System.out.println(s);
    }

    private static void help() {

        P("Single parameter expected.");
        P("java .... /path to property file/");
        System.exit(4);
    }

    private static String prepareURL(Properties p) throws IOException {
        String url = p.getProperty(URL) + ((p.getProperty(KERBEROS) != null) ? ":" + p.getProperty(KERBEROS) : "");
        P("Connecting to " + url);
        return url;
    }

    private static void runUpdate(Connection con, String update) throws SQLException {
        try ( Statement stmt = con.createStatement()) {
            P("Execute: " + update);
            stmt.executeUpdate(update);
            con.commit();
        }
    }

    private static void runQuery(Connection con, String query) throws SQLException {
        try (PreparedStatement statement = con.prepareStatement(query)) {
            ResultSet rset = statement.executeQuery();
            ResultSetMetaData m = rset.getMetaData();
            while (rset.next()) {
                for (int i = 1; i<= m.getColumnCount(); i++) {
                    String s = rset.getString(i);
                    System.out.print(m.getColumnName(i) + ":" + s + " ");
                }
                System.out.println("");
            }
        }

    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

        if (args.length != 1) help();
        Class<?> aClass = Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        P("Property file: " + args[0]);
        Properties prop = new Properties();
        prop.load(new FileInputStream(args[0]));


        try (Connection con = DriverManager.getConnection(prepareURL(prop))) {

//        stmt.executeUpdate("create table test (mykey integer not null primary key, mycolumn varchar)");
//        stmt.executeUpdate("upsert into test values (1,'Hello')");
//        stmt.executeUpdate("upsert into test values (2,'World!')");
//        con.commit();

            try (PreparedStatement statement = con.prepareStatement(LISTTABLES)) {
                ResultSet rset = statement.executeQuery();
                while (rset.next()) {
                    P(rset.getString("TABLE_NAME"));
                }
            }

            if (prop.getProperty(UPDATE) != null)
                runUpdate(con,prop.getProperty(UPDATE));
            if (prop.getProperty(QUERY) != null)
                runQuery(con,prop.getProperty(QUERY));
        }
    }
}