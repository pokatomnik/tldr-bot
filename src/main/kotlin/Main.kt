import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import services.TLDRService
import kotlin.system.exitProcess

fun toCommands(rawText: String, maxCommands: Int = 10): Collection<String> {
    return rawText
        .lowercase()
        .trim()
        .split(Regex("\\s+")).apply {
            slice(0 until Integer.min(maxCommands, size))
        }
}

fun generateButtons(commands: Collection<String>): List<InlineKeyboardButton> {
    return commands.fold(mutableListOf<InlineKeyboardButton>()) { list, item ->
        list.apply {
            val button = InlineKeyboardButton.CallbackData(
                text = item,
                callbackData = item
            )
            add(button)
        }
    }.toList()
}

fun main() {
    val botToken = System.getenv("BOT_TOKEN")
    if (botToken == null) {
        println("Bot token not provided.")
        println("Make sure BOT_TOKEN env exists")
        exitProcess(1)
    }
    val tldrService = TLDRService()
    val scope = CoroutineScope(Dispatchers.IO + Job())
    val bot = bot {
        token = botToken
        timeout = 30

        dispatch {
            command("start") {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hi:) This is a TLDR bot. Type a linux/windows/osX command to get help."
                )
            }

            text {
                if (message.text?.startsWith("/") == true) {
                    return@text
                }

                val commands = toCommands(message.text ?: "")
                scope.launch {
                    when (commands.size) {
                        0 -> bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "Specify command, please",
                            replyToMessageId = message.messageId
                        )
                        1 -> message.text?.let { text ->
                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = tldrService.fetchTLDR(text) ?: "Not found",
                                replyToMessageId = message.messageId,
                                parseMode = ParseMode.MARKDOWN
                            )
                        }
                        else -> bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "Choose one:",
                            replyToMessageId = message.messageId,
                            replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(
                                generateButtons(commands)
                            )
                        )
                    }
                }
            }

            callbackQuery {
                callbackQuery.message?.chat?.id?.let { chatId ->
                    scope.launch {
                        val responseFound = tldrService.fetchTLDR(callbackQuery.data) ?: "Not found"
                        bot.sendMessage(
                            chatId = ChatId.fromId(chatId),
                            text = responseFound,
                            parseMode = ParseMode.MARKDOWN
                        )
                        callbackQuery.message?.messageId?.let { messageId ->
                            bot.deleteMessage(
                                chatId = ChatId.fromId(chatId),
                                messageId = messageId
                            )
                        }
                    }
                }
            }
        }
    }

    bot.startPolling()
}