package com.mahi.ai

import com.mahi.ai.command.ActionValidator
import com.mahi.ai.command.AssistantAction
import com.mahi.ai.command.CommandParser
import org.junit.Assert.*
import org.junit.Test

class CommandParserTest {
    @Test fun openApp() =
        assertEquals(AssistantAction.OpenApp("youtube"), CommandParser.parse("Hey Mahi, open YouTube."))

    @Test fun youtubeSearch() =
        assertEquals(AssistantAction.YouTubeSearch("relaxing music"), CommandParser.parse("Mahi, search for relaxing music"))

    @Test fun play() =
        assertEquals(AssistantAction.YouTubeSearch("bangla songs"), CommandParser.parse("play bangla songs"))

    @Test fun settings() =
        assertEquals(AssistantAction.OpenSettings, CommandParser.parse("open android settings"))

    @Test fun banglaOpen() =
        assertEquals(AssistantAction.OpenApp("ইউটিউব"), CommandParser.parse("ইউটিউব খোল"))

    @Test fun normalChatIsNotACommand() = assertNull(CommandParser.parse("how are you today?"))

    @Test fun validatorRejectsOversized() =
        assertFalse(ActionValidator.isAllowed(AssistantAction.OpenApp("x".repeat(100))))
}
