package graphics;

import objects.Comment;
import objects.Group;
import api.APIRequests;
import api.Program;

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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainWindow extends JFrame {
    public final static int POST_COUNT = 200;

    private String currentGroupName;
    private Map<String, Group> groups;
    private String[] groupNames;

    private JPanel commentPanel;

    public MainWindow(Stream<Group> groups) {
        this.groups = groups.collect(Collectors.toMap(g -> g.name, g -> g));
        this.groupNames = this.groups.keySet().toArray(new String[this.groups.size()]);
        Arrays.sort(this.groupNames);

        setComponents();
    }

    private void setComponents() {
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


        JButton loadButton = new JButton("Update Posts");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addComments(commentPanel);
            }
        });

        // List of Posts from the current selected group
        commentPanel = new JPanel();
        commentPanel.setLayout(new GridLayout(0, 1));

        JScrollPane commentsScrollPane = new JScrollPane(commentPanel);
        commentsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        commentsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);



        add(listScroller, BorderLayout.WEST);
        add(loadButton, BorderLayout.NORTH);
        add(commentsScrollPane, BorderLayout.CENTER);
    }

    private void addComments(JPanel panel) {
        panel.removeAll();
        Group currentGroup = groups.get(currentGroupName);
        Stream<Comment> comments = APIRequests.getComments(currentGroup.id, POST_COUNT);

        comments.forEach(c -> addComment(c, panel));
        repaint();
        revalidate();
    }

    private void addComment(Comment comment, JPanel panel) {
        JLabel post = new JLabel(comment.wallPost.text);
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

        Program.banList.addID(comment.user.id);

        Date commentDate = new Date(comment.wallPost.date * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        JLabel dateLabel = new JLabel(sdf.format(commentDate));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        p.add(post);
        p.add(dateLabel);
        p.add(linkButton);
        panel.add(p);
    }

    private static void open(String uriString) {
        if (Desktop.isDesktopSupported()) {
            try {
                URI uri = new URI(uriString);
                Desktop.getDesktop().browse(uri);
            } catch (Exception e) {

            }
        } else {

        }
    }
}
