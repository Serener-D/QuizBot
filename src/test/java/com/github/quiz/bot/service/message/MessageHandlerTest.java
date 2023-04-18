package com.github.quiz.bot.service.message;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.entity.FlashCard;
import com.github.quiz.bot.service.ConversationStateHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class MessageHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConversationStateHolder conversationStateHolder;
    @Mock
    private FlashCardDao flashCardDao;

    private MessageHandler messageHandler;
    private Update update;

    @BeforeEach
    void setUp() {
        this.messageHandler = new MessageHandler(conversationStateHolder, flashCardDao);
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        message.setText("Message text");
        update.setMessage(message);
        this.update = update;
    }

    @Test
    void shouldSaveCardQuestionToContext() {
        Long chatId = update.getMessage().getChatId();
        Mockito.when(conversationStateHolder.getState(chatId)).thenReturn(null);
        Response response = messageHandler.handle(update);
        Mockito.verify(conversationStateHolder).putState(chatId, ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER);
        Mockito.verify(conversationStateHolder).putCardQueue(eq(chatId), any());
        assertEquals("Enter card answer", response.getMessage());
    }

    @Test
    void shouldSaveCardAnswerToContext() {
        Long chatId = update.getMessage().getChatId();
        Mockito.when(conversationStateHolder.getState(chatId)).thenReturn(ConversationStateHolder.ConversationState.WAITING_CARD_ANSWER);
        Queue<FlashCard> queue = new LinkedList<>();
        FlashCard flashCard = FlashCard.builder().build();
        queue.add(flashCard);
        Mockito.when(conversationStateHolder.getCardQueue(chatId)).thenReturn(queue);

        Response response = messageHandler.handle(update);
        Mockito.verify(conversationStateHolder).putState(chatId, ConversationStateHolder.ConversationState.WAITING_CATEGORY);
        assertEquals("Message text", flashCard.getAnswer());
        assertEquals("Enter category", response.getMessage());
    }

    @Test
    void shouldSaveCardAnswerToDatabase() {
        Long chatId = update.getMessage().getChatId();
        FlashCard flashCard = new FlashCard();
        Mockito.when(conversationStateHolder.getCardQueue(chatId).poll()).thenReturn(flashCard);
        Mockito.when(conversationStateHolder.getState(chatId)).thenReturn(ConversationStateHolder.ConversationState.WAITING_CATEGORY);
        messageHandler.handle(update);
        Mockito.verify(flashCardDao).save(flashCard);
    }

}