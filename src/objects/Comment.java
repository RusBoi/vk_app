package objects;

public class Comment {
    public User user;
    public WallPost wallPost;

    public Comment(User user, WallPost wallPost) {
        this.user = user;
        this.wallPost = wallPost;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "user=" + user +
                ", wallPost=" + wallPost +
                '}';
    }
}
