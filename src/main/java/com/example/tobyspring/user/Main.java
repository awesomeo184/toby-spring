package com.example.tobyspring.user;

import com.example.tobyspring.user.dao.ConnectionMaker;
import com.example.tobyspring.user.dao.NConnectionMaker;
import com.example.tobyspring.user.dao.UserDao;
import com.example.tobyspring.user.domain.User;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ConnectionMaker nConnectionMaker = new NConnectionMaker();

        UserDao dao = new UserDao(nConnectionMaker);

        User user = new User();
        user.setId("spring");
        user.setName("정수현");
        user.setPassword("hello");

        dao.add(user);

        System.out.println(user.getId() + " 등록 성공");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " 조회 성공");
    }
}
