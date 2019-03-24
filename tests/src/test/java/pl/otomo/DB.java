package pl.otomo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.*;


public class DB {

    // Method provides a connection to the database
    public static Connection ConnectToDB() {
             Connection c = null;
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "123";
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection(dbUrl, username, password);
            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return c;
    }

    // Method provides creating table "results"
    public static void createTable() {
        Connection con = DB.ConnectToDB();
        Statement stmt;
        try {
            stmt = con.createStatement();
            String sql = "CREATE TABLE results" +
                    "(id INT PRIMARY KEY     NOT NULL," +
                    " url           varchar, " +
                    " price         int , " +
                    " phone         int , " +
                    " imageURL      varchar, " +
                    " images         bytea)";
            stmt.executeUpdate(sql);
            con.commit();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        try {
            con.close();
        }
        catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }

    // Method provides inserting data into table "results"
    public static void insertIntoDB(int id, String url, int price, int phone, String imageurl, byte[] image) {
        Statement stmt = null;
        Connection con = DB.ConnectToDB();
        try {
            stmt = con.createStatement();
            String sql = "INSERT INTO results (id, url, price, phone, imageurl, images)"
                    + "VALUES ('" +id+ "','" + url + "', '" + price + "', '" + phone + "', '" + imageurl + "', '" + image + "')";
            stmt.executeUpdate(sql);
            con.commit();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        try {
            con.close();
        }
        catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    // Method select data from DB and create pdf report
    public static void CreateReport() throws DocumentException {
        Statement stmt = null;
        Connection con = DB.ConnectToDB();
        int sid;
        String surl;
        int sprice;
        int sphone;
        String imageurl;

        // Create new document
        Document document = new Document(PageSize.A4);

        // Create new pdf file
        try {
            PdfWriter.getInstance(document, new FileOutputStream("D:\\Report.pdf"));
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();
        document.add(Chunk.NEWLINE);
        Font f = new Font(Font.FontFamily.HELVETICA,21.0f, Font.BOLD);
        Paragraph p = new Paragraph("REPORT", f);
        p.setAlignment(p.ALIGN_CENTER);
        document.add(p);
        document.add(Chunk.NEWLINE);

        for (int i = 1; i <= 10; i++) {
            Font font = new Font(Font.FontFamily.HELVETICA,14.0f, Font.BOLD);
            Paragraph title = new Paragraph("Search result #"+i+"", font);
            document.add(title);
            try {
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM results where id= "+i+"");

                while (rs.next()) {

                    //Retrieve data by column name
                    sid = rs.getInt("id");
                    surl = rs.getString("url");
                    sprice = rs.getInt("price");
                    sphone = rs.getInt("phone");
                    imageurl = rs.getString("imageurl");

                    // Get image from DB
                    Image image = Image.getInstance(imageurl);
                    image.scalePercent(30);

                    // Prepare paragraphs
                    Paragraph pid = new Paragraph("ID: " + sid);
                    Paragraph purl = new Paragraph("URL: " + surl);
                    Paragraph pprice = new Paragraph("PRICE: " + sprice);
                    Paragraph pphone = new Paragraph("PHONE: " + sphone);

                    // Add paragraphs to pdf document
                    document.add(pid);
                    document.add(purl);
                    document.add(pprice);
                    document.add(pphone);
                    document.add(image);

                    document.add(Chunk.NEWLINE);
                    document.add(Chunk.NEWLINE);
                }

                stmt.close();
                rs.close();
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
        try {
            con.close();
        }

        catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        document.close();
        System.out.println("Report created successfully");
    }

    // Method provides inserting data into table "results"
    public static void PrepareTable() {
        Statement stmt = null;
        Connection con = DB.ConnectToDB();
        try {
            DatabaseMetaData metadata = con.getMetaData();
            ResultSet resultSet;
            resultSet = metadata.getTables(null, null, "results", null);

            if (resultSet.next()) {
                con.setAutoCommit(false);
                System.out.println("Table exists, records removed");
                stmt = con.createStatement();
                String sql = "delete from results";
                stmt.executeUpdate(sql);
                con.commit();

            } else {
                DB.createTable();
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        try {
            con.close();
        }
        catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
}
