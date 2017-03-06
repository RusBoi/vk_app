package api;

import graphics.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class Program {
    public static int APP_ID;
    public static String CLIENT_SECRET;
    public static String ACCESS_TOKEN;
    private final static int groupID = 97229237;
    public static BanList banList = loadBanList();

    public static void main(String[] args) throws WrongResponse {
        
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("resources/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        APP_ID = Integer.valueOf(prop.getProperty("APP_ID"));
        CLIENT_SECRET = prop.getProperty("CLIENT_SECRET");
        ACCESS_TOKEN = prop.getProperty("ACCESS_TOKEN");

        // load banlist

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = null;
                try {
                    f = new MainWindow(APIRequests.getMyGroups());
                } catch (WrongResponse wrongResponse) {
                    wrongResponse.printStackTrace();
                }
                f.setName("Whores On VK");
//                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
//                f.setUndecorated(true);
                f.setSize(new Dimension(1500, 720));
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                f.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        serializeBanList();
                    }
                });
            }
        });
    }

    private static BanList loadBanList() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("resources/banlist"))) {
            return (BanList) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void serializeBanList() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("resources/banlist"))) {
            oos.writeObject(banList);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
