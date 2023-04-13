package com.github.quiz.bot.service.callback;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.ConversationStateHolder;
import com.github.quiz.bot.service.KeyboardCreator;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CallbackHandler {

    private final ConversationStateHolder conversationStateHolder;
    private final KeyboardCreator keyboardCreator;

    public Response handle(Callback callback, Long chatId, String arguments) {
        return switch (callback) {
            case GET -> get(Long.parseLong(arguments));
            case NEXT_PAGE -> printPage(chatId);
            case DELETE -> delete(Long.parseLong(arguments));
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

    private Response delete(Long cardId) {
        FlashCardDao.delete(cardId);
        return Response.builder()
                .message("Card deleted")
                .build();
    }

    private Response printPage(Long chatId) {
        List<FlashCard> cards = new ArrayList<>(conversationStateHolder.getCardQueue(chatId));
        InlineKeyboardMarkup keyboard = keyboardCreator.createAllCardsKeyboard(cards, chatId);
        return Response.builder()
                .message("Saved cards:")
                .replyMarkup(keyboard)
                .build();
    }

}
