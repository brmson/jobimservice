package de.rwthaachen.jobimservice;

import de.rwthaachen.jobimservice.rest.RestServer;

public class Main {
    private static RestServer restServer;

    public static void main(String[] args) throws Exception {
        restServer = new RestServer();
        restServer.start();
    }
}
