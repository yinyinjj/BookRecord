-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- Create books table
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    cover_url VARCHAR(500),
    isbn VARCHAR(20),
    publisher VARCHAR(100),
    publish_date DATE,
    reading_status VARCHAR(20) NOT NULL DEFAULT 'WANT_TO_READ',
    start_date DATE,
    finish_date DATE,
    rating DECIMAL(3,2),
    page_count INTEGER,
    current_page INTEGER DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5))
);

CREATE INDEX idx_books_user ON books(user_id);
CREATE INDEX idx_books_status ON books(reading_status);
CREATE INDEX idx_books_title ON books(title);

-- Create quotes table
CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    chapter VARCHAR(100),
    page_number INTEGER,
    note TEXT,
    color VARCHAR(20),
    tags VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quotes_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

CREATE INDEX idx_quotes_book ON quotes(book_id);

-- Create reading_notes table
CREATE TABLE reading_notes (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    quote_id BIGINT,
    chapter VARCHAR(100),
    page_number INTEGER,
    title VARCHAR(200),
    content TEXT NOT NULL,
    note_type VARCHAR(20) NOT NULL DEFAULT 'THOUGHT',
    tags VARCHAR(500),
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reading_notes_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_reading_notes_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE SET NULL
);

CREATE INDEX idx_reading_notes_book ON reading_notes(book_id);
CREATE INDEX idx_reading_notes_quote ON reading_notes(quote_id);
CREATE INDEX idx_reading_notes_type ON reading_notes(note_type);

-- Create update timestamp function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_books_updated_at BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quotes_updated_at BEFORE UPDATE ON quotes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reading_notes_updated_at BEFORE UPDATE ON reading_notes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();