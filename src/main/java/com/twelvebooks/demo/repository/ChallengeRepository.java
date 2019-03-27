package com.twelvebooks.demo.repository;

import com.twelvebooks.demo.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    //1.User에 해당하는 List<Challenge>가져오는 쿼리
    @Query(value = "SELECT c FROM Challenge c INNER JOIN FETCH c.user WHERE c.user.id = :id ORDER BY c.id DESC",
            countQuery = "SELECT count(c) FROM Challenge c")
    public List<Challenge> getChallenges(@Param("id") Long id);
}
