package com.github.quiz.bot.service.command;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.callback.Callback;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    public Response handle(Command command, Long chatId) {
        return switch (command) {
            case ALL -> executeAll(chatId);
            default -> Response.builder().message("Unknown command").build();
        };
    }

    private Response executeAll(Long chatId) {
        List<FlashCard> cards = FlashCardDao.getAllByChatId(chatId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (FlashCard card : cards) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(card.getQuestion())
                    .callbackData(Callback.GET + " " + card.getId())
                    .build();
            List<InlineKeyboardButton> buttonRow = List.of(button);
            keyboard.add(buttonRow);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return Response.builder()
                .message("Saved cards:")
                .replyMarkup(keyboardMarkup)
                .build();
    }
}
