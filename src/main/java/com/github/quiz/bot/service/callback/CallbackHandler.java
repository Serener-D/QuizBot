package com.github.quiz.bot.service.callback;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.ConversationStateHolder;
import com.github.quiz.bot.service.KeyboardCreator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@RequiredArgsConstructor
public class CallbackHandler {

    private final ConversationStateHolder conversationStateHolder;
    private final KeyboardCreator keyboardCreator;
    private final FlashCardDao flashCardDao;

    public Response handle(Callback callback, Long chatId, @Nullable String arguments) {
        return switch (callback) {
            case GET -> get(Optional.ofNullable(arguments).map(Long::parseLong).orElse(null));
            case NEXT_PAGE -> printPage(chatId);
            case NEXT_CARD -> printNextCard(chatId);
            case DELETE -> delete(Optional.ofNullable(arguments).map(Long::parseLong).orElse(null));
            case CATEGORY -> startCategoryQuiz(chatId, arguments);
        };
    }

    private Response get(Long cardId) {
        FlashCard flashCard = flashCardDao.get(cardId);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text("Delete").callbackData(Callback.DELETE + " " + cardId).build();
        List<InlineKeyboardButton> buttonRow = List.of(deleteButton);
        keyboard.add(buttonRow);
        keyboardMarkup.setKeyboard(keyboard);

        return Response.builder()
                .message("Question:\n" + flashCard.getQuestion() + "\n\nAnswer:\n" + flashCard.getAnswer() + "\n\nCategory:\n" + flashCard.getCategory())
                .replyMarkup(keyboardMarkup)
                .build();
    }

    private Response delete(Long cardId) {
        flashCardDao.delete(cardId);
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

    private Response printNextCard(Long chatId) {
        Queue<FlashCard> cardsQueue = conversationStateHolder.getCardQueue(chatId);
        FlashCard previousCard = cardsQueue.poll();

        String text = "Answer:\n";
        if (previousCard != null) {
            flashCardDao.incrementShowedCounter(previousCard);
            text += previousCard.getAnswer();
        }

        Response.ResponseBuilder responseBuilder = Response.builder();
        if (!cardsQueue.isEmpty() && cardsQueue instanceof LinkedList<FlashCard> cardsList) {
            FlashCard nextCard = cardsQueue.element();
            text += "\n\nNext question:\n" + nextCard.getQuestion();
            InlineKeyboardMarkup keyboardMarkup = keyboardCreator.createNextCardKeyboard();
            responseBuilder.replyMarkup(keyboardMarkup);
            conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.TAKING_QUIZ);
            conversationStateHolder.putCardQueue(chatId, new LinkedList<>(cardsList));
        } else {
            conversationStateHolder.clearState(chatId);
            conversationStateHolder.clearCardQueue(chatId);
            text += "\n\nEnd of quiz.";
        }
        return responseBuilder.message(text).build();
    }

    private Response startCategoryQuiz(Long chatId, String category) {
        List<FlashCard> cards = flashCardDao.getLeastUsedByChatIdAndCategory(chatId, category, Response.QUIZ_SIZE);
        Collections.shuffle(cards);
        conversationStateHolder.putState(chatId, ConversationStateHolder.ConversationState.TAKING_QUIZ);
        conversationStateHolder.putCardQueue(chatId, new LinkedList<>(cards));
        return Response.createQuizResponse(cards, keyboardCreator, flashCardDao);
    }

}
