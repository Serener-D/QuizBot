package com.github.quiz.bot.service.command;

import com.github.quiz.bot.dao.FlashCardDao;
import com.github.quiz.bot.dto.Response;
import com.github.quiz.bot.service.ConversationStateHolder;
import com.github.quiz.bot.service.KeyboardCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

    @Mock
    private ConversationStateHolder conversationStateHolder;
    @Mock
    private KeyboardCreator keyboardCreator;
    @Mock
    private FlashCardDao flashCardDao;

    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        this.commandHandler = new CommandHandler(conversationStateHolder, keyboardCreator, flashCardDao);
    }

    @Test
    void shouldClearState() {
        long chatId = 1L;
        Mockito.when(conversationStateHolder.getState(chatId)).thenReturn(ConversationStateHolder.ConversationState.TAKING_QUIZ);
        Response response = commandHandler.handle(Command.STOP, chatId);
        assertEquals("Quiz stopped.", response.getMessage());
    }

    @Test
    void shouldNotClearState() {
        long chatId = 1L;
        Response response = commandHandler.handle(Command.STOP, chatId);
        assertEquals("No active quiz.", response.getMessage());
    }

}