package com.github.quiz.bot.service.command;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.ConversationStateHolder;
import com.github.quiz.bot.service.KeyboardCreator;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CommandHandler {

    private final ConversationStateHolder conversationStateHolder;
    private final KeyboardCreator keyboardCreator;
    private final FlashCardDao flashCardDao;

    public Response handle(Command command, Long chatId) {
        return switch (command) {
            case GET_CARDS -> getCards(chatId);
            case RANDOM_QUIZ -> startRandomQuiz(chatId);
            case STOP -> stop(chatId);
            case QUIZ -> startCategoryQuiz(chatId);
        };
    }

    private Response getCards(Long chatId) {
        List<FlashCard> cards = flashCardDao.getAllByChatId(chatId);
        if (cards.isEmpty()) {
            return Response.createNoCardsSavedResponse();
        }
        InlineKeyboardMarkup keyboard = keyboardCreator.createAllCardsKeyboard(cards, chatId);
        return Response.builder()
                .message("Saved cards:")
                .replyMarkup(keyboard)
                .build();
    }

    private Response startRandomQuiz(Long chatId) {
        List<FlashCard> cards = flashCardDao.getLeastUsedByChatId(chatId, Response.QUIZ_SIZE);
        if (cards.isEmpty()) {
            return Response.createNoCardsSavedResponse();
        }
        Collections.shuffle(cards);
        return Response.createQuizResponse(chatId, cards, keyboardCreator, flashCardDao);
    }

    private Response startCategoryQuiz(Long chatId) {
        List<String> categories = flashCardDao.getAllCategoriesByChatId(chatId);
        if (categories.isEmpty()) {
            return Response.createNoCardsSavedResponse();
        }
        InlineKeyboardMarkup keyboard = keyboardCreator.createCategoryKeyboard(categories);
        return Response.builder()
                .message("Select category:")
                .replyMarkup(keyboard)
                .build();
    }

    private Response stop(Long chatId) {
        String message;
        if (conversationStateHolder.getState(chatId) != null || conversationStateHolder.getCardQueue(chatId) != null) {
            conversationStateHolder.clearState(chatId);
            conversationStateHolder.clearCardQueue(chatId);
            message = "Quiz stopped.";
        } else {
            message = "No active quiz.";
        }
        return Response.builder()
                .message(message)
                .build();
    }

}
