package com.github.quiz.bot.service.command;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.KeyboardCreator;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@RequiredArgsConstructor
public class CommandHandler {

    private final KeyboardCreator keyboardCreator;

    public Response handle(Command command, Long chatId) {
        return switch (command) {
            case ALL -> getAll(chatId);
            default -> Response.builder().message("Unknown command").build();
        };
    }

    private Response getAll(Long chatId) {
        List<FlashCard> cards = FlashCardDao.getAllByChatId(chatId);
        InlineKeyboardMarkup keyboard = keyboardCreator.createAllCardsKeyboard(cards, chatId);
        return Response.builder()
                .message("Saved cards:")
                .replyMarkup(keyboard)
                .build();
    }

}
