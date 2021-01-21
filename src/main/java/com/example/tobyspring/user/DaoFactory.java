package com.example.tobyspring.user;

import com.example.tobyspring.user.dao.ConnectionMaker;
import com.example.tobyspring.user.dao.NConnectionMaker;
import com.example.tobyspring.user.dao.UserDao;

public class DaoFactory {

    public UserDao userDao() {
        ConnectionMaker connectionMaker = new NConnectionMaker();
        return new UserDao(connectionMaker);
    }

}
