package com.example.tom.HushText;
import java.util.Comparator;
/**
 * Created by Tom on 15-02-15.
 * Message comparator is comparator for Message class where the messages are compared by date.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 */
public class MessageComparator implements Comparator<Message> {
    @Override
    public int compare(Message a, Message b) {
        if (a.getDate() > b.getDate())
            return 1;
        else if (a.getDate() == b.getDate())
            return 0;
        else
            return -1;
    }
}
