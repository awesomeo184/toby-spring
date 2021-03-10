# Chapter 2 - 테스트

내가 읽고 있는 `토비의 스프링 3.1`의 2장 테스트는 JUnit 4를 기준으로 작성되었다. 아직 JUnit에 대해서 잘 알지는 못하지만
JUnit 4로 작성된 책의 예제를 JUnit 5로 바꿔서 작성해보려고 한다.

(앞부분의 테스트가 왜 중요하고 테스트 프레임워크가 왜 필요한지에 대한 내용은 생략했다.)

## 2.2 UserDaoTest 개선

1장에서 프레임워크의 기본 동작 원리가 IoC라고 설명했다. 프레임워크는 개발자가 만든 클래스에 대한 제어 권한을 넘겨받아서 주도적으로 애플리케이션의 흐름을 제어한다.
따라서 프레임워크에서 동작하는 코드는 main() 메서드도 필요 없고 오브젝트를 만들어서 실행시키는 코드를 만들 필요도 없다.

그런 의미에서 기존에 main() 메서드에서 만든 테스트는 프레임워크에 적용하기에 적합하지 않다. 테스트가 main() 메서드로 만들어졌다는 건
제어권을 직접 갖는다는 의미이기 때문이다. 그래서 가장 먼저 할 일은 main() 메서드에 있던 테스트 코드를 일반 메서드로 옮기는 것이다.

```java
class UserDaoTest {

    @Test
    @DisplayName("DB에 데이터 저장하고 가져오기가 잘 되는지")
    void addAndGet() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("gyumee");
        user.setName("박성철");
        user.setPassword("springno1");

        dao.add(user);

        User user2 = dao.get(user.getId());

        assertThat(user2.getName()).isEqualTo(user.getName());
        assertThat(user2.getPassword()).isEqualTo(user.getPassword());
    }
}
```

<br />

테스트는 잘 진행이 됐지만 아쉬운 점이 하나 있다. 테스트를 실행할 때마다 매번 DB 테이블을 삭제해줘야 한다는 것이다.
가장 좋은 해결책은 addAndGet() 테스트를 마치고 나면 테스트가 등록한 사용자 정보를 삭제해서, 테스트를 수행하기 이전 상태로 만들어주는 것이다.
이를 위해 UserDao에 일관성 있는 테스트를 위한 새로운 기능을 추가하자.

먼저 users 테이블의 모든 데이터를 삭제하는 deleteAll() 메서드를 추가한다.

```java
public class UserDao {
    ...

    public void deleteAll() throws SQLException {
        Connection con = dataSource.getConnection();

        PreparedStatement ps = con.prepareStatement("delete from users");
        ps.executeUpdate();

        ps.close();
        con.close();
    }
}
```

두 번째로 users 테이블의 레코드 개수를 반환하는 getCount() 메서드를 추가한다.

```java
public class UserDao {
    ...

    public int getCount() throws SQLException {
        Connection con = dataSource.getConnection();

        PreparedStatement ps = con.prepareStatement("select count(*) from users");

        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        rs.close();
        ps.close();
        con.close();

        return count;
    }
}
```

deleteAll 메서드는 독립적인 테스트를 만들기는 좀 애매한 부분이 있다. 굳이 테스트를 만들려면 테이블에 더미 데이터를 넣은 후 확인 해야하는데
그것보다는 기존의 테스트에 메서드를 추가해서 확장하는 방법이 더 나을듯 하다.

여기서 주의할 점은 deleteAll()이 잘 작동하는지 먼저 확인해야 한다는 것이다. 만약 deleteAll()이 잘 작동한다면 getCount() 메서드를 호출했을 때
0이 반환되어야 한다. 하지만 또 getCount() 메서드가 잘 작동하는지 알 수 없기 때문에(예를 들어 getCount 메서드가 항상 결과로 0을 반환하는 경우를 의심하지 않을 수 없다)
데이터를 하나 추가하고 getCount를 호출해 1을 반환하는지도 확인해야 한다.

