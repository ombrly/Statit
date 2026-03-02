package com.charble.backend.Repositories;

import com.charble.backend.Models.GlobalBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GlobalBaselineRepository extends JpaRepository<GlobalBaseline, UUID>
{
    //Null
}
