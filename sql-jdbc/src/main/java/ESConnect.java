import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ESConnect {
    static final String URL = "jdbc:elasticsearch://http://localhost:9200";

    public static void main(String[] args) throws Exception {
        System.out.println("Connection");
        Connection con = DriverManager.getConnection(URL, null, null);
        System.out.println("Creating statement");
        Statement st = con.createStatement();
        String sql = "SELECT SUBSTRING('hello', 2)";
        System.out.println("Executing query");
        ResultSet rs = st.executeQuery(sql);

        System.out.println("Reading results.");
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            System.out.println(metaData.getColumnTypeName(i));
        }
        System.out.println();

        while (rs.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.print("Column: " + rs.getObject(i));
            }
            System.out.println();
        }
        rs.close();
        st.close();
        con.close();
    }
}