```java
class UserDaoTest {

    @Test
    @DisplayName("DB에 데이터 저장하고 가져오기가 잘 되는지")
    void addAndGet() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);
        
        User user = new User();
        user.setId("gyumee");
        user.setName("박성철");
        user.setPassword("springno1");

        dao.add(user);

        User user2 = dao.get(user.getId());

        assertThat(dao.getCount()).isEqualTo(1);

        assertThat(user2.getName()).isEqualTo(user.getName());
        assertThat(user2.getPassword()).isEqualTo(user.getPassword());
        
    }
}
```

<br />

**포괄적인 테스트**

앞서 만든 getCount() 메서드의 테스트는 테이블이 비어있는 경우와 데이터가 하나 있는 경우 두 가지밖에 없다.
테스트를 안 만드는 것도 위험한 일이지만, 성의없이 테스트를 만들어 문제가 있는 코드가 테스트를 통과하게 만드는 것은 더 위험한 일이다.
getCount() 메서드에 대한 좀 더 꼼꼼한 테스트를 만들어보자.

<br />

간편한 테스트용 인스턴스를 만들기 위해 User 클래스에 생성자를 추가한다. 자바빈 규약을 따르는 클래스에 생성자를 명시적으로 추가했을 때는
디폴트 생성자도 같이 정의해줘야 한다.

```java
public class User {
    ...
    
    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public User() {}
    
    ...
```

그리고 getCount의 테스트 메서드를 만든다.

```java
class UserDaoTest {

    ...
    
    @Test
    @DisplayName("레코드 수를 잘 세는지")
    void count() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user1 = new User("awesome", "정수현", "hi");
        User user2 = new User("whiteship", "백기선", "developer");
        User user3 = new User("toby", "이일민", "springno1");

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        dao.add(user1);
        assertThat(dao.getCount()).isEqualTo(1);

        dao.add(user2);
        assertThat(dao.getCount()).isEqualTo(2);

        dao.add(user3);
        assertThat(dao.getCount()).isEqualTo(3);

    }
}
```

주의할 점은 JUnit은 테스트의 실행 순서를 보장하지 않는다. 따라서 테스트의 결과가 테스트 순서에 영향을 받도록 만들어서는 안된다.
모든 테스트는 실행 순서에 상관없이 독립적으로 항상 동일한 결과를 낼 수 있도록 해야한다.

<br />

마찬가지로 addAndGet 테스트도 보완할 필요성이 있다. 현재 테스트는 데이터를 하나만 추가하고 있기 때문에 get 메서드가 진짜로 테이블의 레코드를 가져오는 건지
아니면 임의의 값을 내뱉는 건지 알 수 없다. 좀 더 보완하도록 하자.

```java
class UserDaoTest {

    @Test
    @DisplayName("DB에 데이터 저장하고 가져오기가 잘 되는지")
    void addAndGet() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        User user1 = new User("awesome", "정수현", "HIhi");
        User user2 = new User("whiteship", "백기선", "spring");

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount()).isEqualTo(2);

        User getUser1 = dao.get(user1.getId());
        assertThat(getUser1.getName()).isEqualTo(user1.getName());
        assertThat(getUser1.getPassword()).isEqualTo(user1.getPassword());

        User getUser2 = dao.get(user2.getId());
        assertThat(getUser2.getName()).isEqualTo(user2.getName());
        assertThat(getUser2.getPassword()).isEqualTo(user2.getPassword());
    }
    
    ...
}
```

<br />

**예외처리**

한가지 더 생각해볼 문제가 있다. get 메서드에 전달되는 id가 테이블에 없다면 어떻게 해야될까? 두 가지 방법을 생각해볼 수 있다.
하나는 null을 반환하는 것이고 하나는 예외를 던지는 것이다. 각기 장단점이 있지만 여기서는 후자의 방법을 사용해보자

