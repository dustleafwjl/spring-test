package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.exception.AmountIsLessException;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.service.RsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
public class RsController {
  @Autowired RsService rsService;

  @GetMapping("/rs/list")
  public ResponseEntity<List<RsEvent>> getRsEventListBetween(
      @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {

    List<RsEvent> rsEvents = rsService.getRsEventsBetween(start, end);
    if (start == null || end == null) {
      return ResponseEntity.ok(rsEvents);
    }
    return ResponseEntity.ok(rsEvents.subList(start - 1, end));
  }

  @GetMapping("/rs/{index}")
  public ResponseEntity<RsEvent> getRsEvent(@PathVariable int index) {
    List<RsEvent> rsEvents = rsService.getRsEvents();
    if (index < 1 || index > rsEvents.size()) {
      throw new RequestNotValidException("invalid index");
    }
    return ResponseEntity.ok(rsEvents.get(index - 1));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
    if (!rsService.addRsEvents(rsEvent)) {
      return ResponseEntity.badRequest().build();
    }
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
