package com.github.quiz.bot.service.callback;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class CallbackHandler {

    public Response handle(Callback callback, Long chatId, String arguments) {
        return switch (callback) {
            case GET -> get(Long.parseLong(arguments));
            default -> Response.builder().message("Unknown callback").build();
        };
    }

    private Response get(Long cardId) {
        FlashCard flashCard = FlashCardDao.get(cardId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text("Delete").callbackData(Callback.DELETE + " " + cardId).build();
        List<InlineKeyboardButton> buttonRow = List.of(deleteButton);
        keyboard.add(buttonRow);
        keyboardMarkup.setKeyboard(keyboard);

        return Response.builder()
                .message("Question: " + flashCard.getQuestion() + "\nAnswer: " + flashCard.getAnswer() + "\nCategory: " + flashCard.getCategory())
                .replyMarkup(keyboardMarkup)
                .build();

    }

}
