package com.example.tom.HushText;
import java.lang.Comparable;
/**
 * Created by Tom on 15-01-23.
 * @author Tom Hadlaw
 * @version 1.0
 * @since 2015-04-01
 * Message objects represents a single SMS message and any (useful) information about it, Message
 * object comparable by date.
 */
public class Message implements Comparable<Message> {
    private String message;
    private long date;
    private String address;
    private boolean isSent;

    /**
     * Constructs message object.
     * @param Message Message contents.
     * @param Date Date of message (in seconds since 1970).
     * @param Address Address (phone number) of message.
     * @param SentRecieved True if message was sent or False if it was recieved (will obviously vary
     *                     depending on what end you're on).
     */
    public Message(String Message, long Date, String Address, boolean SentRecieved) {
        message = Message;
        date = Date;
        address = Address;
        isSent = SentRecieved;
    }

    /**
     * Get date of message.
     * @return Date in the form of seconds since jan 1st 1970.
     */
    public long getDate() {
        return date;
    }

    /**
     * Get message contents.
     * @return Message content string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message content string.
     * @param Message New message contents.
     */
    public void setMessage(String Message) { message = Message; }

    /**
     * Gets whether or not the message was sent or recieved.
     * @return True if sent, false otherwise.
     */
    public boolean isSent() {
        return isSent;
    }

    /**
     * Gets the address (phone number) of the message.
     * @return String of the message address.
     */
    public String getAddress() {
        return address;
    }

    @Override
    public int compareTo(Message m) {
        if (this.getDate() > m.getDate())
            return 1;
        else if (this.getDate() == m.getDate())
            return 0;
        else
            return -1;
    }
}
