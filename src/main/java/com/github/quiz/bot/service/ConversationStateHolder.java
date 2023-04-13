package com.github.quiz.bot.service;

import com.github.quiz.bot.entity.FlashCard;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ConversationStateHolder {

    private final Map<Long, ConversationState> conversationStateMap = new HashMap<>();
    private final Map<Long, Queue<FlashCard>> flashCardMap = new HashMap<>();

    public void putState(Long chatId, ConversationState state) {
        conversationStateMap.put(chatId, state);
    }

    public ConversationState getState(Long chatId) {
        return conversationStateMap.get(chatId);
    }

    public void clearState(Long chatId) {
        conversationStateMap.remove(chatId);
    }

    public void putCardQueue(Long chatId, Queue<FlashCard> flashCardQueue) {
        flashCardMap.put(chatId, flashCardQueue);
    }

    public Queue<FlashCard> getCardQueue(Long chatId) {
        return flashCardMap.get(chatId);
    }

    public void clearCardQueue(Long chatId) {
        flashCardMap.remove(chatId);
    }

    public enum ConversationState {IDLE, WAITING_CATEGORY, WAITING_CARD_ANSWER, RECEIVING_CARDS, TAKING_QUIZ}

}
