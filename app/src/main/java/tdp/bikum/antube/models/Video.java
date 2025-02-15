package tdp.bikum.antube.models;

public class Video {
    private String id;
    private String title;
    private String url; // **TRƯỜNG URL NÀY KHÔNG CẦN THIẾT CHO VIDEO LOCAL, CÓ THỂ XÓA HOẶC ĐỂ NGUYÊN**
    private String path; // **THÊM TRƯỜNG PATH ĐỂ LƯU ĐƯỜNG DẪN FILE VIDEO LOCAL**
    private String thumbnailUrl;

    // Constructor
    public Video(String id, String title, String path, String thumbnailUrl) { // **SỬA CONSTRUCTOR ĐỂ NHẬN THAM SỐ PATH**
        this.id = id;
        this.title = title;
        this.url = url; // **CÓ THỂ ĐỂ NGUYÊN HOẶC KHÔNG SỬ DỤNG**
        this.path = path; // **THÊM DÒNG NÀY**
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() { // **GETTER CHO PATH**
        return path;
    }

    public void setPath(String path) { // **SETTER CHO PATH**
        this.path = path;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}