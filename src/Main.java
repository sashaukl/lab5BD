import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import java.sql.*;
import java.util.Random;

public class Main {
    private Statement st;
    private Statement st1;
    private String tableName;

    public String randomString(){
        String s = "";
        Random r  = new Random();
        for (int i=0; i< 4+r.nextInt(4) ;i++)
        {
            s += (char)(0x410+r.nextInt(32));
        }
        return s;
    }

    private void fillTable(){
        try {
            for (int i=0;i<10;i++)
            {
                st.executeUpdate("INSERT INTO " + tableName + " VALUES (" + i +", '" + randomString() + "', " + new Random().nextInt(10000) + ")" );
            }

        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void cursor(){
        try(Connection con= DriverManager.getConnection("jdbc:derby:database;create=true");
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            if(!con.getMetaData().getTables(null, "APP", "Goods".toUpperCase(), null).next()) {
                stmt.executeUpdate("CREATE TABLE Goods(id INT PRIMARY KEY, Name VARCHAR(50), Price INT)");
            }
            else{
                stmt.executeUpdate("DELETE FROM Goods");
            }
            tableName = "Goods";
            st = stmt;
            fillTable();
            ResultSet rs = st.executeQuery("SELECT * from " + tableName);
            while (rs.next()){
                System.out.println(rs.getInt("id") + "\t" + rs.getString("Name") + "\t" +  rs.getInt("Price"));
            }
            rs.absolute(-1);
            System.out.println("\nid after absolute is: " + rs.getInt("id"));

            rs.afterLast();
            System.out.println("exist row after afterLast is: " + rs.absolute(0) );

            rs.beforeFirst();
            System.out.println("exist row after beforeFirst is: " + rs.absolute(0) );

            rs.first();
            System.out.println("id after First is: " + rs.getInt("id"));

            rs.last();
            System.out.println("id after last is: " + rs.getInt("id"));

            rs.previous();
            System.out.println("id after First is: " + rs.getInt("id"));

            rs.relative(-2);
            System.out.println("id after relative is: " + rs.getInt("id"));

            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableName + " WHERE id= ?" );
            ps.setInt(1, 4 );
            rs = ps.executeQuery();

            while(rs.next()){
                System.out.println(rs.getInt("id") + "\t" +rs.getString("Name") + "\t" + rs.getInt("price") );
            }



            st.close();
        }
        catch(SQLException e) {e.printStackTrace();}
    }

    public void noCursor(){
        try{
            Class.forName("org.sqlite.JDBC");
        }
        catch (Exception e){
        }
        try( Connection con = DriverManager.getConnection("jdbc:sqlite:database.db");
             Statement stmt = con.createStatement()){
            if(!con.getMetaData().getTables(null, "APP", "Goods".toUpperCase(), null).next()) {
                stmt.executeUpdate("CREATE TABLE Goods(id INT PRIMARY KEY, Name VARCHAR(50), Price INT)");
            }
            else{
                stmt.executeUpdate("DELETE FROM Goods");
            }
            tableName = "Goods";
            st = stmt;
            fillTable();
            boolean result = st.execute("UPDATE Goods SET NAME = 'NAME' where id=2 ");
            st.execute("DELETE From Goods WHERE id=5");
            result = st.execute("SELECT * from Goods");
            if(result){
                ResultSet rs = st.getResultSet();
                System.out.println(rs.getFetchSize());
                while(rs.next()){
                    System.out.println(rs.getInt("id") + "\t" + rs.getString("Name"));
                }
            }
            //copy();
            st.execute("DELETE From Goods;");
            st.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private ResultSet rs1;
    public void copy(){
        try(Connection con1= DriverManager.getConnection("jdbc:derby:database;create=true");
            Statement stmt = con1.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Connection con2 = DriverManager.getConnection("jdbc:sqlite:database.db")
            ) {
            PreparedStatement insertStatement =
                    con2.prepareStatement("insert into Goods values(?, ?,?)");
            if (!con1.getMetaData().getTables(null, "APP", "Goods".toUpperCase(), null).next()) {
                stmt.executeUpdate("CREATE TABLE Goods(id INT PRIMARY KEY, Name VARCHAR(50), Price INT)");
            } else {
                stmt.executeUpdate("DELETE FROM Goods");
            }
            st = stmt;
            fillTable();
            ResultSet rs = st.executeQuery("SELECT  * FROM Goods");
            System.out.println("COPY\n\n");
            while (rs.next()){
                int id = rs.getInt("id");
                String name = rs.getString("Name");
                int price = rs.getInt("Price");
                System.out.println(id + " " + name + " " + price);
                insertStatement.clearParameters();
                insertStatement.setInt(1, id);
                insertStatement.setString(2, name);
                insertStatement.setInt(3, price);
                insertStatement.executeUpdate();
            }
            insertStatement.close();
            st.close();
            st = con2.createStatement();
            System.out.println("////////////////////");
            rs = st.executeQuery("SELECT * FROM Goods");
            while (rs.next()){
                System.out.println(rs.getInt("id") + " " + rs.getString("Name"));
            }
            st.close();

            }
            catch (SQLException e){
                System.out.println("fdfsd");
            }
    }

    public static void main(String[] args) {
        Main main = new Main ();
        main.cursor();
        main.noCursor();
        main.copy();
    }
}
