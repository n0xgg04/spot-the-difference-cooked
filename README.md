# Spot The Difference (Java/JavaFX/Sockets/MySQL)

Dự án client-server chơi tìm điểm khác biệt. Xem cấu hình trong `server/src/main/resources/server-config.properties` và `client/src/main/resources/client-config.properties`.

- Build: `mvn -q -DskipTests package`
- Chạy server: `mvn -q -pl server exec:java`
- Chạy client (JavaFX): `mvn -q -pl client javafx:run`
- Công cụ admin upload ảnh: `mvn -q -pl admin javafx:run`

Xem thư mục `db/` để khởi tạo schema MySQL.

## Khởi tạo database và dữ liệu mẫu

1. Tạo schema và bảng:

```powershell
mysql -u root -p < db/schema.sql
```

2. Nạp dữ liệu mẫu (user với mật khẩu dạng plain text theo yêu cầu):

```powershell
mysql -u root -p spotgame < db/seed.sql
```

- User mẫu: alice/123456, bob/123456, carol/password
- Có sẵn vài trận đấu và bảng xếp hạng đã được tính lại.

## Chạy trực tiếp trong VS Code (khuyên dùng)

Đã cấu hình sẵn Tasks trong `.vscode/tasks.json`.

1. Mở Command Palette (Ctrl+Shift+P) > “Tasks: Run Task” > chọn “Run Server”.
   - Server sẽ chạy nền trên port 5050.
2. Lặp lại “Tasks: Run Task” > chọn “Run Client” để mở ứng dụng JavaFX.
3. (Tuỳ chọn) “Run Admin Uploader” để chuẩn bị dữ liệu bộ ảnh.

Mẹo/khắc phục nhanh:

- Nếu báo “Address already in use: bind”, hãy dừng tiến trình Java cũ rồi chạy lại server:
  - Windows PowerShell:
    ```powershell
    taskkill /F /IM java.exe
    ```
- Nếu client báo “Connection refused”, hãy đảm bảo server đang chạy và cấu hình `client-config.properties` trỏ tới đúng host/port.
- Nếu JavaFX không mở, thử cập nhật JDK 17 và đảm bảo VS Code có Java Extension Pack.

## Công cụ Admin Uploader (chuẩn bị dữ liệu)

Bạn có thể dùng module `admin` để nạp cặp ảnh và đánh dấu điểm khác biệt, dữ liệu sẽ lưu vào các bảng `image_sets`, `image_differences`.

Chạy:

```powershell
mvn -pl admin javafx:run
```

Chỉnh DB trong `admin/src/main/resources/admin-config.properties` nếu cần.

Lưu ý về nơi lưu ảnh (mặc định: lưu FILE, lưu đường dẫn vào DB):

- Ảnh được sao chép vào thư mục do cấu hình `storage.dir` (admin) và `content.dir` (server) chỉ định. Mặc định là `content/imagesets/` ở thư mục dự án.
- Database chỉ lưu đường dẫn tương đối (cột `img_left_path`, `img_right_path` trong bảng `image_sets`).
- Kiểm tra nhanh các bản ghi mới:

```sql
SELECT id, name, width, height, img_left_path, img_right_path, created_at
FROM image_sets
ORDER BY id DESC
LIMIT 5;
```

- Server sẽ đọc file theo `content.dir`, chuyển thành Base64 và gửi cho client khi bắt đầu trận.

mvn -pl server exec:java
mvn -pl client javafx:run
mvn -pl client javafx:run
mvn -pl admin javafx:run
