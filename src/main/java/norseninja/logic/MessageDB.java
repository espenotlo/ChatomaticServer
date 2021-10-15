package norseninja.logic;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MessageDB {
    private final HashMap<LocalTime, Message> messages;

    public MessageDB() {
        this.messages = new HashMap<>();
    }

    public List<Message> getMessages() {
        return this.messages.values().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    public boolean addMessage(Message message) {
        while (true) {
            if (!this.messages.containsKey(message.getTimeStamp())) {
                this.messages.put(message.getTimeStamp(), message);
                return true;
            } else {
                message.incrementTimeStamp();
            }
        }
    }

    public List<Message> getAllToOrFromUser(User user) {
        List<Message> returnList = this.messages.values().stream()
                .filter(message -> message.getToUser().equals(user.getUsername()) || message.getToUser().equalsIgnoreCase("all") || message.getFromUser().equals(user.getUsername()))
                .collect(Collectors.toList());
        return returnList.stream().sorted().collect(Collectors.toList());
    }

    public List<Message> getUndelivered(User user, LocalTime lastDelivered) {
        List<Message> returnList = getAllToOrFromUser(user).stream()
                .filter(message -> lastDelivered.isBefore(message.getTimeStamp()))
                .collect(Collectors.toList());

        return returnList.stream().sorted().collect(Collectors.toList());
    }
}
