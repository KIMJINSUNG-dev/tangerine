INSERT IGNORE INTO document_types (name, description)
VALUES
    ('SONG', '수록곡 문서'),
    ('COMPOSER', '작곡가 문서'),
    ('GAME', '게임 타이틀 문서');

-- SONG 유형(type_id=1)의 기본 필드들
INSERT IGNORE INTO document_templates (type_id, field_key, field_name, field_type, required, display_order)
VALUES
    (1, 'composer', '작곡가', 'TEXT', true, 1),
    (1, 'bpm', 'BPM', 'NUMBER', true, 2),
    (1, 'genre', '장르', 'TEXT', false, 3),
    (1, 'bga', 'BGA 유무', 'BOOLEAN', false, 4);