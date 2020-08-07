package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.VoteDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends CrudRepository<TradeDto, Integer> {
    List<TradeDto> findAll();

//    @Query(name = "select * from trade t where e.rank = ?", nativeQuery = true)
    Optional<TradeDto> findByRank(int rank);

//    Optional<TradeDto> findByRsEventId();

//    void deleteByRsEventId(int rsEventId);
}
