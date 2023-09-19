import entity.Address;
import entity.Member;
import entity.Product;

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
     * @param EntityManager em 엔티티 매니저
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
}
