package objects;

public class User {
    public int id;
    public String first_name;
    public String last_name;
    public String deactivated;
    public int sex;
    public String photo_200;
    public String photo_50;
    public String photo_100;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id == user.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