주어진 id에 해당하는 정보가 없다는 의미를 가진 예외가 필요하다. 여기서는 일단 EmptyResultDataAccessException 예외를 사용하겠다.

우선 테스트 코드를 먼저 만들어보자

```java
class UserDaoTest {
    ...
    
    @Test
    @DisplayName("전달된 id가 존재하지 않는 경우")
    void getUserFailure() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        UserDao dao = ac.getBean("userDao", UserDao.class);

        dao.deleteAll();
        assertThat(dao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> {
            dao.get("unknownId");
        }).isInstanceOf(EmptyResultDataAccessException.class);
    }
}
```

테스트를 돌려보면 당연히 실패한다. 이번에는 테스트가 성공하도록 UserDao를 수정해보자.

```java
public class UserDao {
    ...

    public User get(String id) throws SQLException {
        
        ...
        
        ResultSet rs = ps.executeQuery();

        User user = null;
        if(rs.next()) {
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }
        rs.close();
        ps.close();
        con.close();

        if (user == null) {
            throw new EmptyResultDataAccessException(1);
        }

        return user;
    }
}
```

**테스트 코드 리팩토링**

프로덕션 코드 뿐만 아니라 테스트 코드도 유지 보수가 필요하다. 우리가 만든 테스트 메서드들을 보면 계속 중복되는 코드가 있다.
바로 애플리케이션 컨텍스트를 생성하는 코드이다. 애플리케이션 컨텍스트는 모든 메서드가 사용하므로 따로 메서드로 빼주는 것이 좋다.

JUnit 5에서는 @BeforeAll, @BeforeEach, @AfterAll, @AfterEach 애노테이션을 통해서 테스트 실행 전후에 실행할 메서드를 지정할 수 있다.
이름 그대로 끝에 All이 붙으면 테스트 실행 전후에 해당 메서드가 한번만 실행되고, Each가 붙으면 각 메서드마다 한번씩 실행된다.

테스트를 수행하는데 필요한 정보나 오브젝트를 **픽스쳐(fixture)** 라고 하는데, 지금 우리 테스트에도 픽스쳐가 있다. 바로 User 인스턴스들이다.
이것도 setUp 메서드에 추가해주도록 하자.

```java
class UserDaoTest {

    private UserDao dao;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() throws SQLException {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        dao = ac.getBean("userDao", UserDao.class);

        user1 = new User("awesome", "정수현", "HIhi");
        user2 = new User("whiteship", "백기선", "spring");
        user3 = new User("toby", "이일민", "springno1");
    }
    
    ...
}
```

이렇게 @BeforeEach 애노테이션을 붙인 메서드를 생성해서 중복된 코드를 따로 빼준다. 이때 `dao`를 인스턴스 필드에
선언해주지 않으면 setUp() 메서드의 지역변수가 되기 때문에 다른 테스트 메서드에서 사용할 수 없으므로, 선언해주도록 한다.

이렇게 하면 클래스 내에 있는 각 테스트 메서드가 실행되기 전에 setUp() 메서드가 한번씩 실행된다. 그런데 여기서 잠깐.
UserDao의 변수는 한번 할당하고나면 모든 테스트 메서드가 공유할 수 있을텐데 굳이 각 테스트 메서드마다 setUp 메서드를 실행할 필요가 있을까?
잠시 JUnit의 테스트 인스턴스의 생명주기를 알아보도록 하자.

**JUnit 인스턴스의 생명주기**

JUnit은 각 테스트의 독립성을 보장하고, 테스트 사이의 상호작용으로 인한 부작용을 방지하기 위해 매 테스트 메서드를 실행할 때마다 새로운 인스턴스를 생성한다.
즉 @Test 애노테이션이 붙은 세 개의 메서드 test1, test2, test3가 있을 때, 각각을 실행할 때마다 테스트 클래스의 인스턴스가 새로 생성된다.
따라서 UserDao를 한번 초기화했다고 해서 모든 테스트 메서드가 같은 인스턴스 멤버를 사용할 수 있는 것이 아니다.

