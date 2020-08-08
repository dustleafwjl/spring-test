package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.AmountIsLessException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;


  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public List<RsEvent> getRsEventsBetween(Integer start, Integer end) {
    List<TradeDto> tradeDtos = getTradeDtos(start, end);
    List<RsEvent> tradeEvents = getRsEventsWithTrade(tradeDtos);
    List<RsEvent> rsEvents = getRsEvents();

    rsEvents = addTradeRsInToRsEvent(start, tradeDtos, tradeEvents, rsEvents);
    return rsEvents;
  }

  public boolean addRsEvents(@RequestBody @Valid RsEvent rsEvent) {
    Optional<UserDto> userDto = userRepository.findById(rsEvent.getUserId());
    boolean userDtoisPresent = userDto.isPresent();
    if(!userDtoisPresent) {
      return userDtoisPresent;
    }
    RsEventDto build =
            RsEventDto.builder()
                    .keyword(rsEvent.getKeyword())
                    .eventName(rsEvent.getEventName())
                    .voteNum(0)
                    .user(userDto.get())
                    .build();
    rsEventRepository.save(build);

    return userDtoisPresent;
  }

  private List<TradeDto> getTradeDtos(Integer start, Integer end) {
    return tradeRepository.findAll().stream()
            .filter(ele->{
              if(start!=null && end != null) {
                return ele.getRank()>start && ele.getRank() <= end;
              }else if(start != null) {
                return ele.getRank() > start;
              } else if(end != null) {
                return ele.getRank() > 0 && ele.getRank() <= end;
              } else {
                return true;
              }
            }).collect(Collectors.toList());
  }

  private List<RsEvent> addTradeRsInToRsEvent(Integer start, List<TradeDto> tradeDtos, List<RsEvent> tradeEvents, List<RsEvent> rsEvents) {
    for (int index = 0; index < tradeDtos.size(); index ++) {
      int rankIndex = tradeDtos.get(index).getRank() - 1;
      if(start != null) {
        rankIndex += start - 1;
      }
      rsEvents.add(rankIndex, tradeEvents.get(index));
    }
    rsEvents = rsEvents.stream().filter(distinctByKey(RsEvent::getEventName)).collect(Collectors.toList());
    return rsEvents;
  }


  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object,Boolean> seen = new ConcurrentHashMap<>();
    return t -> ((ConcurrentHashMap) seen).putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }
  private List<RsEvent> getRsEventsWithTrade(List<TradeDto> tradeDtos) {
    return tradeDtos.stream()
            .map(ele -> rsEventRepository.findById(ele.getRsEvent().getId()).get())
            .sorted(Comparator.comparing(RsEventDto::getVoteNum).reversed())
            .map(
                    item ->
                            RsEvent.builder()
                                    .eventName(item.getEventName())
                                    .keyword(item.getKeyword())
                                    .userId(item.getId())
                                    .voteNum(item.getVoteNum())
                                    .build())
            .collect(Collectors.toList());
  }

  public List<RsEvent> getRsEvents() {
    return rsEventRepository.findAll().stream()
            .sorted(Comparator.comparing(RsEventDto::getVoteNum).reversed())
            .map(item ->
                        RsEvent.builder()
                                .eventName(item.getEventName())
                                .keyword(item.getKeyword())
                                .userId(item.getId())
                                .voteNum(item.getVoteNum())
                                .build())
            .collect(Collectors.toList());
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public void buy(Trade trade, int eventId) {
    Optional<RsEventDto> rsEvent = rsEventRepository.findById(eventId);
    Optional<TradeDto> existTrade = tradeRepository.findByRank(trade.getRank());
    TradeDto tradeDto;
    if(!rsEvent.isPresent()) {
      throw new RuntimeException("rsevent is not exist");
    }
    if(existTrade.isPresent()) {
      tradeDto = existTrade.get();
      if(tradeDto.getAmount() < trade.getAmount()) {
        tradeDto.setRsEvent(rsEvent.get());
        tradeDto.setAmount(trade.getAmount());
      } else {
        throw new AmountIsLessException("amount is less");
      }
    } else {
      tradeDto = TradeDto.builder().rank(trade.getRank())
              .amount(trade.getAmount()).rsEvent(rsEvent.get()).build();
    }
    deleteTradeIfEventIsExistInTrade(eventId);
    tradeRepository.save(tradeDto);
  }

  private void deleteTradeIfEventIsExistInTrade(int eventId) {
    Optional<TradeDto> hasExistEventInTrade = tradeRepository.findByRsEventId(eventId);
    hasExistEventInTrade.ifPresent(tradeRepository::delete);
  }
}
