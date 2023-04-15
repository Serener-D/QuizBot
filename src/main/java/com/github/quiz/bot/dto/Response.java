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

    private String message;
    private ReplyKeyboard replyMarkup;

    public static Response createQuizResponse(Long chatId,
                                              List<FlashCard> cards,
                                              KeyboardCreator keyboardCreator,
                                              FlashCardDao flashCardDao) {
        String text;
        Response.ResponseBuilder responseBuilder = Response.builder();
        if (!cards.isEmpty()) {
            FlashCard card = cards.get(0);
            card.setShowedCounter(card.getShowedCounter() + 1);
            flashCardDao.update(card);

            text = "Question:\n" + card.getQuestion();
            InlineKeyboardMarkup keyboardMarkup = keyboardCreator.createRandomQuizKeyboard(cards, chatId);
            responseBuilder.replyMarkup(keyboardMarkup);
        } else {
            text = "You don't have any cards saved";
        }
        return responseBuilder.message(text).build();
    }

}
