package com.elastic.configuration;

import com.elastic.plugins.postgres.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-03-18 오전 10:55
 **/
@Slf4j
//@SpringBootTest
public class PostgresqlDBTest {
    private String dburl = "jdbc:postgresql://192.168.10.7,192.168.10.8:5432/postgres";
    private String dbuser = "postgres";
    private String dbpassword = "postgres";

    private List<User> userList;

    private User user = new User();

    Connection connection;

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(dburl,dbuser,dbpassword);
        return c;
    }

    @Test
    public void dbBulkAddTest() throws SQLException, ClassNotFoundException, InterruptedException {


        for (int i=5;i<30;i++){
            user = new User();
            user.setPid((long) i);
            user.setName("둘리"+i);
            user.setPassword("둘리"+i);
            add(user);
            log.info(user.toString()+"등록 성공");
            Thread.sleep(5000);
        }
    }

    public void add(User user) throws SQLException, ClassNotFoundException {
        Connection c = getConnection();

        PreparedStatement ps = c.prepareStatement("insert into users(pid,name,password) values(?,?,?)");
        ps.setLong(1, user.getPid());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }
}