예를 들어 `dao`를 한번만 초기화하기 위해서 setUp() 메서드에 @BeforeAll 애노테이션을 붙였다고 가정하자.
```java
class UserDaoTest {

    private UserDao dao;

    @BeforeAll
    void setUp() {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        dao = ac.getBean("userDao", UserDao.class);
    }
    
    ...
}
```

이렇게 하고 테스트 클래스를 실행하면 setUp 메서드를 스태틱 메서드로 변경하거나 클래스의 TestInstance 생명주기를 클래스 단위로 변경하라는 오류가 발생한다.
오류가 발생하지 않게 하기 위해서는 클래스 위에 다음과 같은 애노테이션을 붙여줘야한다.

```java
@TestInstance(Lifecycle.PER_CLASS)
class UserDaoTest {

    private UserDao dao;

    @BeforeAll
    void setUp() {
        ApplicationContext ac = new GenericXmlApplicationContext(
            "chapter2.unit2/applicationContext.xml");

        dao = ac.getBean("userDao", UserDao.class);
    }
    
    ...
}
```

위 애노테이션이 붙으면 테스트 클래스의 인스턴스를 단 한번만 생성한다. 다시 말해, 이전에는 각 메서드가 실행될 때마다 클래스의 인스턴스가 한번씩 생성되었는데
이제는 처음에 딱 한번만 생성하고 나머지 메서드들이 해당 인스턴스를 공유하게 되는 것이다. 이렇게 따로 애노테이션을 붙여주지 않으면 JUnit은 기본적으로
각 메서드마다 인스턴스를 따로 생성한다. deleteAll() 메서드를 setUp()에 추가하면 차이를 확실히 알 수 있는데, @BeforeAll이 붙은 경우에는 당연히 오류가 난다.
deleteAll() 메서드는 각 테스트 실행 전에 한번씩 수행되어야 할 작업이기 때문이다.

현재 상태에서는 등록된 빈이 몇개 없기 때문에 @BeforeAll을 쓰든 @BeforeEach를 쓰든 크게 상관이 없다. 그런데 애플리케이션이 점차 발전해서 빈이 수백개 등록된다면
매 테스트 메서드마다 새로운 컨텍스트를 생성하는 것은 매우 비효율적이다. 이를 위해 스프링이 직접 애플리케이션 컨텍스트 테스트 지원 기능을 제공하는데, 이를 사용하면
애플리케이션 컨텍스트는 단 한번 생성된다.

스프링이 지원하는 기능을 JUnit에서 사용하기 위해선 @ExtendWith 애노테이션을 통해 확장기능을 지정해주어야 한다.
그리고 나서 @ContextConfiguration으로 사용할 애플리케이션 컨텍스트를 지정한다. 그리고 @Autowired로 값을 주입해준다. 코드는 다음과 같다

```java
@ExtendWith(SpringExtension.class)  // 확장 기능으로 SpringExtendtion 지정
@ContextConfiguration(locations = "/chapter2.unit2/applicationContext.xml")  // 컨텍스트 위치 전달
class UserDaoTest {

    @Autowired    // 컨텍스트 주입
    private ApplicationContext ac;
    ...

    @BeforeEach
    void setUp() throws SQLException {
        dao = ac.getBean("userDao", UserDao.class);
        ...
    }
}
```

코드를 보면 인스턴스 변수인 ac를 초기화하는 코드가 없다. 그런데도 NullPointerException이 터지지않고 테스트가 잘 작동한다.
@Autowired를 읽고 스프링이 값을 주입했기 때문이다.

애플리케이션 컨텍스트를 지정할 때 xml이 아니라 클래스라면 classes로 값을 넘겨준다. 우리가 사용중인 예제에서는 다음과 같다.

```java
@ContextConfiguration(classes = DaoFactory.class)
```

