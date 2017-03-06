package struct;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import objects.Comment;
import objects.Group;
import objects.User;
import objects.WallPost;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class APIRequests {
    public final static int DELAY = 300;
    
    private static WrongResponse getError(JsonObject jsonObject) {
        Gson gson = new Gson();
        return gson.fromJson(jsonObject.get("error").getAsJsonObject(), WrongResponse.class);
    }

    private static InputStream myGroupsJson() {
        String urlString = String.format("https://api.vk.com/method/groups.get?extended=1&access_token=%s&v=5.62", Program.ACCESS_TOKEN);
        System.out.println(urlString);
        try {
            return new URL(urlString).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static InputStream wallJson(int groupID, int count, int offset) {
        String urlString = String.format("https://api.vk.com/method/wall.get?owner_id=%d&count=%d&offset=%d&access_token=%s&v=5.62", -groupID, count, offset, Program.ACCESS_TOKEN);
        System.out.println(urlString);
        try {
            return new URL(urlString).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static InputStream userInfoJson(int userID) {
        String urlString = String.format("https://api.vk.com/method/users.get?user_ids=%d&fields=photo_200,photo_50,photo_100&access_token=%s&v=5.62", userID, Program.ACCESS_TOKEN);
        System.out.println(urlString);
        try {
            return new URL(urlString).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUser(int userID) throws WrongResponse {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(APIRequests.userInfoJson(userID)), JsonObject.class);
        try {
            Thread.sleep(DELAY); // !!!!
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            JsonElement jsonElement = jsonObject.get("response").getAsJsonArray().get(0);
            return gson.fromJson(jsonElement, User.class);

        } catch (NullPointerException ex) {
            throw getError(jsonObject);
        }
    }

    public static Stream<Group> getMyGroups() throws WrongResponse {
        List<Group> groups = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(APIRequests.myGroupsJson()), JsonObject.class);
        try {
            JsonArray groupArray = jsonObject.get("response").getAsJsonObject().get("items").getAsJsonArray();
            groupArray.forEach((el) -> groups.add(gson.fromJson(el, Group.class)));
            return groups.stream();

        } catch(RuntimeException ex) {
            throw getError(jsonObject);
        }
    }

    private static Stream<WallPost> getWallPosts(int groupID, int count) {
        List<WallPost> wallPosts = new ArrayList<>();
        Gson gson = new Gson();
        int downloadedCount = 0;

        while (downloadedCount < count) {
            InputStream resp = APIRequests.wallJson(groupID, Math.min(100, count - downloadedCount), downloadedCount);
            try {
                Thread.sleep(DELAY); // !!!!
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            downloadedCount += Math.min(100, count - downloadedCount);
            JsonObject jsonObject = gson.fromJson(new InputStreamReader(resp), JsonObject.class);
            try {
                JsonArray jsonArray = jsonObject.get("response").getAsJsonObject().get("items").getAsJsonArray();
                jsonArray.forEach((el) -> wallPosts.add(gson.fromJson(el, WallPost.class)));

            } catch (NullPointerException ex) {
                      }
        }
        return wallPosts.stream()
                .filter(wp -> !Program.banList.contains(wp.from_id))
                .filter(wp -> wp.from_id > 0)
                .distinct();
    }

    public static Stream<Comment> getComments(int groupID, int count) {
        return getWallPosts(groupID, count)
                    .map(new Function<WallPost, Comment>() {
                        @Override
                        public Comment apply(WallPost wallPost) {
                            try {
                                return new Comment(getUser(wallPost.from_id), wallPost);
                            } catch (WrongResponse wrongResponse) {
                                wrongResponse.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .peek(new Consumer<Comment>() {
                        @Override
                        public void accept(Comment c) {
                            if (c.user.sex == 2 || c.user.deactivated != null)
                                Program.banList.addID(c.user.id);
                        }
                    })
                    .filter(c -> c.user.sex != 2)
                    .filter(c -> c.user.deactivated == null);
    }
    
    public static Image downloadImage(String location) throws IOException {
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return ImageIO.read(url);
    }
}
