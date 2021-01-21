package com.example.tobyspring.user;

import com.example.tobyspring.user.dao.ConnectionMaker;
import com.example.tobyspring.user.dao.CountingConnectionMaker;
import com.example.tobyspring.user.dao.DConnectionMaker;
import com.example.tobyspring.user.dao.NConnectionMaker;
import com.example.tobyspring.user.dao.UserDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoFactory {

    @Bean
    public UserDao userDao() {
        return new UserDao(connectionMaker());
    }

    @Bean
    public ConnectionMaker connectionMaker() {
        return new CountingConnectionMaker(realConnectionMaker());
    }

    @Bean
    public ConnectionMaker realConnectionMaker() {
        return new NConnectionMaker();
    }

}
