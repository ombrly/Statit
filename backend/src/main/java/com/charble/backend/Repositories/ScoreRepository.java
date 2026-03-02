package com.charble.backend.Repositories;

import com.charble.backend.Models.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID>
{
    //Null
}
