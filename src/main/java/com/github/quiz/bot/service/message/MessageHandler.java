package com.github.quiz.bot.service.message;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.ConversationStateHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedList;
import java.util.Queue;

@RequiredArgsConstructor
public class MessageHandler {

    private static final String DEFAULT_RESPONSE = "Something went wrong";

    private final ConversationStateHolder conversationStateHolder;
    private final FlashCardDao flashCardDao;

    @SneakyThrows
    public Response handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String message = DEFAULT_RESPONSE;
        if (isNewCardReceived(update)) {
            saveCardAnswerQuestionToContext(update);
            message = "Enter card answer";
        } else if (isWaitingAnswerNewCard(chatId)) {
            saveCardAnswerToContext(update);
            message = "Enter category";
        } else if (isWaitingCategoryForNewCard(chatId)) {
            saveCardToDatabase(update);
            message = "FlashCard saved";
        }
        return Response.builder().message(message).build();
    }

    private void saveCardAnswerQuestionToContext(Update update) {
        Long chatId = update.getMessage().getChatId();
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER);
        Queue<FlashCard> flashCardQueue = new LinkedList<>();
        flashCardQueue.add(FlashCard.builder().chatId(chatId).question(update.getMessage().getText()).build());
        conversationStateHolder.putCardQueue(chatId, flashCardQueue);
    }

    private void saveCardAnswerToContext(Update update) {
        Long chatId = update.getMessage().getChatId();
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.WAITING_CATEGORY);
        conversationStateHolder.getCardQueue(chatId).element().setAnswer(update.getMessage().getText());
    }

    private void saveCardToDatabase(Update update) {
        Long chatId = update.getMessage().getChatId();
        FlashCard flashCard = conversationStateHolder.getCardQueue(chatId).poll();
        flashCard.setCategory(update.getMessage().getText());
        flashCardDao.save(flashCard);
        conversationStateHolder.clearState(chatId);
        conversationStateHolder.clearCardQueue(chatId);
    }

    private boolean isNewCardReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        return conversationStateHolder.getState(chatId) == null
                || conversationStateHolder.getState(chatId) == ConversationStateHolder.ConversationState.IDLE;
    }

    private boolean isWaitingAnswerNewCard(Long chatId) {
        return conversationStateHolder.getState(chatId) == ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER;
    }

    private boolean isWaitingCategoryForNewCard(Long chatId) {
        return conversationStateHolder.getState(chatId) == ConversationStateHolder.ConversationState.WAITING_CATEGORY;
    }

}
