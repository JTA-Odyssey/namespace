package com.jtaodyssey.namespace.services;

import com.jtaodyssey.namespace.components.JTARegistration;
import com.jtaodyssey.namespace.database.DBManager;
import com.jtaodyssey.namespace.database.SQLDatabase;

/**
 * This service is used to register users to the db
 */
public class JTARegistrationService {
    private volatile static JTARegistrationService reg = null;
    private DBManager manager;

    private JTARegistrationService() {
        this.manager = SQLDatabase.getInstance();
    }

    public static JTARegistrationService getInstance() {
        if (reg == null) {
            synchronized (AuthenticationService.class) {
                if (reg == null) {
                    reg = new JTARegistrationService();
                }
            }
        }
        return reg;
    }

    public boolean register(JTARegistration registration) {
        try {
            manager.Registration(registration);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This will apply the update for a given user to the user database
     *  Will return true if successful or false if the username already exists
     */
    public boolean update(JTARegistration updatedReg) {
        // make call to update
        return false;
    }
}
