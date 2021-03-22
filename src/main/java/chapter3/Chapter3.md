
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

deleteAll() 메서드에서 변하지 않는 부분과 변하는 부분은 무엇일까? 커넥션을 얻고 쿼리를