package com.thoughtworks.rslist.service;

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

import java.util.Optional;

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
