package struct;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BanList implements Serializable {
    private Set<Integer> banList;

    public BanList() {
        banList = new HashSet<>();
    }

    public void addID(int id) {
        banList.add(id);
    }

    public void removeID(int id) {
        banList.remove(id);
    }

    public boolean contains(int id) {
        return banList.contains(id);
    }
}
