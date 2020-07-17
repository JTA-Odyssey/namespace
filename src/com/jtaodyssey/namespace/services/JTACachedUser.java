package com.jtaodyssey.namespace.services;

import com.jtaodyssey.namespace.components.*;

import java.io.*;
import java.util.*;

/**
 * This class will save data to the appropriate location
 */
public class JTACachedUser {
    private static String storageRoot = null;
    private JTAUser user;
    private String userPath;
    private Map<String, List<JTATextMessage>> messages; // maps texts to the channel
                                                            // they were found on
    private Map<String, JTAChannel> channels;

    // todo we should be able to remove this
    private void initStorageRoot() {
        if (storageRoot == null) {
            Properties appProp = new Properties();
            String location = "config.properties";
            try {
                appProp.load(new BufferedInputStream(new FileInputStream(location)));
                JTACachedUser.storageRoot = appProp.getProperty("userStoragePath");
            } catch (IOException e) {
                e.printStackTrace();
            }
            File userRoot = new File(userPath);
            if (!userRoot.isDirectory()) {
                userRoot.mkdir();
            }
        }
    }

    public JTACachedUser(JTAUser user) {
        setUser(user);
        initStorageRoot();
        setUserPath(storageRoot
                + user.getFirstName().toLowerCase()
                + "_" + user.getLastName().toLowerCase() + "/");
        this.messages = new HashMap<>();
        this.channels = new HashMap<>();
    }

    public JTAUser getUser() {
        return user;
    }

    private void setUser(JTAUser user) {
        this.user = user;
    }

    public String getUserPath() {
        return userPath;
    }

    private void setUserPath(String userPath) {
        this.userPath = userPath;
    }

    public void record(JTAChannel channel, JTATextMessage message) {
        if (messages.get(channel.getName()) == null) {
            messages.put(channel.getName(), new ArrayList<>());
        }
        processChannel(channel);
        messages.get(channel.getName()).add(message);
        saveMessageData();
    }

    /**
     * Process channels. If they don't exist then add it to the list of
     * channels that the user currently follows
     */
    private void processChannel(JTAChannel channel) {
        channels.putIfAbsent(channel.getName(), channel);
    }

    /**
     * because this runs on a thread. if another thread tries to read before
     * it closes there will be a problem
     */
    private void saveMessageData() {
        saveFile(userPath + "messages.dat", messages);
        saveFile(userPath + "channels.dat", channels);
    }

    void loadMessageData() {
        // todo will it be necessary to thread this
        Object o = loadFile(userPath + "messages.dat");
        if (o instanceof HashMap) {
            messages = (HashMap<String, List<JTATextMessage>>)o;
        }
        o = loadFile(userPath + "channels.dat");
        if (o instanceof HashMap) {
            channels =  (HashMap<String, JTAChannel>)o;
        }
    }

    private void saveFile(String relativeFilePath, Object o) {
        new Thread(()-> {
            try {
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(relativeFilePath)));
                os.writeObject(o);
                os.flush();
                os.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }).start();
    }

    private Object loadFile(String relativePathFromRoot) {
        File fin = new File(relativePathFromRoot);
        if (fin.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(relativePathFromRoot)));
                Object o = in.readObject();
                in.close();
                return o;
            }
            catch (IOException io) {
                io.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    Map<String, List<JTATextMessage>> getMessages() { return messages; }

    public List<JTATextMessage> getMessages(String channel) {
        if (messages.get(channel) == null) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(messages.get(channel));
    }

    public List<JTATextMessage> getMessages(JTAChannel channel) {
        return getMessages(channel.getName());
    }

    public Collection<JTAChannel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    Set<String> getChannelNames() {
        return Collections.unmodifiableSet(channels.keySet());
    }
}
