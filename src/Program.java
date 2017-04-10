import graphics.MainWindow;
import struct.BanList;
import struct.ProgramState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class Program {
    public static void main(String[] args) {
        // Устанавливаем Look and Feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

        } catch (Exception ex) {}


        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("resources/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int APP_ID = Integer.valueOf(prop.getProperty("APP_ID"));
        String CLIENT_SECRET = prop.getProperty("CLIENT_SECRET");
        String ACCESS_TOKEN = prop.getProperty("ACCESS_TOKEN");

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                BanList banList = null;
                try {
                    banList = loadBanList();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    banList = new BanList();
                }
                JFrame f = new MainWindow(new ProgramState(APP_ID, CLIENT_SECRET, ACCESS_TOKEN, banList));
                f.setName("Whores On VK");
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                f.setSize(new Dimension(1500, 720));
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                f.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        BanList bs = ((MainWindow) e.getSource()).getProgramState().banList;
                        serializeBanList(bs);
                    }
                });
            }
        });
    }

    private static BanList loadBanList() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("resources/banlist"));
        try {
            return (BanList) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void serializeBanList(BanList banList) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("resources/banlist"))) {
            oos.writeObject(banList);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
