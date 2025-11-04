# Thư mục âm thanh cho game

Đặt các file âm thanh vào đây. Hỗ trợ các định dạng: `.mp3`, `.wav`, `.m4a`

## Các âm thanh gợi ý:

- `click.wav` - Âm thanh khi click chuột
- `correct.wav` - Âm thanh khi tìm đúng điểm khác biệt (hit)
- `wrong.wav` - Âm thanh khi click sai (miss)
- `button_hover.wav` - Âm thanh khi hover button
- `game_start.wav` - Âm thanh khi bắt đầu game
- `game_over.wav` - Âm thanh khi kết thúc game
- `victory.wav` - Âm thanh khi thắng
- `defeat.wav` - Âm thanh khi thua
- `notification.wav` - Âm thanh thông báo
- `invite.wav` - Âm thanh khi nhận lời mời
- `countdown.wav` - Âm thanh đếm ngược (tick tock)
- `timeout.wav` - Âm thanh hết giờ

## Ví dụ load âm thanh trong code:

```java
// Load âm thanh
String soundPath = getClass().getResource("/sounds/correct.wav").toExternalForm();
AudioClip sound = new AudioClip(soundPath);

// Phát âm thanh
sound.play();

// Phát với volume
sound.play(0.5); // 50% volume
```

## Thư viện âm thanh miễn phí:

- https://freesound.org/
- https://mixkit.co/free-sound-effects/
- https://www.zapsplat.com/
- https://soundbible.com/

**Lưu ý:** File âm thanh nên nhỏ gọn (< 1MB) để load nhanh.
