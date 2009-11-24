package org.biomart.test.cancelQueries.arneVersion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ArneOriginal {
	public static void main(String[] args) {
		try {
			Connection c = DriverManager.getConnection("jdbc:postgresql://bm-test.res.oicr.on.ca:5432/ac_cancel", "martadmin", "biomart");
			run(c, "kill_test6");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void run(Connection c, String tableName) throws SQLException {
		
        PreparedStatement st = c.prepareStatement( "create table " + tableName + "( num int)" );
        st.execute();
        st = c.prepareStatement( "insert into " + tableName + "( num ) values( ? )" );
        int i;
        for( i=1; i<10000; i++ ) {
          st.setInt( 1, i );
          st.execute();
        }
        c.commit();

        System.out.println("sel");
        st = c.prepareStatement( "select x1.num, x2.num, x3.num, x4.num from " +
                " " + tableName + " x1, " + tableName + " x2, " + tableName + " x3, " + tableName + " x4",
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        
        ResultSet rs = st.executeQuery();
        System.out.println("getting results");
        i = 5000;
        int sum = 0;
        while( rs.next() && (i>0)) {
            i--;
            sum += rs.getInt(2);
        }
        st.cancel();
        st.close();
        rs.close();
        st = c.prepareStatement( "drop table " + tableName + "" );
        st.execute();
        c.commit();
	}
}
