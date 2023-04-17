package com.github.quiz.bot.service.callback;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.ConversationStateHolder;
import com.github.quiz.bot.service.KeyboardCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CallbackHandlerTest {

    @Mock
    private ConversationStateHolder conversationStateHolder;
    @Mock
    private KeyboardCreator keyboardCreator;
    @Mock
    private FlashCardDao flashCardDao;

    private CallbackHandler callbackHandler;

    @BeforeEach
    void setUp() {
        this.callbackHandler = new CallbackHandler(conversationStateHolder, keyboardCreator, flashCardDao);
    }

    @Test
    void shouldReturnCardContentsByCardIdAndHaveDeleteButton() {
        Long cardId = 1L;
        String question = "What Is the Color Of Night?";
        String answer = "Sanguine, my brother.";
        String category = "DB";
        FlashCard flashCard = FlashCard.builder().question(question).answer(answer).category(category).build();
        Mockito.when(flashCardDao.get(cardId)).thenReturn(flashCard);

        Response response = callbackHandler.handle(Callback.GET, null, cardId.toString());
        String expectedMessage = "Question:\n" + question + "\n\nAnswer:\n" + answer + "\n\nCategory:\n" + category;
        assertEquals(expectedMessage, response.getMessage());

        InlineKeyboardButton expectedDeleteButton = InlineKeyboardButton.builder().text("Delete").callbackData(Callback.DELETE + " " + cardId).build();
        InlineKeyboardMarkup keyboardMarkup = (InlineKeyboardMarkup) response.getReplyMarkup();
        assertEquals(expectedDeleteButton, keyboardMarkup.getKeyboard().get(0).get(0));
    }

    @Test
    void shouldEndQuiz() {
        Long chatId = 1L;
        Queue<FlashCard> flashCardQueue = new LinkedList<>();
        FlashCard flashCard = FlashCard.builder().question("question").answer("answer").category("category").build();
        flashCardQueue.add(flashCard);
        Mockito.when(conversationStateHolder.getCardQueue(chatId)).thenReturn(flashCardQueue);
        Response response = callbackHandler.handle(Callback.NEXT_CARD, chatId, null);
        assertTrue(response.getMessage().contains("End of quiz"));
    }

    @Test
    void shouldContinueQuiz() {
        Long chatId = 1L;
        Queue<FlashCard> flashCardQueue = new LinkedList<>();
        FlashCard flashCard = FlashCard.builder().question("question").answer("answer").category("category").build();
        FlashCard flashCard1 = FlashCard.builder().question("question1").answer("answer1").category("category1").build();
        flashCardQueue.add(flashCard);
        flashCardQueue.add(flashCard1);
        Mockito.when(conversationStateHolder.getCardQueue(chatId)).thenReturn(flashCardQueue);
        Response response = callbackHandler.handle(Callback.NEXT_CARD, chatId, null);
        assertTrue(response.getMessage().contains("Next question"));
    }

    @Test
    void shouldIncrementShowedCounter() {
        Long chatId = 1L;
        Queue<FlashCard> flashCardQueue = new LinkedList<>();
        FlashCard flashCard = FlashCard.builder().question("question").answer("answer").category("category").build();
        flashCardQueue.add(flashCard);
        Mockito.when(conversationStateHolder.getCardQueue(chatId)).thenReturn(flashCardQueue);
        callbackHandler.handle(Callback.NEXT_CARD, chatId, null);
        Mockito.verify(flashCardDao).incrementShowedCounter(flashCard);
    }

}