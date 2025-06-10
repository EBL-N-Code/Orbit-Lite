package org.skyblockagent.chat.message;

import net.minecraft.client.gui.DrawContext;
import org.skyblockagent.chat.text.TextLine;
import org.skyblockagent.chat.text.TextParser;
import org.skyblockagent.gui.ChatDisplay;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.network.RunInfo;
import org.skyblockagent.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class AIMessage extends ChatMessage {
    private final TextParser textParser = new TextParser();
    private RunInfo runInfo;
    private int errMsgWidth;

    public AIMessage() {
        super(new ArrayList<>(List.of("")));
    }

    public AIMessage(String content, long timestamp) {
        super(content, timestamp);
    }

    @Override
    public void setWidth(int displayWidth) {
        width = error ? errMsgWidth : displayWidth;
        textParser.setMaxWidth(width);
        wrapLines(width);
    }

    public void render(DrawContext context) {
//        if (error) RenderUtils.drawRect(context, x, y, width, height - 16, 8, 0x40FF5555, 1f, 0xFFFF5555);
//        int textColor = error ? 0xFFFF5555 : RenderUtils.WHITE_COLOR;
//        int y = error ? this.y + 4 : this.y;
//        int x = error ? this.x + 6 : this.x;
//        for (String line : lines) {
//            RenderUtils.drawText(context, line, x, y, textColor);
//            y += 10;
//        }
        textParser.render(context, x, y);
    }

    public void add(String msg) {
        content.set(content.size() - 1, content.getLast() + msg);
        textParser.processChunk(msg);

        int oldHeight = height;
        wrapLines(width);
        setHeight();

        ChatScreen chatScreen = ChatScreen.getInstance();
        if (chatScreen != null) {
            ChatDisplay chatDisplay = chatScreen.getChatDisplay();
            int diff = height - oldHeight;
            if (diff > 0) {
                chatDisplay.addSpace(diff);
            }
        }
    }

    public void onError(String error) {
        this.error = true;
        errMsgWidth = TEXT_RENDERER.getWidth(error) + 12;
        width = errMsgWidth;
        add(error);
    }

    public void onFinish() {
        toolbar = new Toolbar(this);
        for (TextLine line : textParser.getLines()) {
            System.out.println(line.getRawText());
        }
    }

    @Override
    public Sender getSender() {
        return Sender.AI;
    }

    public RunInfo getRunInfo() {
        return runInfo;
    }

    public void setRunInfo(RunInfo runInfo) {
        this.runInfo = runInfo;
    }
}