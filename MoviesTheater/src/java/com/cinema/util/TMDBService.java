package com.cinema.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vjphoalac
 */
public class TMDBService {

    private static final String API_KEY = "345edfcb79bbd8b705203592b42549a5";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    public static Map<String, String> fetchMovieData(String movieName) {
        Map<String, String> result = new HashMap<>();
        try {
            // 1. Gọi API tìm kiếm theo tên phim
            // URL có chứa "language=vi-VN" để ưu tiên lấy tiếng Việt nếu có
            String searchUrl = BASE_URL + "/search/movie?api_key=" + API_KEY
                    + "&query=" + URLEncoder.encode(movieName, "UTF-8") + "&language=vi-VN";
            JsonObject searchData = callApi(searchUrl);
            JsonArray results = searchData.getAsJsonArray("results");

            if (results.size() == 0) {
                return null;
            }

            // Lấy ID phim đầu tiên (phim phù hợp nhất)
            int movieId = results.get(0).getAsJsonObject().get("id").getAsInt();

            // 2. Gọi API lấy chi tiết dựa vào ID phim ở trên
            // append_to_response=videos,credits: Gộp lấy luôn video (trailer) và credits (đạo diễn, diễn viên) trong 1 lần gọi
            String detailUrl = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY
                    + "&append_to_response=videos,credit&language=vi-VN";
            JsonObject details = callApi(detailUrl);

            // Bắt đầu bóc tách dữ liệu JSON và lưu vào Map
            result.put("MovieName", details.has("title") && !details.get("title").isJsonNull() ? details.get("title").getAsString() : "");
            result.put("Description", details.has("overview") && !details.get("overview").isJsonNull() ? details.get("overview").getAsString() : "");
            result.put("Duration", details.has("runtime") && !details.get("runtime").isJsonNull() ? details.get("runtime").getAsString() : "0");

            // Poster phim
            if (details.has("poster_path") && !details.get("poster_path").isJsonNull()) {
                result.put("Poster", "https://image.tmdb.org/t/p/w500" + details.get("poster_path").getAsString());
            }
            // Kinh phí và Doanh thu (TMDB tính bằng USD)
            result.put("Budget", details.has("budget") ? String.valueOf(details.get("budget").getAsLong()) : "0");
            result.put("GlobalBoxOffice", details.has("revenue") ? String.valueOf(details.get("revenue").getAsLong()) : "0");
            // Ngôn ngữ và Quốc gia
            if (details.has("original_language") && !details.get("original_language").isJsonNull()) {
                result.put("Language", details.get("original_language").getAsString().toUpperCase());
            }
            if (details.has("production_countries") && details.getAsJsonArray("production_countries").size() > 0) {
                result.put("Country", details.getAsJsonArray("production_countries").get(0).getAsJsonObject().get("name").getAsString());
            }
            // Lọc ra Đạo diễn và lấy tối đa 5 Diễn viên nổi bật
            if (details.has("credits")) {
                JsonObject credits = details.getAsJsonObject("credits");

                // Duyệt qua đội ngũ (Crew) để lấy người có công việc là Director
                for (JsonElement c : credits.getAsJsonArray("crew")) {
                    JsonObject person = c.getAsJsonObject();
                    if ("Director".equals(person.get("job").getAsString())) {
                        result.put("Director", person.get("name").getAsString());
                        break;
                    }
                }

                // Lấy 5 người đầu tiên trong mảng Diễn viên (Cast)
                JsonArray cast = credits.getAsJsonArray("cast");
                StringBuilder castNames = new StringBuilder();
                for (int i = 0; i < Math.min(cast.size(), 5); i++) {
                    castNames.append(cast.get(i).getAsJsonObject().get("name").getAsString());
                    if (i < Math.min(cast.size(), 5) - 1) {
                        castNames.append(", ");
                    }
                }
                result.put("Cast", castNames.toString());
            }
            // Lọc ra video loại Trailer từ Youtube
            if (details.has("videos")) {
                for (JsonElement v : details.getAsJsonObject("videos").getAsJsonArray("results")) {
                    JsonObject video = v.getAsJsonObject();
                    if ("YouTube".equals(video.get("site").getAsString()) && "Trailer".equals(video.get("type").getAsString())) {
                        result.put("Trailer", "https://www.youtube.com/watch?v=" + video.get("key").getAsString());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "Lỗi kết nối TMDB API: " + e.getMessage());
        }
        return result; // Trả Map chứa tất cả thông tin về
    }
    
    // Hàm hỗ trợ thực hiện HTTP GET Request
    private static JsonObject callApi(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP error code: " + conn.getResponseCode());
        }
        
        // Sử dụng Gson để parse chuỗi trả về JsonObject
        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        return JsonParser.parseReader(reader).getAsJsonObject();
    }
}


