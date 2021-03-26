
# Chapter 3. 템플릿

객체지향 설계의 핵심 원칙인 개방 폐쇄 원칙(OCP)을 다시 살펴보자. 이 원칙은 코드에서 어떤 부분은 변경을 통해 그 기능이 다양해지고 확장하려는 성질이 있고,
어떤 부분은 고정되어 있고 변하지 않으려는 성질이 있음을 말해준다. 따라서 변화의 특성이 다른 부분을 구분해주고, 각각 다른 목적과 다른 이유에 의해 다른 시점에
독립적으로 변경될 수 있는 효율적인 구조를 만들어주는 것이 개방 폐쇄 원칙의 핵심이다.

템플릿이란 이렇게 바뀌는 성질이 다른 코드 중에서 변경이 거의 일어나지 않으며 일정한 패턴으로 유지되는 특성을 가진 부분을 자유롭게 변경되는 성질을 가진
부분으로부터 독립시켜 효과적으로 활용할 수 있도록 하는 방법이다.

3장에서는 스프링에 적용된 템플릿 기법을 살펴보고, 이를 적용해 완성도 있는 DAO 코드를 만드는 방법을 알아본다.

책에서는 jdbc 코드를 리팩토링하는 과정을 통해 객체 지향의 원칙을 지키면 자연스럽게 스프링의 jdbc 템플릿이 가진 구조가 만들어진다는 것을 설명한다.
이후 jdbc 템플릿에 대해 한 unit을 할애해서 설명하는데, 요즘은 jdbc 템플릿을 거의 사용하지 않기 때문에 따로 정리를 하지는 않겠다.
이보다는 이 장의 핵심적인 내용인 템플릿/콜백 패턴을 이해하는데 주력한다.

## 3.1 다시보는 초난감 DAO

UserDao 코드에는 여전히 문제가 남아있는데, 이는 바로 예외 처리에 대한 코드가 없다는 것이다. DB 커넥션은 제한된 리소스이기 때문에 이를 사용하는 코드에는
반드시 예외처리를 해줘야한다. 정상적인 흐름을 벗어나 예외가 발생했을 경우에도 리소스는 반드시 반환해줘야 하기 때문이다.

```java
public class UserDao {
    ...
    
    public void deleteAll() throws SQLException {
        Connection con = dataSource.getConnection();

//      여기서부터
        PreparedStatement ps = con.prepareStatement("delete from users");
        ps.executeUpdate();
//      여기까지
        
        ps.close();
        con.close();
    }
    
    ...
}
```

UserDao에 deleteAll() 메서드를 보자. 만약 위에 표시해둔 부분에서 예외가 발생한다면 어떻게 될까? 메서드가 호출될 때마다
DB 커넥션을 반환하지 않고 계속 사용하기만하니 자원이 계속 줄어든다. 만약 다중 이용자가 사용하는 서버에서 이런 일이 발생한다면
얼마 지나지 않아 컴퓨터는 자원이 모자란다는 오류를 내며 종료된다.

그렇기 때문에 이런 jdbc 코드에서는 try-catch-finally 구문을 사용하도록 권장한다. finally 구문 안에 자원을 반환하는 코드를 넣으면
무슨 일이 있어도 자원이 반환되기 때문이다.

우선 deleteAll()과 getCount()만 예외처리를 해보았다.
```java
public class UserDao {
    
    ...
    
    public void deleteAll() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("delete from users");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) { }
            }

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {}
            }
        }
    }

    public int getCount() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("select count(*) from users");

            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {}
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {}
            }
        }
    }
}
```

## 3.2 변하는 것과 변하지 않는 것

일단 코드를 보면 한숨부터 나온다. finally 블록 안에 if 블록이 있고 그 안에 또 try-catch 구문이 나타난다. 반복되는 코드도 너무 많다.
이런 복잡한 코드는 버그와 오류를 찾기 매우 힘들다.

이 코드를 어떻게 리팩토링할 수 있을까? 위 코드의 문제를 해결하는 핵심은 변하지 않는, 그러나 많은 곳에서 중복되는 코드와 계속 확장되고 자주 변화하는 코드를 잘
분리해내는 것이다. 자세히 살펴보면 1장에서 살펴봤던 것과 비슷한 문제이고 같은 방법으로 접근하면 된다. 다만 앞선 문제와 성격이 다르기 때문에
해결 방법이 조금 다르다.

