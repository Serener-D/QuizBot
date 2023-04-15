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

    public Response handle(Command command, Long chatId) {
        return switch (command) {
            case GET_CARDS -> getCards(chatId);
            case RANDOM_QUIZ -> startRandomQuiz(chatId);
            case STOP -> stop(chatId);
            case QUIZ -> startCategoryQuiz(chatId);
        };
    }

    private Response getCards(Long chatId) {
        List<FlashCard> cards = FlashCardDao.getAllByChatId(chatId);
        InlineKeyboardMarkup keyboard = keyboardCreator.createAllCardsKeyboard(cards, chatId);
        return Response.builder()
                .message("Saved cards:")
                .replyMarkup(keyboard)
                .build();
    }

    private Response startRandomQuiz(Long chatId) {
        List<FlashCard> cards = FlashCardDao.getLeastUsedByChatId(chatId, Response.QUIZ_SIZE);
        Collections.shuffle(cards);
        return Response.createQuizResponse(chatId, cards, keyboardCreator);
    }

    private Response startCategoryQuiz(Long chatId) {
        List<String> categories = FlashCardDao.getAllCategoriesByChatId(chatId);
        InlineKeyboardMarkup keyboard = keyboardCreator.createCategoryKeyboard(categories);
        return Response.builder()
                .message("Select category:")
                .replyMarkup(keyboard)
                .build();
    }

    private Response stop(Long chatId) {
        conversationStateHolder.clearState(chatId);
        conversationStateHolder.clearCardQueue(chatId);
        return Response.builder()
                .message("Quiz stopped.")
                .build();
    }

}
