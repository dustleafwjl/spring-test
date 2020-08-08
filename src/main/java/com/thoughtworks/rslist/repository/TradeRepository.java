package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.TradeOnlyDto;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TradeRepository extends CrudRepository<TradeDto, Integer> {
    Optional<TradeDto> findByRsEventId(int rsEventId);
}
