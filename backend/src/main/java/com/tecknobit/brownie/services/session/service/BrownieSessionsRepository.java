package com.tecknobit.brownie.services.session.service;

import com.tecknobit.brownie.services.session.entity.BrownieSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrownieSessionsRepository extends JpaRepository<BrownieSession, String> {


}
