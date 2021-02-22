# 토비의 스프링 읽기
## Chapter 1 - 오브젝트와 의존관계
### 1.1 초난감 DAO
> DAO(Data Access Object): DB를 사용해 데이터를 조회하거나
> 조작하는 기능을 전담하도록 만든 오브젝트를 일컫는다.

> 자바빈(JavaBean): 원래 비주얼 툴에서 조작 가능한 컴포넌트를 지칭하는 용어였으나
> 지금은 다음의 두 관례를 따르는 오브젝트를 가리킨다.
> 1. 디폴트 생성자를 갖는다. 툴이나 프레임워크에서 리플렉션을 이용해 오브젝트를 생성하는데 필요하기 때문.
> 2. 프로퍼티를 갖는다. 자바빈이 노출하는 이름을 가진 속성을 프로퍼티라 한다.
> 흔히 말하는 게터세터(Getter & Setter)

### 1.2 DAO의 분리

객체지향 패러다임에서 가장 중요하게 여기는 것은 변화에 대한 대응이다.

변경 사항이 생길 때, 최소한의 코드 수정으로 변화에 대응하기 위해서는
**분리와 확장**을 고려한 설계가 필요하다.

#### 분리
변경에 대한 요청은 한가지 관심사에 대해서만 일어난다.
따라서 하나의 관심사는 한 구역에 몰아 넣어야, 변경 요청이 있을 때 여러군데를 손 봐야하는 일이 생기지 않는다.
DB 접속용 암호를 변경하라는 요청이 들어왔는데, 클래스를 수백개 고쳐야하는 상황이 발생해서는 안되지 않는가.
관심이 같은 것끼리는 모으고, 관심이 다른 것은 떨어뜨려놓는 관심사의 분리(Separation of Concerns)를 잘 지켜야 한다.

* UserDao의 관심사항
    1. DB 연결을 위한 커넥션을 어떻게 가져올 것인가. 더 세분화하면 어떤 DB를 사용할 것이고, 어떤 로그인 정보를 쓰는지까지도 나눌 수 있다.
    2. 사용자 등록을 위해 SQL 문장을 담을 Statement를 만들고 실행한다.
    3. 작업이 끝나고 리소스를 반환한다.

방법 1 - 중복 메서드 추출
DB 커넥션을 가져오는 부분이 add 메서드와 get 메서드에 중복되어 있으므로 독립된 메서드로 추출한다.

```java
public void add(User user) throws SQLException{
    Connection con=DriverManager.getConnection(
    url, username,password
    );
    ...
}

public User get(String id) throws SQLException{
    Connection con=DriverManager.getConnection(
    url, username, password
    );
    ...
}
```

↓

```java
public void add(User user) throws SQLException{
    Connection con = getConnection();        
    ...
}

public User get(String id) throws SQLException{
    Connection con = getConnection();
    ...
}

private Connection getConnection() throw SQLException {
    return DriverManager.getConnection(url, username, password);
}
```

방법 2 - DB 커넥션 만들기의 독립 (상속을 통한 확장)
우리의 DAO가 발전을 거듭해서 N사와 D사에서 우리의 DAO를 구매하겠다는 연락이 왔다.
그러나 N사와 D사가 각기 다른 종류의 DB를 사용하고 있고 DB 커넥션을 만드는데 있어서 각기 다른 방법을 사용하고 싶다는 것이다.
그리고 UserDao를 구매한 이후에도 DB 커넥션 방법이 종종 변화할 수 있다.

이런 경우 UserDao의 소스코드를 공개하지 않고도 고객 스스로 원하는 DB 커넥션을 만들어 사용할 수 있는 방법은 무엇일까?

UserDao를 추상클래스로 만들고 getConnection 메서드를 추상메서드로 만든 후, N사와 D사는 UserDao를 상속받은 Dao를 사용하게 하면 된다.

```java
public abstract class UserDao {

    public void add(User user) throws SQLException {
        Connection con = getConnection();
        ...
    }

    public User get(String id) throws SQLException {
        Connection con = getConnection();
        ...
    }

    public abstract Connection getConnection() throws SQLException;
    
}
```

N사의 Dao
```java
public class NUserDao extends UserDao{

    private final String url = "";
    private final String username = "";
    private final String password = "";
    
    @Override
    public Connection getConnection() throws SQLException {
        // N사의 독자적인 DB 커넥션 코드
        return DriverManager.getConnection(url, username, password);
    }
}
```

이렇게 슈퍼클래스에 기본적인 로직의 흐름(커넥션 가져오기, SQL 생성, 실행, 반환)을 만들고, 그 기능의 일부를 추상 메서드나
오버라이딩이 가능한 protected 메서드 등으로 만든 뒤 서브클래스에서 이런 메서드를 필요에 맞게 구현하여 사용하도록 하는 방법을
디자인 패턴에서 **Template Method Pattern**이라고 한다.

UserDao의 getConnection 메서드는 Connection 타입의 오브젝트를 생성한다는 기능을 정의해놓은 추상 메서드이다.
NUserDao의 getConnection 메서드는 어떤 Connection 클래스의 오브젝트를 어떻게 생성할 것인지를 결정하는 방법이라고도 볼 수 있다.
이렇게 서브클래스에서 구체적인 오브젝트 생성 방법을 결정하게 하는 것을 **Factory Method Pattern**이라고 한다.

UserDao는 Connection 인터페이스 타입의 오브젝트라는 것 외에는 관심이 없다.
UserDao는 어떤 기능을 사용한다는 데에만 관심이 있고, NUserDao나 DUserDao는 어떤 식으로 Connection 기능을 제공하는지에 관심을 두고 있는 것이다.
또, 어떤 방법으로 Connection 오브젝트를 만들어내는지도 NUserDao와 DUserDao의 관심사항이다.

> **템플릿 메서드 패턴** 
> 
> 상속을 통해 슈퍼클래스의 기능을 확장할 때 사용하는 가장 대표적인 방법이다. 변하지 않는 기능은 슈퍼클래스에 만들어두고
> 자주 변경되며 확장할 기능은 서브클래스에서 만들도록 한다. 슈퍼클래스에서는 미리 추상 메서드 또는 오버라이드 가능한 메서드를 정의해두고
> 이를 활용해 코드의 기본 알고리즘을 담고 있는 템플릿 메서드를 만든다. 슈퍼클래스에서 디폴트 기능을 정의해두거나 비워뒀다가
> 서브클래스에서 선택적으로 오버라이드할 수 있도록 만들어둔 메서드를 훅(hook) 메서드라고 한다.

> **팩토리 메서드 패턴**
> 
> 슈퍼클래스의 코드에서는 서브클래스에서 구현할 메서드를 호출해서 필요한 타입의 오브젝트를 가져와 사용한다. 이 메서드는 주로 인터페이스 타입으로
> 오브젝트를 리턴하므로 서브클래스에서 구체적으로 어떤 오브젝트를 리턴하는지는 알 수 없다. 그냥 해당 타입의 인터페이스에 정의된 메서드를 사용하는 것이다.
> 이렇게 서브클래스에서 오브젝트 생성 방법과 클래스를 결정할 수 있도록 미리 정의해둔 메서드를 팩터리 메서드라고 하고, 이 방식을 통해
> 오브젝트 생성 방법을 나머지 로직, 즉 슈퍼클래스의 기본 코드에서 독립시키는 방법을 팩토리 메서드 패턴이라고 한다.