package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;
    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }


    @Test
    public void startJPQL(){
        // member1  찾기
        Member findMember=em.createQuery("select m from Member m " +
                        "where m.username = :username", Member.class)
                .setParameter("username","member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydslQType(){
        //member static import
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){
        // 리스트조회
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        //단건조회 +limit1
        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .limit(1).fetchOne();


        //처음한건 조회
        Member fetchFirst =  queryFactory
                .selectFrom(member)
                .fetchFirst();

        //페이징에서 사용
        QueryResults<Member> results =queryFactory
                .selectFrom(member)
                .fetchResults();

        /*
        results.getTotal(); //복잡한 쿼리에서는 성능상 사용 X
        List<Member> content= results.getResults();
         */

        //count용 쿼리로변경
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }


    @Test
    public void sort(){
        em.persist(new Member(null
        ,100));
        em.persist(new Member("member6"
                ,100));

        em.persist(new Member("member5"
                ,100));

        /*
        나이 내림차순
        이름 올림차순
        이름이 없으면 마지막에 출력
         */
        List<Member> result =queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");

        assertThat(member6.getUsername()).isEqualTo("member6");

        //assertThat(memberNull.getUsername()).isEqualTo(null);

        assertThat(memberNull.getUsername()).isNull();
    }





}
