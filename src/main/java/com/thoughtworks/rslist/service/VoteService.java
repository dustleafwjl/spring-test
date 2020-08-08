package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoteService {
    final VoteRepository voteRepository;

    public VoteService(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }
    public List<Vote> getVoteRecord(@RequestParam int userId, @RequestParam int rsEventId, @RequestParam int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex - 1, 5);
        return voteRepository.findAllByUserIdAndRsEventId(userId, rsEventId, pageable).stream()
                .map(item ->
                            Vote.builder()
                                .voteNum(item.getNum())
                                .userId(item.getUser().getId())
                                .time(item.getLocalDateTime())
                                .rsEventId(item.getRsEvent().getId())
                                .build())
                .collect(Collectors.toList());
    }
}
