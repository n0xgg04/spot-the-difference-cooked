-- Seed data for Spot The Difference (plain text passwords stored in password_hash)
USE spotgame;

-- Users (plain passwords as requested)
INSERT INTO users (username, password_hash)
VALUES
  ('alice', '123456'),
  ('bob',   '123456'),
  ('carol', 'password')
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash);

-- Example matches between users
INSERT INTO matches (player_a_id, player_b_id, score_a, score_b, result, created_at)
VALUES
  ((SELECT id FROM users WHERE username='alice'), (SELECT id FROM users WHERE username='bob'),   5, 3, 'A',    NOW()),
  ((SELECT id FROM users WHERE username='bob'),   (SELECT id FROM users WHERE username='carol'), 2, 2, 'DRAW', NOW()),
  ((SELECT id FROM users WHERE username='carol'), (SELECT id FROM users WHERE username='alice'), 1, 4, 'B',    NOW())
;

-- Recompute leaderboard stats based on matches
UPDATE users u
LEFT JOIN (
  SELECT player_a_id AS uid, SUM(score_a) AS pts FROM matches GROUP BY player_a_id
) a ON u.id = a.uid
LEFT JOIN (
  SELECT player_b_id AS uid, SUM(score_b) AS pts FROM matches GROUP BY player_b_id
) b ON u.id = b.uid
LEFT JOIN (
  SELECT uid,
         SUM(win)   AS wins,
         SUM(loss)  AS losses,
         SUM(draws) AS draws
  FROM (
    SELECT player_a_id AS uid,
           CASE WHEN result='A'    THEN 1 ELSE 0 END AS win,
           CASE WHEN result='B'    THEN 1 ELSE 0 END AS loss,
           CASE WHEN result='DRAW' THEN 1 ELSE 0 END AS draws
    FROM matches
    UNION ALL
    SELECT player_b_id AS uid,
           CASE WHEN result='B'    THEN 1 ELSE 0 END AS win,
           CASE WHEN result='A'    THEN 1 ELSE 0 END AS loss,
           CASE WHEN result='DRAW' THEN 1 ELSE 0 END AS draws
    FROM matches
  ) t
  GROUP BY uid
) w ON u.id = w.uid
SET u.total_points = COALESCE(a.pts,0) + COALESCE(b.pts,0),
    u.total_wins   = COALESCE(w.wins,0),
    u.total_losses = COALESCE(w.losses,0),
    u.total_draws  = COALESCE(w.draws,0);