deleteAll() 메서드에서 변하지 않는 부분과 변하는 부분은 무엇일까? 자원을 열고 닫는 부분은 변하지 않는 부분이다. 실행할 쿼리를 작성하는
부분은 변하는 부분이다.

### 템플릿 메서드 패턴의 적용

변하지 않는 부분은 슈퍼클래스에 두고 변하는 부분을 추상 메서드로 정의해서 서브클래스에서 오버라이드하도록 만들어보자.

우선 UserDao를 추상클래스로 만들고, 변하는 부분을 추출해서 추상메서드로 만든다.

```java
public abstract class UserDao {
    
    ...

    public void deleteAll() throws SQLException {
        
        ...
        
        try {
            con = dataSource.getConnection();
            
            ps = makeStatement(con);
            
            ps.executeUpdate();
        } ...
    }

    protected abstract PreparedStatement makeStatement(Connection con);
}
```

실제 구현은 UserDao를 상속받는 부분에서 한다.

```java
public class UserDaoDeleteAll extends UserDao{

    @Override
    protected PreparedStatement makeStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("delete from users");
        return ps;
    }
}
```

그런데 이런 접근법은 제한이 많다. 가장 큰 문제는 새로운 기능을 정의할 때마다 새로운 클래스를 만들어줘야 한다는 것이다.
또 확장구조가 이미 클래스를 설계하는 시점에서 고정되어 버린다. UserDao와 서브클래스들의 관계가 컴파일 타임에 결정되어 버리기 때문에
그 관계에 대한 유연성이 떨어진다.

### 전략 패턴 적용

OCP를 잘 지키면서도 템플릿 메서드 패턴보다 더 유연하고 확장성이 뛰어난 것이, 오브젝트를 아얘 분리하고 클래스 레벨에서는 인터페이스를 통해서만
의존하도록 만드는 전략 패턴이다. 전략 패턴에서는 추상화된 인터페이스(Strategy)를 만들고 이를 구현한 클래스에서 변하는 부분을 구체적으로 구현한다.
변하지 않는 부분은 일정한 구조로 동작하다가 변하는 부분이 필요한 시점에 Strategy 인터페이스를 통해 외부의 독립적인 전략 클래스에 위임한다.
이때 Strategy의 클라이언트를 Context라고 부른다.

deleteAll()의 컨텍스트를 정리해보면 다음과 같다.
* DB 커넥션 가져오기
* PreparedStatement를 만들어줄 외부 기능 호출하기
* 전달받은 PreparedStatement 실행하기
* 예외가 발생하면 이를 메서드 밖으로 던지기
* 리소스 닫기

전략 패턴을 적용하기 위해 Strategy 인터페이스를 다음과 같이 작성한다.
```java
public interface StatementStrategy {
    PreparedStatement makePreparedStatement(Connection con) throws SQLException;
}
```

이 인터페이스를 상속해서 실제 전략을 구현한 클래스를 만든다.
```java
public class DeleteAllStatement implements StatementStrategy{

    @Override
    public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("delete from users");
        return ps;    
    }
}
```

그리고 이를 컨텍스트에서 사용한다.
```java
public class UserDao {
    
    ...

    public void deleteAll() throws SQLException {
        
        ...
        
        try {
            con = dataSource.getConnection();

            StatementStrategy strategy = new DeleteAllStatement();
            ps = strategy.makePreparedStatement(con);

            ps.executeUpdate();
        } ...
    }

}
```

하지만 여기서도 문제가 하나 드러난다. Context에 해당하는 UserDao가 구체적인 전략인 DeleteAllStatement에 의존하고 있다.
이래서는 Strategy 인터페이스를 분리한 의미가 없다.

### DI 적용을 위한 클라이언트/컨텍스트 분리

이 문제를 해결하기 위해서 앞서 살펴본 DI를 이용해보자. 즉 Context가 어떤 전략을 사용할지를 Context를 사용하는 클라이언트가
결정하게 만드는 것이다.

이 구조를 적용하려면 우선 컨텍스트 코드를 따로 메서드로 분리해줄 필요가 있다. 컨텍스트의 클라이언트는 컨텍스트가 사용할 구체적인 전략을
전달해줘야하므로 StatementStrategy를 매개변수로 받는다.
```java
public class UserDao {
    
    public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = dataSource.getConnection();

            ps = stmt.makePreparedStatement(con);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    
}
```

