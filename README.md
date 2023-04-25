## QuizBot

This is a telegram bot I wrote when preparing for OCP 17. It saves questions and answers to them, which can be later asked in a form of quiz. It might be helpful when preparing for some exam.

#### My deployed bot: 
https://t.me/learn_quiz_bot

#### How to deploy:
1. Create your bot with [BotFather](https://t.me/BotFather) and define 4 commands: <code>/get_card</code>, <code>/random_quiz</code>, <code>/quiz</code>, <code>/stop</code>. You should also remember your bot's token received from BotFather, you will need it in step 3
2. Compile the project with maven. I've included maven-assembly-plugin, so you can execute <code>mvn clean compile assembly:single</code>
3. To start the bot execute <code>java -jar quiz.jar [BOT_TOKEN]</code> command. Insert your token from step 1 instead of [BOT_TOKEN]

#### How to use when deployed:
- User types a message. a dialog with bot is initiated, where bot asks user to provide details for the future flashcard. When bot gets all the necessary information, the card is saved to the Database
- <b>/get_card command</b> - prints all existing user's flashcards. If user has > 20 cards, the pagination occurs. When user selects one the printed cards, more detailed information is printed about the selected card and a button to delete it
- <b>/random_quiz command</b> - starts quiz with random category (quiz size is limited to 10 flashcards)
- <b>/quiz command</b> - when user selects the category based on a previously saved cards, bot starts quiz (quiz size is limited to 10 flashcards)
- <b>/stop command</b> - oh sh*t button. Stops ongoing quiz (and clears the conversation state)

#### TODO Features:
1. <b>/set_quiz_size</b> command to step away from hardcoded size of 10 flashcards per quiz
2. <b>/import</b> command to mass import cards as a .csv file

#### Known bugs:
- Making too long question/answer, so that telegram splits it in two messages