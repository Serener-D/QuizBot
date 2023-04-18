package com.github.quiz.bot.dto;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.KeyboardCreator;
import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

@Data
@Builder
public class Response {

    public static final int QUIZ_SIZE = 10;
    public static final String NO_CARDS_SAVED_MESSAGE = "You don't have any cards saved";

    private String message;
    private ReplyKeyboard replyMarkup;

    public static Response createNoCardsSavedResponse() {
        return Response.builder().message("NO_CARDS_SAVED_MESSAGE").build();
    }

    public static Response createQuizResponse(List<FlashCard> cards,
                                              KeyboardCreator keyboardCreator,
                                              FlashCardDao flashCardDao) {
        String text;
        Response.ResponseBuilder responseBuilder = Response.builder();
        if (!cards.isEmpty()) {
            FlashCard card = cards.get(0);
            flashCardDao.incrementShowedCounter(card);

            text = "Question:\n" + card.getQuestion();
            InlineKeyboardMarkup keyboardMarkup = keyboardCreator.createNextCardKeyboard();
            responseBuilder.replyMarkup(keyboardMarkup);
        } else {
            text = NO_CARDS_SAVED_MESSAGE;
        }
        return responseBuilder.message(text).build();
    }

}
