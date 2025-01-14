-- Создаем таблицу site
CREATE TABLE IF NOT EXISTS site (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL,
    status_time DATETIME NOT NULL,
    last_error TEXT DEFAULT NULL,
    url VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

-- Создаем таблицу page
CREATE TABLE IF NOT EXISTS page (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_id BIGINT,
    path TEXT NOT NULL,
    code INT NOT NULL,
    content MEDIUMTEXT NOT NULL,
    FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE,
    UNIQUE KEY unique_path (site_id, path(255))
);
