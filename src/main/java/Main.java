import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

public class Main {

    //private static final String URL = "jdbc:phoenix:a1.fyre.ibm.com,aa1.fyre.ibm.com,hurds1.fyre.ibm.com:2181:/hbase-secure";
    //private static final String KERBEROS = ":sb@FYRE.NET:/home/sbartkowski/bin/keytabs/sbfyre.keytab";

//    private static final String URL = "jdbc:phoenix:a1.fyre.ibm.com,aa1.fyre.ibm.com,hurds1.fyre.ibm.com:2181:/hbase-secure";
//    private static final String KERBEROS = ":sb@FYRE.NET:/home/sbartkowski/bin/keytabs/sbfyre.keytab";

    private static final String LISTTABLES = "select DISTINCT(\"TABLE_NAME\") from SYSTEM.CATALOG";
    private static final String URL = "url";
    private static final String KERBEROS = "kerberos";

    private static void P(String s) {
        System.out.println(s);
    }

    private static void help() {

        P("Single parameter expected.");
        P("java .... /path to property file/");
        System.exit(4);
    }

    private static String prepareURL(String pname) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(pname));
        String url = p.getProperty(URL) + ((p.getProperty(KERBEROS) != null) ? ":" + p.getProperty(KERBEROS) : "");
        P("Connecting to " + url);
        return url;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

        if (args.length != 1) help();
        Class<?> aClass = Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        P("Property file: " + args[0]);

        try (Connection con = DriverManager.getConnection(prepareURL(args[0]))) {

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
        }
    }
}