여기서는 클라이언트를 따로 클래스로 분리하지 않고 deleteAll() 메서드가 클라이언트의 책임을 맡도록 구성해준다.

```java
public class UserDao {
    
    public void deleteAll() throws SQLException {
        StatementStrategy stmt = new DeleteAllStatement();
        jdbcContextWithStatementStrategy(stmt);
    }
    
}
```

이제 구조로 볼 때 완벽한 전략 패턴의 모습을 갖췄다. 특히 클라이언트가 컨텍스트가 사용할 전략을 정해서 전달한다는 면에서
DI 구조라고 이해할 수도 있다.

> **마이크로 DI**
> 의존관계 주입(DI)은 다양한 형태로 적용할 수 있다. DI의 가장 중요한 개념은 제 3자에 의해서 두 오브젝트 사이의 유연한 관계가
> 설정된다는 것이다.
> 
> 일반적으로 DI는 의존관계에 있는 두 개의 오브젝트, 둘의 관계를 설정하는 오브젝트 팩토리(DI 컨테이너), 그리고 이를 사용하는 클라이언트라는
> 4개의 오브젝트 사이에서 일어난다. 하지만 때로는 원시적인 전략 패턴 구조를 따라 클라이언트가 DI 컨테이너의 역할을 맡을 수도 있다.
> 또는 클라이언트와 전략(의존 오브젝트)이 결합될 수도 있다. 심지어는 클라이언트와 DI 관계에 있는 두 개의 오브젝트가 모두 하나의 클래스 안에
> 담길 수도 있다.
> 
> 이런 경우에 DI가 매우 작은 단위의 코드와 메서드 사이에서 일어나기도 한다. 얼핏보면 DI같아 보이지 않지만, 엄연히 DI가 이뤄지고 있다.
> 이렇게 DI의 장점을 단순화해서 IoC 컨테이너의 도움 없이 코드 내에서 적용한 경우를 **마이크로 DI** 혹은 수동 DI라고 부른다.

## 3.3 JDBC 전략 패턴의 최적화

add() 메서드에도 똑같은 방식으로 적용한다.

```java
public class AddStatement implements StatementStrategy{
    User user;

    public AddStatement(User user) {
        this.user = user;
    }

    @Override
    public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
        PreparedStatement ps = con
            .prepareStatement("insert into users (id, name, password) values (?,?,?)");

        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        return ps;
    }
}
```

```java
public class UserDao {
    ...
    public void add(User user) throws SQLException {
        AddStatement stmt = new AddStatement(user);
        jdbcContextWithStatementStrategy(stmt);
    }
    ...
}
```

### 전략과 클라이언트의 동거

지금까지 해온 작업만으로도 많은 문제를 해결했지만 두 가지 아쉬운 점이 있다. 첫 번째는 DAO 메소드마다 새로운 StatementStrategy를
매번 새로 만들어줘야 한다는 것이다. 두 번째는 AddStatement처럼 User같은 추가 정보가 필요한 경우에 오브젝트를 전달받는 생성자와
이를 저장해둘 인스턴스 변수를 번거롭게 만들어야 한다는 것이다.

StatementStrategy의 구체 클래스는 각각 특정 메서드 하나에서 사용되므로, 이를 익명 클래스로 만들어 사용하면
클래스 파일이 늘어나는 문제와, 새로운 인스턴스 변수를 만들어줘야 하는 문제를 한번에 해결할 수 있다.

```java
public class UserDao {
    ...

    public void add(final User user) throws SQLException {

        StatementStrategy stmt = new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con
                    .prepareStatement("insert into users (id, name, password) values (?,?,?)");

                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());

                return ps;
            }
        };

        jdbcContextWithStatementStrategy(stmt);
    }
    ...
}
```

한번만 사용되기 때문에 굳이 참조변수를 선언할 필요도 없다.

```java
public class UserDao {
    public void add(final User user) throws SQLException {

        jdbcContextWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con
                    .prepareStatement("insert into users (id, name, password) values (?,?,?)");

                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());

                return ps;
            }
        });
    }

    public void deleteAll() throws SQLException {
        jdbcContextWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection con) throws SQLException {
                return con.prepareStatement("delete from users");
            }
        });
    }
}
```

