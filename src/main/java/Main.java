import entity.Member;

import javax.persistence.*;
import java.sql.SQLOutput;
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
}
