package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeOnlyDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TradeOnlyRepository extends CrudRepository<TradeOnlyDto, Integer> {
    List<TradeOnlyDto> findAll();

//    @Query(name = "select * from trade t where e.rank = ?", nativeQuery = true)
    Optional<TradeOnlyDto> findByRank(int rank);

//    @Query(name = "select * from trade t where t.rs_event_id = :rsEventId", nativeQuery = true)
//    Optional<TradeDto> findAccordingToRsEventId(int rsEventId);

    Optional<TradeOnlyDto> findByRsEventId(int rsEventId);

//    void deleteByRsEventId(int rsEventId);
}
