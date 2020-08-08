package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.exception.AmountIsLessException;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.service.RsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@Validated
public class RsController {
  @Autowired RsEventRepository rsEventRepository;
  @Autowired UserRepository userRepository;
  @Autowired
  TradeRepository tradeRepository;
  @Autowired RsService rsService;

  @GetMapping("/rs/list")
  public ResponseEntity<List<RsEvent>> getRsEventListBetween(
      @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {
    List<TradeDto> tradeDtos = tradeRepository.findAll().stream()
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
    List<RsEvent> tradeEvents = getRsEventsWithTrade(tradeDtos);
    List<RsEvent> rsEvents = getRsEvents();

    for (int index = 0; index < tradeDtos.size(); index ++) {
      int rankIndex = tradeDtos.get(index).getRank() - 1;
      if(start != null) {
        rankIndex += start - 1;
      }
      rsEvents.add(rankIndex, tradeEvents.get(index));
    }
    rsEvents = rsEvents.stream().filter(distinctByKey(RsEvent::getEventName)).collect(Collectors.toList());


    if (start == null || end == null) {
      return ResponseEntity.ok(rsEvents);
    }
    return ResponseEntity.ok(rsEvents.subList(start - 1, end));
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

  private List<RsEvent> getRsEvents() {
    return rsEventRepository.findAll().stream()
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

  @GetMapping("/rs/{index}")
  public ResponseEntity<RsEvent> getRsEvent(@PathVariable int index) {
    List<RsEvent> rsEvents =
        rsEventRepository.findAll().stream()
            .map(
                item ->
                    RsEvent.builder()
                        .eventName(item.getEventName())
                        .keyword(item.getKeyword())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .build())
            .collect(Collectors.toList());
    if (index < 1 || index > rsEvents.size()) {
      throw new RequestNotValidException("invalid index");
    }
    return ResponseEntity.ok(rsEvents.get(index - 1));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
    Optional<UserDto> userDto = userRepository.findById(rsEvent.getUserId());
    if (!userDto.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    RsEventDto build =
        RsEventDto.builder()
            .keyword(rsEvent.getKeyword())
            .eventName(rsEvent.getEventName())
            .voteNum(0)
            .user(userDto.get())
            .build();
    rsEventRepository.save(build);
    return ResponseEntity.created(null).build();
  }

  @PostMapping("/rs/vote/{id}")
  public ResponseEntity vote(@PathVariable int id, @RequestBody Vote vote) {
    rsService.vote(vote, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/rs/buy/{id}")
  public ResponseEntity buy(@PathVariable int id, @RequestBody Trade trade){
    rsService.buy(trade, id);
    return ResponseEntity.ok().build();
  }


  @ExceptionHandler({RequestNotValidException.class})
  public ResponseEntity<Error> handleRequestErrorHandler(RequestNotValidException e) {
    Error error = new Error();
    error.setError(e.getMessage());
    return ResponseEntity.badRequest().body(error);
  }
  @ExceptionHandler({AmountIsLessException.class})
  public ResponseEntity<Error> handleAmountErrorHandler(AmountIsLessException e) {
    Error error = new Error();
    error.setError(e.getMessage());
    return ResponseEntity.badRequest().body(error);
  }
}
