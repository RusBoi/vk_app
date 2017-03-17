package graphics;

import objects.Comment;
import objects.Group;
import struct.APIRequests;
import struct.ProgramState;
import struct.WrongResponse;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainWindow extends JFrame {
    public final static int POST_COUNT = 20;
    private final static int COMMENT_LENGTH = 60;
    public static final int UNIT_INCREMENT = 16;

    private ProgramState ps;
    private String currentGroupName;
    private Map<String, Group> groups;
    private String[] groupNames;
    private LinkedHashMap<Comment, JButton> currentComments;

    private JPanel commentPanel;
    private JMenuBar menuBar;

    public MainWindow(ProgramState ps) {
        this.ps = ps;
        try {
            groups = APIRequests.getMyGroups(ps.ACCESS_TOKEN).collect(Collectors.toMap(g -> g.name, g -> g));
        } catch (WrongResponse wrongResponse) {
            wrongResponse.printStackTrace();
            groups = new HashMap<>();
        }
        groupNames = groups.keySet().toArray(new String[this.groups.size()]);
        Arrays.sort(groupNames);
        setComponents();
    }

    private void setComponents() {
        // Main Menu
        menuBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Main");

        JMenuItem viewBanList = new JMenuItem("Ban list");
        viewBanList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        mainMenu.add(viewBanList);

        menuBar.add(mainMenu);

        // List Of Groups
        JList groupList = new JList(groupNames);
        groupList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting())
                    return;
                JList source = (JList)e.getSource();
                currentGroupName = (String)source.getSelectedValue();
            }
        });
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setLayoutOrientation(JList.VERTICAL);
        groupList.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(groupList);
        listScroller.getVerticalScrollBar().setUnitIncrement(16);

        JButton loadButton = new JButton("Update Posts");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadComments(commentPanel);
            }
        });

        // List of Posts from the current selected group
        commentPanel = new JPanel();
        commentPanel.setLayout(new GridLayout(0, 1));

        JScrollPane commentsScroller = new JScrollPane(commentPanel);
        commentsScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        commentsScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentsScroller.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);

        add(listScroller, BorderLayout.WEST);
        add(loadButton, BorderLayout.NORTH);
        add(commentsScroller, BorderLayout.CENTER);
        setJMenuBar(menuBar);
    }

    private void loadComments(JPanel panel) {
        panel.removeAll();
        Group currentGroup = groups.get(currentGroupName);
        currentComments = APIRequests.getComments(currentGroup.id, POST_COUNT, ps.banList, ps.ACCESS_TOKEN)
                .collect(Collectors.toMap(c -> c, new Function<Comment, JButton>() {
                    @Override
                    public JButton apply(Comment comment) {
                        ps.banList.addID(comment.user.id);
                        JButton b = new JButton("Unblock");
                        b.setBackground(Color.RED);
                        b.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JButton b = (JButton)e.getSource();
                                if (b.getText().equals("Unblock")) {
                                    b.setBackground(Color.WHITE);
                                    ps.banList.removeID(comment.user.id);
                                    b.setText("Block");
                                } else {
                                    b.setBackground(Color.RED);
                                    ps.banList.addID(comment.user.id);
                                    b.setText("Unblock");
                                }
                            }
                        });
                        return b;
                    }
                }, (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));

        for (Map.Entry<Comment, JButton> e : currentComments.entrySet())
            addComment(e.getKey(), e.getValue(), panel);
        repaint();
        revalidate();
    }

    private void addComment(Comment comment, JButton blockButton, JPanel panel) {
        String text = comment.wallPost.text;
        if (text.length() > COMMENT_LENGTH)
            text = text.substring(0, COMMENT_LENGTH);
        JLabel post = new JLabel(text);
        try {
            ImageIcon icon = new ImageIcon(APIRequests.downloadImage(comment.user.photo_100));
            post.setIcon(icon);
        } catch (IOException e) {}

        post.setIconTextGap(20);
        post.setMaximumSize(new Dimension(panel.getWidth() / 2, 100));
        post.setMinimumSize(new Dimension(panel.getWidth() / 2, 100));

        JButton linkButton = new JButton("VK");
        linkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open("https://vk.com/id" + comment.user.id);
            }
        });

        Date commentDate = new Date(comment.wallPost.date * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        JLabel dateLabel = new JLabel(sdf.format(commentDate));


        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        p.add(post);
        p.add(dateLabel);
        p.add(Box.createHorizontalStrut(14));
        p.add(linkButton);
        p.add(Box.createHorizontalStrut(14));
        p.add(blockButton);
        panel.add(p);
    }

    private void open(String uriString) {
        if (Desktop.isDesktopSupported()) {
            try {
                URI uri = new URI(uriString);
                Desktop.getDesktop().browse(uri);
            } catch (Exception e) {

            }
        } else {

        }
    }

    public ProgramState getProgramState() {
        return ps;
    }
}
