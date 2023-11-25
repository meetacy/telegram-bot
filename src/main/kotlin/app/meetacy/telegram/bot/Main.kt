package app.meetacy.telegram.bot

import app.meetacy.sdk.MeetacyApi
import app.meetacy.sdk.production
import app.meetacy.sdk.types.auth.telegram.SecretTelegramBotKey
import app.meetacy.sdk.types.auth.telegram.TemporalTelegramHash
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.utils.shortcuts.sentMessages
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.longPolling
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.URLInlineKeyboardButton
import dev.inmo.tgbotapi.types.message.abstracts.PrivateContentMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

suspend fun main(): Unit = coroutineScope {
    val scope = this

    val token = System.getenv("BOT_TOKEN")
    val secretBotKey = System.getenv("SECRET_KEY").let(::SecretTelegramBotKey)
    val secretBotKeySize = 64

    val bot = telegramBot(token)
    val meetacy = MeetacyApi.production()

    bot.longPolling {
        messagesFlow.mapNotNull { update ->
            update.data as? PrivateContentMessage<*>
        }.onEach { message ->
            val content = message.content

            if (content !is TextContent) {
                bot.sendMessage(
                    chat = message.chat,
                    text = "Please enter a command, I don't know what to reply..."
                )
                return@onEach
            }

            if (!content.text.startsWith("/start")) {
                bot.sendMessage(
                    chat = message.chat,
                    text = "Unknown command, please try again..."
                )
                return@onEach
            }

            val commandParts = content.text.split(" ")

            suspend fun sendStartMessage() = bot.sendMessage(
                chat = message.chat,
                text = "Welcome to Meetacy Service Bot that helps with authorization by Telegram. " +
                        "For now there is no other functions. To follow up with our news, subscribe to our channel ⬇\uFE0F⬇\uFE0F⬇\uFE0F",
                replyMarkup = inlineKeyboard {
                    +URLInlineKeyboardButton(
                        text = "@meetacy",
                        url = "https://t.me/meetacy"
                    )
                }
            )

            if (commandParts.size != 2) {
                sendStartMessage()
                return@onEach
            }

            val (_, temporalHash) = commandParts

            if (temporalHash.length != secretBotKeySize) {
                sendStartMessage()
                return@onEach
            }

            scope.launch {
                meetacy.auth.telegram.finish(
                    temporalHash = TemporalTelegramHash(temporalHash),
                    secretBotKey = secretBotKey,
                    telegramId = message.from.id.chatId,
                    username = message.from.username?.username,
                    firstName = message.from.firstName,
                    lastName = message.from.lastName
                )
            }

            bot.sendMessage(
                chat = message.chat,
                text = "You have successfully authorized using your Telegram account, you can now return to the app. To follow up with our news, subscribe to our channel ⬇\uFE0F⬇\uFE0F⬇\uFE0F",
                replyMarkup = inlineKeyboard {
                    +URLInlineKeyboardButton(
                        text = "@meetacy",
                        url = "https://t.me/meetacy"
                    )
                }
            )
        }.launchIn(scope)
    }
}
