package objects;

public class WallPost {
    public int id;
    public int from_id;
    public int date;
    public String text;

    @Override
    public String toString() {
        return "WallPost{" +
                "text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WallPost wallPost = (WallPost) o;

        return from_id == wallPost.from_id;
    }

    @Override
    public int hashCode() {
        return from_id;
    }
}
