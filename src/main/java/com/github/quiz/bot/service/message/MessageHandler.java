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

    private static final String DEFAULT_RESPONSE_MESSAGE = "Something went wrong";

    private final ConversationStateHolder conversationStateHolder;
    private final FlashCardDao flashCardDao;

    @SneakyThrows
    public Response handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String responseMessage = DEFAULT_RESPONSE_MESSAGE;
        if (isNewCardReceived(update)) {
            responseMessage = saveCardAnswerQuestionToContext(update);
        } else if (isWaitingAnswerNewCard(chatId)) {
            responseMessage = saveCardAnswerToContext(update);
        } else if (isWaitingCategoryForNewCard(chatId)) {
            responseMessage = saveCardToDatabase(update);
        }
        return Response.builder().message(responseMessage).build();
    }

    private String saveCardAnswerQuestionToContext(Update update) {
        Long chatId = update.getMessage().getChatId();
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER);
        Queue<FlashCard> flashCardQueue = new LinkedList<>();
        flashCardQueue.add(FlashCard.builder().chatId(chatId).question(update.getMessage().getText()).build());
        conversationStateHolder.putCardQueue(chatId, flashCardQueue);
        return "Enter card answer";
    }

    private String saveCardAnswerToContext(Update update) {
        Long chatId = update.getMessage().getChatId();
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.WAITING_CATEGORY);
        conversationStateHolder.getCardQueue(chatId).element().setAnswer(update.getMessage().getText());
        return "Enter category";
    }

    private String saveCardToDatabase(Update update) {
        Long chatId = update.getMessage().getChatId();
        String responseMessage;
        FlashCard flashCard = conversationStateHolder.getCardQueue(chatId).poll();
        if (flashCard != null) {
            flashCard.setCategory(update.getMessage().getText());
            flashCardDao.save(flashCard);
            responseMessage = "FlashCard saved";
        } else {
            responseMessage = DEFAULT_RESPONSE_MESSAGE;
        }
        conversationStateHolder.clearState(chatId);
        conversationStateHolder.clearCardQueue(chatId);
        return responseMessage;
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