스프링 테스트 컨텍스트 프레임워크의 기능은 하나의 테스트 클래스 안에서 애플리케이션 컨텍스트를 공유해주는 것이 전부가 아니다. 여러 개의 테스트 클래스가 있는데 모두 같은
설정 파일을 가진 애플리케이션 컨텍스트를 사용한다면, 스프링은 테스트 클래스 사이에서도 애플리케이션 컨텍스트를 공유하게 해준다.

`@Autowired`가 붙은 인스턴스 변수가 있으면, 테스트 컨텍스트 프레임워크는 변수 타입과 일치하는 컨텍스트 내의 빈을 찾는다. 타입이 일치하는 빈이 있으면
인스턴스 변수에 주입해준다. 일반적으로는 주입을 위해서는 생성자나 수정자 메서드같은 메서드가 필요하지만, 이 경우에는 메서드가 없어도 주입이 가능하다.
또 별도의 DI 설정 없이 필드의 타입정보를 이용해 빈을 자동으로 가져올 수 있는데, 이런 방법을 타입에 의한 자동와이어링이라고 한다.

@Autowired를 이용해 빈을 DI 받을 수 있다면, 컨테이너에 빈으로 등록된 UserDao 또한 굳이 우리가 직접 생성해줄 필요가 없다.

```java
@Autowired
private UserDao dao;
```

@Autowired를 지정하기만 하면 어떤 빈이든 다 가져올 수 있다. 단 같은 타입의 빈이 두개 등록된 경우에는 다른 방법을 사용해야한다.

<br />

**테스트 코드에 의한 DI**

현재 DB에 유용한 데이터가 저장되어 있는데 테스트에서 deleteAll() 메서드로 정보를 다 날려버리면 안되기 때문에 테스트에서만 사용할 DB를 따로 구성하는 것이 좋아보인다.
이런 경우엔 테스트 코드에 의한 DI를 이용해서 테스트 중에 DAO가 사용할 DataSource 오브젝트를 바꿔주는 방법을 사용하면된다.

```java
@DirtiesContext  // 테스트 메서드에서 애플리케이션 컨텍스트의 구성이나 상태를 변경한다는 것을 테스트 컨텍스트 프레임워크에 알려준다
class UserDaoTest {
    ...
    
    @BeforeEach
    void setUp() {
        ...
        DataSource dataSource = new SingleConnectionDataSource(...);
        dao.setDataSource(dataSource);
    }
}
```

`@DirtiesContext` 애노테시션을 추가하면 테스트 컨텍스트는 이 애노테이션이 붙은 테스트 클래스에는 애플리케이션 컨텍스트를 공유하지 않는다.
테스트 메서드를 수행하고 나면 매번 새로운 애플리케이션 컨텍스트를 만들어 다음 테스트가 사용하게 해준다.

이처럼 테스트 코드에서 빈 오브젝트에 수동으로 DI하는 방법은 장점보다 단점이 많다. 코드가 많아져 번거롭기도 하고 애플리케이션 컨텍스트도 매번 새로
만들어야 하는 부담이 있다. 그렇기 때문에 아예 테스트에 사용될 DataSource 클래스가 빈으로 정의된 테스트 전용 설정파일을 따로 만들어두는 방법을 이용해도 된다.

또 다른 방법으로는 아얘 스프링 컨테이너 없는 DI 테스트를 만들 수 있다.

```java
class UserDaoTest {

    UserDao dao;

    @BeforeEach
    void setUp() {
        ...
        dao = new UserDao();
        DataSource dataSource = new SingleConnectionDataSource(...);
        dao.setDataSource(dataSource);
    }
}
```

테스트를 만들 때는 항상 이렇게 스프링 컨테이너 없이 테스트할 수 있는 방법을 가장 우선적으로 고려해야한다. 이 방법이 테스트 수행 속도가 가장 빠르고
테스트 자체가 간결하다. 테스트를 위해 필요한 오브젝트의 생성과 초기화가 단순하다면 이 방법을 가장 먼저 고려해야한다.

<br />