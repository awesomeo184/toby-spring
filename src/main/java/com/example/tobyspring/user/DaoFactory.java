package com.example.tobyspring.user;

import com.example.tobyspring.user.dao.AccountDao;
import com.example.tobyspring.user.dao.ConnectionMaker;
import com.example.tobyspring.user.dao.DConnectionMaker;
import com.example.tobyspring.user.dao.MessageDao;
import com.example.tobyspring.user.dao.NConnectionMaker;
import com.example.tobyspring.user.dao.UserDao;

public class DaoFactory {

    public UserDao userDao() {
        return new UserDao(connectionMaker());
    }

    public AccountDao accountDao() {
        return new AccountDao(connectionMaker());
    }

    public MessageDao messageDao() {
        return new MessageDao(connectionMaker());
    }

    public ConnectionMaker connectionMaker() {
        return new DConnectionMaker();
    }

}
