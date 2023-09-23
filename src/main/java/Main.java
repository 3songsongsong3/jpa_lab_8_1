import entity.Address;
import entity.Member;
import entity.Product;
import entity.Team;

import javax.persistence.*;
import java.sql.SQLOutput;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa_lab_8_1");
        EntityManager em = emf.createEntityManager();
        EntityTransaction et = em.getTransaction();
        et.begin();

        typeQuery(em);
        query(em);
        bindParams(em, "member1");
        locateParams(em, "member1");
    }

    /**
     * 작성한 JPQL을 실행하려면 쿼리 객체를 만들어야 한다.
     * 쿼리 객체는 TypedQuery와 Query가 있는데
     * 반환할 타입을 명확하게 지정할 수 있으면 TypedQuery객체를 사용하고,
     * 명확하게 지정할 수 없으면 Query 객체를 사용하면 된다.
     * @param em 엔티티 매니저
     */
    public static void typeQuery(EntityManager em) {
        // 두 번째 파라미터에 반환할 타입을 지정하면 TypeQuery를 반환하고
        // 지정하지 않으면 Query를 반환한다.
        // 조회 대상이 Member 엔티티이므로 조회 대상 타입이 명확하다.
        TypedQuery<Member> query =
                em.createQuery("SELECT m FROM Member m", Member.class);
        List<Member> resultList = query.getResultList();
        for(Member member: resultList) {
            System.out.println("member = " + member);
        }
    }


    /**
     * 작성한 JPQL을 실행하려면 쿼리 객체를 만들어야 한다.
     * 쿼리 객체는 TypedQuery와 Query가 있는데
     * 반환할 타입을 명확하게 지정할 수 없으면 Query 객체를 사용하면 된다.
     * @param em 엔티티 매니저
     */
    public static void query(EntityManager em) {
        // 조회 대상이 String 타입인 회원 이름과 Integer 타입인 나이이므로 조회 대상 타입이 명확하지 않다.
        Query query =
                em.createQuery("SELECT m.username, m.age FROM Member m");
        List<Member> resultList = query.getResultList();
        for(Object o: resultList) {
            Object[] result = (Object[]) o;
            System.out.println("username = " + result[0]);
            System.out.println("age = " + result[1]);
        }
    }

    /**
     * 파라미터 바인딩
     * @param em 엔티티 매니저
     * @param usernameParam 유저이름
     */
    public static void bindParams(EntityManager em, String usernameParam) {

        String username = "User1";

        // :username 이라는 이름 기준 파라미터를 정의
        TypedQuery<Member> query =
                em.createQuery("SELECT m FROM Member m where m.username = :username", Member.class);

        // username이라는 이름으로 파라미터를 바인딩한다.
        query.setParameter("username", usernameParam);
        List<Member> resultList = query.getResultList();

        for(Member member: resultList) {
            System.out.println("member : " + member);
        }

        // 메소드 체인 방식 활용
        List<Member> members = em.createQuery("SELECT m FROM Member m where m.username = :username", Member.class)
                .setParameter("username", usernameParam)
                .getResultList();
    }

    /**
     * 위치 기준 파라미터를 사용하려면 ? 다음에 위치 값을 주면 된다. 위치 값은 1부터 시작된다.
     * @param em
     * @param usernameParam
     */
    public static void locateParams(EntityManager em, String usernameParam) {

        List<Member> members = em.createQuery("SELECT m FROM Member m where m.username = ?1", Member.class)
                .setParameter(1, usernameParam)
                .getResultList();
    }

    /**
     * 프로젝션
     * SELECT 절에 조회할 대상을 지정하는 것을 프로젝션이라 하고 [SELECT (프로젝션 대상) FROM]으로 대상을 선택한다.
     * 프로젝션 대상은 엔티티, 엠비디드 타입, 스칼라 타입이 있다. 스칼라 타입은 숫자, 문자 등 기본 데이터 타입을 뜻한다.
     */
    public static void projection(EntityManager em) {
        // 엔티티 프로젝션
        // SELECT m FROM Member m
        // SELECT m.team FROM Member m
        // 원하는 객체를 바로 조회한다. 컬럼을 하나하나 나열하는 SQL과는 차이가 있다.
        // 이렇게 조회한 엔티티는 영속성 컨텍스트에서 관리된다.

        // 임베디드 타입 프로젝션
        // 엔티티와 거의 비슷하게 사용된다.
        // 임베디드 타입은 조회의 시작점이 될 수 없다는 제약이 있다.
        // SELECT a FROM Address a (x)
        // Order 엔티티가 시작점이다. 이렇게 엔티티를 통해서 임베디드 타입을 조회한다.
        String query = "SELECT o.address FROM Order o";
        List<Address> addresses = em.createQuery(query, Address.class).getResultList();
        // 임베디드 타입은 엔티티 타입이 아닌 값 타입이다. 따라서 이렇게 조회한 임베디드 타입은 영속성 컨텍스트에서 관리되지 않는다.

        // 스칼라 타입 프로젝션
        // 숫자 문자 날짜와 같은 기본 데이터 타입들을 스칼라 타입이라 한다.
        List<String> username =
                em.createQuery("SELECT username FROM Member m", String.class)
                        .getResultList();
        // 중복 데이터를 제거하려면 DISTINCT를 사용한다.
        // SELECT DISTINCT username FROM Member m

        // 여러 값 조회
        // 엔티티를 대상으로 조회하면 편리하겠지만, 꼭 필요한 데이터들만 선택해서 조회해야 할 때도 있다.
        // 프로젝션에 여러 값을 선택하면 TypeQuery를 사용할 수 없고 대신에 Query를 사용해야 한다.
        /*
        Query query2 =
                em.createQuery("SELECT m.username, m.age FROM Member m");

        List resultList = query2.getResultList();

        Iterator iterator = resultList.iterator();
        while (iterator.hasNext()) {
            Object[] row = (Object[]) iterator.next();
            String username2 = (String) row[0];
            Integer age = (Integer) row[1];
        }
        */
        // 위의 소스를 간결하게 수정 1
        List<Object[]> resultList =
                em.createQuery("SELECT m.username, m.age FROM Member m")
                        .getResultList();

        for (Object[] row : resultList) {
            String username2 = (String)row[0];
            Integer age = (Integer)row[1];
        }

        // 비슷한 소스
        List<Object[]> resultList2 =
                em.createQuery("SELECT o.member, o.product, o.orderAmount FROM Order o")
                        .getResultList();

        for (Object[] row: resultList2) {
            Member member = (Member)row[0];
            Product product = (Product)row[1];
            Integer age = (Integer) row[2];
        }
    }

    /**
     * JPA는 페이징을 다음 두 API로 추상화했다.
     * setFirstResult(int startPosition) 조회 시작 위치(0부터 시작한다)
     * setMaxResults(int maxResult) 조회할 데이터 수
     * @param em
     */
    public static void setPagingInfo(EntityManager em) {

        // FirstResult의 시작은 0이므로 11번째부터 시작해서 총 20건의 데이터를 조회한다.
        // 11~30번 데이터를 조회한다.
        // 데이터베이스마다 다른 페이징 처리를 같은 API로 처리할 수 있는 것은 데이터베이스 방언 덕분이다.
        TypedQuery<Member> query =
                em.createQuery("SELECT m FROM Member m ORDER BY m.username DESC",
                        Member.class);
        query.setFirstResult(10);
        query.setMaxResults(20);
        query.getResultList();
    }

    /**
     * 내부 조인은 INNER JOIN을 사용한다. INNER는 생략할 수있다.\
     * 외부 조인은 LEFT OUTER JOIN을 사용한다. OUTER는 생략 가능해서 보통 LEFT JOIN으로 사용한다.
     * @param em
     */
    public static void innerJoin(EntityManager em) {

        // 조인 시 연관 필드 사용
        String teamName = "팀A";
        String query = "SELECT m FROM Member m INNER JOIN m.team t "
                        + "WHERE t.name = :teamName";
        // Member 타입 사용 가능
        List<Member> resultList = em.createQuery(query, Member.class)
                .setParameter("teamName", teamName)
                .getResultList();


        String query2 = "SELECT m, t FROM Member m JOIN m.team t";
        List<Object[]> resultList2 = em.createQuery(query2).getResultList();

        for (Object[] row: resultList2) {
            Member member = (Member) row[0];
            Team team = (Team) row[1];
        }
        /*
         외부 조인
            SELECT m
            FROM Member m LEFT [OUTER] JOIN m.team t
         */

        /*
         컬렉션 조인
            일대다 관계나 다대다 관계처럼 컬렉션을 사용하는 곳에 조인하는 것을 컬렉션 조인이라 한다.

            SELECT t, m FROM Team t LEFT JOIN t.members m

            여기서 t LEFT JOIN t.members는 팀과 팀이 보유한 회원목록을 컬렉션 값 연관 필드로 외부 조인했다.
         */

        /*
          세타 조인

          WHERE 절을 사용해서 세타 조인을 할 수 있다.
          참고로 세타 조인은 내부 조인만 지원한다.
          세타 조인을 사용하면 전혀 관계없는 엔티티도 조인할 수있다.

          SELECT count(m) FROM Member m, Team t
          WHERE m.username = t.name
         */

        /*
          JOIN ON 절

          JPA 2.1부터 조인할 때 ON절을 지원한다.
          ON 절을 사용하면 조인 대상을 필터링하고 조인할 수 있다.

          SELECT m,t FROM Member m
          LEFT JOIN m.team t on t.name = 'A'
         */
    }

    /**
     * 페치 조인
     * @param em
     */
    public static void fetchJoin(EntityManager em) {
        /*
            페치 조인은 SQL에서 이야기하는 조인의 종류는 아니고 JPQL에서 성능 최적화를 위해 제공하는 기능이다.
            연관된 엔티티나 컬렉션을 한 번에 같이 조회하는 기능인데 join fetch 명령어로 사용할 수 있다.

            select m
            from Member m join fetch m.team

            join 다음에 fetch라 적었다. 이렇게 하면 연관된 엔티티나 컬렉션을 함께 조회한다.
            회원(m)과 팀(m.team)을 함께 조회한다.

            m.team에 별칭이 없는데, 페치 조인은 별칭을 사용할 수 없다.

            실행된 SQL은 다음과 같다.

            SELECT
                M.*, T.*
            FROM MEMBER T
                INNER JOIN TEAM T ON M.TEAM_ID = T.ID

            엔티티 페치 조인 JPQL에서 select m으로 회원 엔티티만 선택했는데,
            실행된 SQL을 보면 SELECT M.*, T.*로 회원과 연관된 팀도 함께 조회된 것을 확인할 수 있다.

         */
        String jpql = "select m from Member m join fetch m.team";
        List<Member> members = em.createQuery(jpql, Member.class).getResultList();

        for (Member member : members) {
            System.out.println("username = " + member.getUsername() + ", " +
                    "teamname = " + member.getTeam().name());
        }

        /*
            컬렉션 페치 조인

            일대다 관계인 컬렉션을 페치 조인해보자.

            select t
            from Team t join fetch t.members
            where t.name = '팀A'

            SELECT
                T.*, M.*
            FROM TEAM T
               INNER JOIN MEMBER M ON T.ID = M.TEAM_ID
            WHERE T.NAME = '팀A'
         */
        String jpql2 = "select t from Team t join fetch t.members where t.name = '팀A'";
        List<Team> teams = em.createQuery(jpql2, Team.class).getResultList();

        for(Team team : teams) {
            System.out.println("teamname = " + team.getName() + ", " +
                    "team = " + team);
            for (Member member : team.getMembers()) {
                // 패치 조인으로 팀과 회원을 함께 조회해서 지연 로딩 발생 안함
                System.out.println(
                        "->username = " + member.getUsername() + "," +
                                "member = " + member
                );
            }
        }

        /*
            페치조인과 DISTINCT

            JPQL의 DISTINCT 명령어는 SQL에 DISTINCT를 추가하는 것은 물론이고 애플리케이션에서 한 번 더 중복을 제거한다.

            String distinctSql = "select distinct t from Team t join fetch t.members where t.name = '팀A'";

            로우 번호 ||    팀      ||   회원
            1              팀A          회원1
            2              팀A          회원2
            -----------------------------------
            select distinct t의 의미는 팀 엔티티의 중복을 제거하라는 것이다.

            출력 결과는 다음과 같다.
            teamname  = 팀A, team = Team@0x100
            -> username = 회원1, member = Member@0x200
            -> username = 회원2, member = Member@0x300
         */

        /*
            페치 조인과 일반 조인의 차이

            select t
            from Team t join t.members m
            where t.name = '팀A'

            -->

            SELECT
                T.*
            FROM TEAM T
            INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
            WHERE T.NAME = '팀A'

            JPQL에서 팀과 회원 컬렉션을 조인했으므로 회원 컬렉션도 함께 조회되지 않는다!!!

            JPQL은 결과를 반환할 때 연관관계 까지 고려하지 않는다.
            단지 SELECT 절에 지정한 엔티티만 조회할 뿐이다.
            --------------------------------------------------------
            반면에 페치 조인을 사용하면 연관된 엔티티도 함께 조회한다.

            select t
            from Team t join fetch t.members
            where t.name = '팀A'

            SELECT
                T.*, M.*
            FROM TEAM T
            INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
            WHERE T.NAME = '팀A'
         */
    }
    public static void testPassExpression(EntityManager em) {

        /*
            경로 표현식이라는 것은 쉽게 이야기해서 .(점)을 찍어 객체 그래프를 탐색하는 것이다.

            select m.username
            from Member m
                join m.team t
                join m.orders o
            where t.name = '팀A'

            여기서 m.username, m.team, m.orders, t.name이 모두 경로 표현식을 사용한 예다.
         */

        /*
            JPQL에서 경로 표현식을 사용해서 경로 탐색을 하려면 다음 3가지 경로에 따라 어떤 특징이 있는지 이해해야 한다.

            1. 상태 필드 경로 : 경로 탐색의 끝이다. 더는 탐색할 수 없다.
            2. 단일 값 연관 경로 : 묵시적으로 내부 조인이 일어난다. 단일 값 연관 경로는 계속 탐색할 수 있다.
            3. 컬렉션 값 연관 경로 : 묵시적으로 내부 조인이 일어난다. 더는 탐색할 수 없다.
                                   단, FROM 절에서 조인을 통해 별칭을 얻으면 별칭으로 탐색할 수 있다.

            1. 상대 필드 경로 탐색
                select m.username, m.age from Member m
            2. 단일 값 연관 경로
                select o.member from Order o
                JPQL을 보면 o.member를 통해 주문에서 회원으로 단일 값 연관 필드로 경로 탐색을 했다.
                단일 값 연관 필드로 경로 탐색을 하면 SQL에서 내부 조인이 일어나는데, 이것을 묵시적 조인이라고 한다.
                묵시적 조인은 모두 내부 조인이다. 외부 조인은 명시적으로 JOIN 키워드를 사용해야 한다.

                - 명시적 조인 : JOIN을 직접 적어주는 것
                    select m from Member m JOIN m.team t
                - 묵시적 조인 : 경로 표현식에 의해 묵시적으로 조인이 일어나는 것, 내부 조인 INNER JOIN만 할 수 있다.
                    SELECT m.team FROM Member m
            3. 컬렉션 값 연관 경로 탐색
                JPQL을 다루면서 많이 하는 실수 중 하나는 컬렉션 값에서 경로 탐색을 시도 하는 것이다.

                select t.members from Team t // 성공
                select t.members.username from Team t // 실패

                t.members 처럼 컬렉션 까지는 경로 탐색이 가능하다. 하지만 t.members.username 처럼 컬렉션에서의 경로 탐색은 허용하지 않는다.

                만약 컬렉션에서 경로 탐색을 하고 싶으면 다음 코드처럼 조인을 사용해서 새로운 별칭을 획득해야 한다.
                select m.username from Team t join t.members m

         */
    }

    /**
     * 서브쿼리
     * @param em
     */
    public static void testSubQuery(EntityManager em) {
        /*
            JPQL도 SQL처럼 서브 쿼리를 지원한다.
            여기에는 몇 가지 제약이 있는데 서브 쿼리를 WHERE, HAVING 절에서만 사용할 수 있고
            SELECT, FROM 절에서는 사용할 수 없다.

            select m from Member m
            where m.age > (select avg(m2.age) from Member m2)

            다음은 한 건이라도 주문한 고객을 찾는다.
            select m from Member m
            where (select count(o) from Order o where m = o.member) > 0
            ==
            select m from Member m
            where m.orders.size > 0
         */

        /*
            서브 쿼리 함수

            서브쿼리는 다음 함수들과 깉이 사용할 수 있다.
            [NOT] EXISTS
            {ALL | ANY | SOME}
            [NOT] IN

            EXISTS
            서브쿼리에 결과가 존재하면 참이다. NOT은 반대
            select m from Member m
            where exists (select t from m.team t where t.name = '팀A')

            {ALL | ANY | SOME}
            비교 연산자와 같이 사용한다.
            - ALL : 조건을 모두 만족하면 참이다.
            - ANY 혹은 SOME : 둘은 같은 의미다. 조건을 하나라도 만족하면 참이다.

                예 : 전체 상품 각각의 재고보다 주문량이 많은 주문들
                select o from Order o
                where o.orderAmount > ALL (select p.stockAmount from Product p)

                예 : 어떤 팀이든 팀에 소속된 회원
                select m from Member m
                where m.team = ANY (select t from Team t)

            IN
            서브쿼리의 결과 중 하나라도 같은 것이 있으면 참이다.

                예 : 20세 이상을 보유한 팀
                select t from Team t
                where t IN (select t2 from Team t2 JOIN t2.members m2 where m2.age >= 20)

         */
    }

    /**
     * 조건식
     */
    public static void testExpression(EntityManager em) {
        /*
            컬렉션 식

            컬렉션 식은 컬렉션에만 사용하는 특별한 기능이다.
            컬렉션은 컬렉션 식 이외에 다른 식은 사용할 수 없다.

            문법 : {컬렉션 값 연관 경로} IS [NOT] EMPTY
            설명 : 컬렉션에 값이 비었으면 참

            // JPQL : 주문이 하나라도 있는 회원 조회
            select m from Member m
            where m.orders is not empty

            // 실행된 SQL
            select m.* from Member m
            where
                exists (
                    select o.id
                    from Orders o
                    where m.id = o.member_id
                )
            컬렉션은 컬렉션 식만 사용할 수 있다.
            is null은 사용 불가

            // 컬렉션의 멤버 식
            문법 : {엔티티나 값} [NOT] MEMBER [OF] {컬렉션 값 연관 경로}
            설명 : 엔티티나 값이 컬렉션에 포함되어 있으면 참
            예
            select t from Team t
            where :memberParam member of t.members
         */

        /*
            CASE 식

            특정 조건에 따라 분기할 때 CASE 식을 사용한다. CASE 식은 4가지 종류가 있다.
            - 기본 CASE
                select
                    case when m.age <= 10 then '학생요금'
                         when m.age >= 60 then '경로요금'
                         else '일반요금'
                    end
                from Member m

            - 심플 CASE
                select
                    case t.name
                        when '팀A' then '인센티브110%'
                        when '팀B' then '인센티브120%'
                        else '인센티브105%'
                    end
                from Team t

            - COALESCE
                스칼라식을 차례대로 조회해서 null이 아니면 반환한다.
                예 : m.username이 null이면 '이름 없는 회원을 반환하라.
                select coalesce(m.username, '이름 없는 회원') from Member m

            - NULLIF
                두 값이 같으면 null을 반환하고 다르면 첫번째 값을 반환한다.
                예 : 사용자 이름이 '관리자'면 null을 반환하고 나머지는 본인의 이름을 반환하라
               select NULLIF(m.username, '관리자') from Member m
         */
    }

    /**
     * 엔티티를 직접 사용하여 조회
     * @param em
     */
    public static void selectByEntity(EntityManager em) {

        /*
            엔티티 직접 사용

            객체 인스턴스는 참조 값으로 식별하고 테이블 로우는 기본 키 값으로 식별한다.
            JPQL에서 엔티티 객체를 직접 사용하면 SQL에서는 해당 엔티티의 기본 키 값을 사용한다.
         */
        String qlString = "select m from Member m where m = :member";
        List resultList = em.createQuery(qlString)
                .setParameter("member", member)
                .getResultList();
        /*
            실행된 SQL

            select m.*
            from Member m
            where m.id = ?
         */

        /*
            식별자 값을 직접 사용하는 코드
         */
        String qlString2 = "select m from Member m where m.id = :memberId";
        List resultList2 = em.createQuery(qlString2)
                .setParameter("memberId", 4L)
                .getResultList();
        ////////////////////////////////////////////////////////////////////////////
        /*
            외래 키 값
         */
        Team team = em.find(Team.class, 1L);
        String qlString3 = "select m from Member m where m.team = :team";
        List resultList3 = em.createQuery(qlString3)
                .setParameter("team", team)
                .getResultList();
        /*
            기본키 값이 1L인 팀 엔티티를 파라미터로 사용하고 있다.
            m.team은 현재 team_id라는 외래 키와 매핑되어 있다.
         */

        /*
            외래키에 식별자를 직접 사용하는 코드
         */
        String qlString4 = "select m from Member m where m.team.id = :teamId";
        List resultList4 = em.createQuery(qlString4)
                .setParameter("teamId", 1L)
                .getResultList();

    }
}




