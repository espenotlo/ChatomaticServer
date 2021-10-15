package norseninja.logic;

import java.time.LocalTime;

public class Message implements Comparable<Message> {
    private final String fromUser;
    private final String toUser;
    private final String messageText;
    private LocalTime timeStamp;

    /**
     * Creates a new message.
     * @param fromUser username of the sender
     * @param toUser username of the recipient
     * @param messageText message text
     */
    public Message(String fromUser, String toUser, String messageText) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.messageText = messageText;
        this.timeStamp = LocalTime.now();
    }

    public String getFromUser() {
        return this.fromUser;
    }

    public String getToUser() {
        return this.toUser;
    }

    public String getMessageText() {
        return this.messageText;
    }

    public LocalTime getTimeStamp() {
        return this.timeStamp;
    }

    public void incrementTimeStamp() {
        this.timeStamp = this.timeStamp.plusSeconds(1);
    }

    @Override
    public int compareTo(Message message) {
        return this.timeStamp.compareTo(message.timeStamp);
    }
}
