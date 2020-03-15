package cobol.services.ordermanager;
import java.sql.*;

public class Fetcher {
    public void connect(){
        try{
            Connection con=DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/menu","root","cobol2020");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from stand");
            while(rs.next()){
                System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
