package org.dsa.iot.broker;

/**
 * @author Samuel Grenier
 */
public class Main {

    public static void main(String[] args) {
        Broker broker = Broker.create(args);
        if (broker != null) {
            broker.start();
        }
    }

}
