package chapter1.unit6;

import static org.assertj.core.api.Assertions.*;

import chapter1.unit5.DaoFactory;
import chapter1.unit5.UserDao;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextSingletonTest {

    @Test
    @DisplayName("애플리케이션 컨텍스트를 통해 조회한 빈은 싱글톤이다.")
    void isSingleton() {
        DaoFactory daoFactory = new DaoFactory();
        UserDao factoryDao1 = daoFactory.userDao();
        UserDao factoryDao2 = daoFactory.userDao();
        assertThat(factoryDao1).isNotSameAs(factoryDao2);

        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao contextDao1 = ac.getBean("userDao", UserDao.class);
        UserDao contextDao2 = ac.getBean("userDao", UserDao.class);

        assertThat(contextDao1).isSameAs(contextDao2);
    }
}
