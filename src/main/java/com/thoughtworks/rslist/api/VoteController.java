package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VoteController {
  @Autowired VoteRepository voteRepository;
  @Autowired
  VoteService voteService;
  @GetMapping("/voteRecord")
  public ResponseEntity<List<Vote>> getVoteRecord(
      @RequestParam int userId, @RequestParam int rsEventId, @RequestParam int pageIndex) {
    List<Vote> votes = voteService.getVoteRecord(userId, rsEventId, pageIndex);
    return ResponseEntity.ok(votes);
  }
}
