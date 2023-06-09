package com.github.quiz.bot.service;

import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.callback.Callback;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KeyboardCreator {

    private final ConversationStateHolder conversationStateHolder;

    private static final int PAGINATION_THRESHOLD = 20;

    public InlineKeyboardMarkup createAllCardsKeyboard(List<FlashCard> cards, Long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = 0; i < cards.size() && i < PAGINATION_THRESHOLD; i++) {
            FlashCard card = cards.get(i);
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(card.getQuestion())
                    .callbackData(Callback.GET + " " + card.getId())
                    .build();
            List<InlineKeyboardButton> buttonRow = List.of(button);
            keyboard.add(buttonRow);
        }
        if (cards.size() > PAGINATION_THRESHOLD) {
            paginate(keyboard, cards);
        } else {
            conversationStateHolder.clearState(chatId);
            conversationStateHolder.clearCardQueue(chatId);
        }
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup createCategoriesKeyboard(List<String> categories) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String category : categories) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(category)
                    .callbackData(Callback.CATEGORY + " " + category)
                    .build();
            List<InlineKeyboardButton> buttonRow = List.of(button);
            keyboard.add(buttonRow);
        }
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup createNextCardKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Next")
                .callbackData(Callback.NEXT_CARD.name())
                .build();
        List<InlineKeyboardButton> buttonRow = List.of(button);
        keyboard.add(buttonRow);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void paginate(List<List<InlineKeyboardButton>> keyboard, List<FlashCard> cards) {
        Long chatId = cards.get(0).getChatId();
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("> next page")
                 .callbackData(Callback.NEXT_PAGE.name())
                .build();
        List<InlineKeyboardButton> buttonRow = List.of(button);
        keyboard.add(buttonRow);
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.RECEIVING_CARDS);
        conversationStateHolder.putCardQueue(chatId, cards.stream().skip(PAGINATION_THRESHOLD).collect(Collectors.toCollection(LinkedList::new)));
    }

}
