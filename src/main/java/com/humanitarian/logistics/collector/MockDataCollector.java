package com.humanitarian.logistics.collector;

import com.humanitarian.logistics.model.Post;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MockDataCollector implements DataCollector {
    private final String sourceName;
    private boolean authenticated;
    private String status;
    private static final Random RAND = new Random(42);

    private static final String[][] SAMPLE_TEXTS = {
        {"positive", "Cảm ơn đội cứu hộ đã giúp đỡ người dân vùng lũ! Thật tuyệt vời!"},
        {"positive", "Tinh thần đoàn kết của người Việt thật đáng khâm phục, mọi người cùng giúp đỡ nhau."},
        {"positive", "Hàng cứu trợ đã đến kịp thời, cảm ơn tấm lòng hảo tâm của mọi người."},
        {"positive", "Đội ngũ y tế đang làm việc không mệt mỏi để cứu chữa người bị thương."},
        {"positive", "Các đơn vị đang khẩn trương khắc phục hậu quả bão, rất đáng khen ngợi."},
        {"positive", "Chính quyền đã hỗ trợ tiền mặt kịp thời cho các hộ dân bị thiệt hại."},
        {"positive", "Cảm ơn các nhà hảo tâm đã gửi thực phẩm và nước uống đến vùng lũ."},
        {"negative", "Nhà cửa bị sập hoàn toàn sau bão, không biết bao giờ mới xây lại được."},
        {"negative", "Đường sá bị ngập nặng, giao thông tê liệt hoàn toàn, không thể di chuyển."},
        {"negative", "Mất điện, mất nước đã 3 ngày, cuộc sống vô cùng khó khăn."},
        {"negative", "Người dân vùng lũ đang rất cần được cứu trợ khẩn cấp, chưa thấy ai đến."},
        {"negative", "Cơ sở hạ tầng bị phá hủy nghiêm trọng, cầu đường sạt lở nặng."},
        {"negative", "Hàng cứu trợ đến chậm, phân phối không công bằng."},
        {"negative", "Bão quá mạnh, thiệt hại quá lớn, nhiều người mất nhà cửa."},
        {"neutral", "Bão Yagi đã đổ bộ vào các tỉnh miền Bắc với sức gió mạnh nhất."},
        {"neutral", "Theo dự báo, bão sẽ suy yếu dần trong 24 giờ tới."},
        {"neutral", ""}, // empty for variety
    };

    public MockDataCollector(String sourceName) {
        this.sourceName = sourceName;
        this.authenticated = false;
        this.status = "Not authenticated";
    }

    @Override
    public String getSourceName() { return sourceName; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public void authenticate(String apiKey, String apiSecret) {
        this.authenticated = true;
        this.status = "Connected (mock)";
    }

    @Override
    public String getStatus() { return status; }

    @Override
    public List<Post> collect(String keyword, int limit) {
        return generateMockPosts(keyword, limit);
    }

    @Override
    public List<Post> collectWithHashtag(String hashtag, int limit) {
        return generateMockPosts(hashtag, limit);
    }

    @Override
    public List<Post> collectByDateRange(String keyword, String startDate, String endDate, int limit) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        return generateMockPosts(keyword, limit, start, end);
    }

    private List<Post> generateMockPosts(String keyword, int limit) {
        LocalDateTime start = LocalDateTime.of(2024, 9, 6, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 9, 30, 23, 59);
        return generateMockPosts(keyword, limit, start, end);
    }

    private List<Post> generateMockPosts(String keyword, int limit, LocalDateTime start, LocalDateTime end) {
        List<Post> posts = new ArrayList<>();
        long totalHours = java.time.Duration.between(start, end).toHours();

        for (int i = 0; i < limit; i++) {
            Post post = new Post();
            post.setId(sourceName + "_" + System.currentTimeMillis() + "_" + i);
            post.setSource(sourceName);
            post.setAuthorId("user_" + RAND.nextInt(1000));
            post.setAuthorName("User " + RAND.nextInt(1000));

            String[] sample = SAMPLE_TEXTS[RAND.nextInt(SAMPLE_TEXTS.length)];
            String content = sample[1].isEmpty()
                ? keyword + " là chủ đề đang được quan tâm."
                : sample[1];

            post.setContent(content);

            long randomHours = RAND.nextLong(totalHours);
            post.setTimestamp(start.plusHours(randomHours).plusMinutes(RAND.nextInt(60)));
            post.setLanguage("vi");
            post.setLikeCount(RAND.nextInt(500));
            post.setShareCount(RAND.nextInt(100));
            post.setCommentCount(RAND.nextInt(50));
            post.addMetadata("sentiment", sample[0]);

            posts.add(post);
        }

        posts.sort(Comparator.comparing(Post::getTimestamp));
        return posts;
    }

    public static List<Post> generateYagiSampleData() {
        List<Post> allPosts = new ArrayList<>();
        String[] sources = {"twitter", "facebook", "tiktok", "youtube"};
        for (String source : sources) {
            MockDataCollector mock = new MockDataCollector(source);
            allPosts.addAll(mock.collect(source + " posts about Yagi", 50));
        }
        return allPosts;
    }
}
