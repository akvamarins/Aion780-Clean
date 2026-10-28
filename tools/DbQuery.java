import java.sql.*;

public class DbQuery {
    public static void main(String[] a) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/al_server_ls?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
        Statement s = c.createStatement();
        System.out.println("=== account_data ===");
        ResultSet rs = s.executeQuery("SELECT id, name, activated, access_level, membership, toll, last_server, last_ip FROM account_data");
        while (rs.next()) System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | act=" + rs.getInt("activated") + " | acc=" + rs.getInt("access_level") + " | memb=" + rs.getString("membership") + " | toll=" + rs.getInt("toll") + " | last_server=" + rs.getInt("last_server") + " | ip=" + rs.getString("last_ip"));
        System.out.println("=== gameservers ===");
        rs = s.executeQuery("SELECT * FROM gameservers");
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) System.out.print(md.getColumnName(i) + " ");
        System.out.println();
        while (rs.next()) { for (int i = 1; i <= md.getColumnCount(); i++) System.out.print(rs.getString(i) + " | "); System.out.println(); }
        rs.close(); s.close(); c.close();
    }
}
