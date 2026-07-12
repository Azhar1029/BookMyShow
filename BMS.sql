
create database bms;
#drop database bms;
use bms;


-- 1. Cities
CREATE TABLE cities (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    state VARCHAR(255),
    CONSTRAINT uq_cities_name UNIQUE (name)
);

-- 2. Users
CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(255),
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at DATETIME,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

-- 3. Movies
CREATE TABLE movies (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       VARCHAR(1000),
    genre             VARCHAR(255),
    language          VARCHAR(255),
    duration_minutes  INT,
    rating            DOUBLE,
    release_date      DATE,
    poster_url        VARCHAR(500)
);

-- 4. Theaters (belongs to a City)
CREATE TABLE theaters (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    address  VARCHAR(255),
    city_id  BIGINT NOT NULL,
    CONSTRAINT fk_theaters_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- 5. Screens (belongs to a Theater)
CREATE TABLE screens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    total_seats INT,
    theater_id  BIGINT NOT NULL,
    CONSTRAINT fk_screens_theater FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

-- 6. Seats (belongs to a Screen)
CREATE TABLE seats (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number  VARCHAR(255) NOT NULL,
    seat_row     VARCHAR(10),
    seat_col     INT,
    seat_type    VARCHAR(20),
    screen_id    BIGINT NOT NULL,
    CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens(id),
    CONSTRAINT chk_seats_type CHECK (seat_type IN ('REGULAR', 'PREMIUM', 'VIP'))
);

-- 7. Shows (a Movie playing on a Screen at a date/time)
CREATE TABLE shows (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id      BIGINT NOT NULL,
    screen_id     BIGINT NOT NULL,
    show_date     DATE NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME,
    ticket_price  DOUBLE,
    CONSTRAINT fk_shows_movie  FOREIGN KEY (movie_id)  REFERENCES movies(id),
    CONSTRAINT fk_shows_screen FOREIGN KEY (screen_id) REFERENCES screens(id)
);

-- 8. Bookings (a User booking a Show)
CREATE TABLE bookings (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    show_id     BIGINT NOT NULL,
    total_price DOUBLE,
    status      VARCHAR(20),
    booked_at   DATETIME,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_show FOREIGN KEY (show_id) REFERENCES shows(id),
    CONSTRAINT chk_bookings_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

-- ALTER TABLE bookings DROP CONSTRAINT chk_bookings_status;
-- ALTER TABLE bookings ADD CONSTRAINT chk_bookings_status
--     CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED'));

-- SELECT CHECK_CLAUSE
-- FROM information_schema.CHECK_CONSTRAINTS
-- WHERE CONSTRAINT_SCHEMA = 'BMS' AND CONSTRAINT_NAME = 'chk_bookings_status';

-- SHOW CREATE TABLE bookings;

-- 9. Booking <-> Seat join table (which seats belong to which booking)
CREATE TABLE booking_seats (
    booking_id  BIGINT NOT NULL,
    seat_id     BIGINT NOT NULL,
    PRIMARY KEY (booking_id, seat_id),
    CONSTRAINT fk_booking_seats_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_booking_seats_seat    FOREIGN KEY (seat_id)    REFERENCES seats(id)
);

-- 1. Cities
INSERT INTO cities (name, state) VALUES ('Mumbai', 'Maharashtra');
INSERT INTO cities (name, state) VALUES ('Delhi', 'Delhi');
INSERT INTO cities (name, state) VALUES ('Bangalore', 'Karnataka');
INSERT INTO cities (name, state) VALUES ('Hyderabad', 'Telangana');
INSERT INTO cities (name, state) VALUES ('Chennai', 'Tamil Nadu');

select * from cities;

#SET sql_safe_updates = 0;

-- 2. Users
-- All four passwords are 'pass123' (bcrypt-hashed). Rahul is ADMIN, the rest are USER.
INSERT INTO users (name, email, password, phone, role, created_at)
VALUES ('Rahul Sharma', 'rahul@example.com', '$2b$10$cY0J6pG5JJACSJY5YMHaiuZd7ntYlEdh19rAj2YW4PjvLuVXeZL.S', '9876543210', 'ADMIN', NOW());
INSERT INTO users (name, email, password, phone, role, created_at)
VALUES ('Priya Patel', 'priya@example.com', '$2b$10$ycLgtIT2OAJQ/IFdirAQU.V.fz.aNiULIbVeVsYmWKsQ2L7aMDoWe', '9876543211', 'USER', NOW());
INSERT INTO users (name, email, password, phone, role, created_at)
VALUES ('Amit Kumar', 'amit@example.com', '$2b$10$DoSHTYZxo5e/HwO6fNtdJO6GOU0J5R/.Uuu7FNbxSSfhzNWacI6WS', '9876543212', 'USER', NOW());
INSERT INTO users (name, email, password, phone, role, created_at)
VALUES ('Sneha Reddy', 'sneha@example.com', '$2b$10$lC/PhxIpn6POUH/tMvscROEQKjmcjsWuVb6uXhNFi.I/6SRVL3Vzi', '9876543213', 'USER', NOW());

use bms;
select * from users;
UPDATE users SET role = 'ADMIN' WHERE email = 'azhar@gmail.com';


-- 3. Movies
INSERT INTO movies (title, description, genre, language, duration_minutes, rating, release_date, poster_url)
VALUES ('Pushpa 2', 'The rule of Pushpa Raj continues', 'Action', 'Telugu', 150, 8.2, '2025-12-05', 'https://www.cinejosh.com/newsimg/newsmainimg/allu-arjun-to-make-grand-entry-for-pushpa-2-the-rule-trailer-launch_b_1611240530.jpg');

INSERT INTO movies (title, description, genre, language, duration_minutes, rating, release_date, poster_url)
VALUES ('Jawan', 'A prison warden recruits inmates to commit acts of terror', 'Action', 'Hindi', 160, 7.9, '2025-09-07', 'https://upload.wikimedia.org/wikipedia/en/3/39/Jawan_film_poster.jpg');

INSERT INTO movies (title, description, genre, language, duration_minutes, rating, release_date, poster_url)
VALUES ('Animal', 'A son undergoes a transformation when his father is in danger', 'Drama', 'Hindi', 180, 7.5, '2025-12-01', 'https://upload.wikimedia.org/wikipedia/en/thumb/9/90/Animal_%282023_film%29_poster.jpg/250px-Animal_%282023_film%29_poster.jpg');

INSERT INTO movies (title, description, genre, language, duration_minutes, rating, release_date, poster_url)
VALUES ('Leo', 'A mild-mannered cafe owner hides a violent past', 'Thriller', 'Tamil', 155, 7.0, '2025-10-19', 'https://upload.wikimedia.org/wikipedia/en/7/75/Leo_%282023_Indian_film%29.jpg');

INSERT INTO movies (title, description, genre, language, duration_minutes, rating, release_date, poster_url)
VALUES ('Dunki', 'The story of illegal immigration through donkey flight', 'Comedy', 'Hindi', 140, 6.8, '2025-12-21', 'https://upload.wikimedia.org/wikipedia/en/thumb/4/4f/Dunki_poster.jpg/250px-Dunki_poster.jpg');

-- 4. Theaters (cityId references: 1=Mumbai, 2=Delhi, 3=Bangalore, 4=Hyderabad, 5=Chennai)
INSERT INTO theaters (name, address, city_id) VALUES ('PVR Phoenix', 'Lower Parel, Mumbai', 1);
INSERT INTO theaters (name, address, city_id) VALUES ('INOX Nariman Point', 'Nariman Point, Mumbai', 1);
INSERT INTO theaters (name, address, city_id) VALUES ('PVR Select City', 'Saket, New Delhi', 2);
INSERT INTO theaters (name, address, city_id) VALUES ('GOLD Cinema', 'Rajiv Chowk, New Delhi', 2);
INSERT INTO theaters (name, address, city_id) VALUES ('INOX Mantri Mall', 'Malleshwaram, Bangalore', 3);
INSERT INTO theaters (name, address, city_id) VALUES ('AMB Cinemas', 'Gachibowli, Hyderabad', 4);
INSERT INTO theaters (name, address, city_id) VALUES ('SPI Palazzo', 'Vadapalani, Chennai', 5);


select * from theaters;


-- 5. Screens (theaterId references: 1=PVR Phoenix, 2=INOX Nariman, 3=PVR Select, 4=INOX Mantri, 5=AMB, 6=SPI)
INSERT INTO screens (name, total_seats, theater_id) VALUES ('Screen 1', 10, 1);
INSERT INTO screens (name, total_seats, theater_id) VALUES ('Screen 2', 8, 1);
INSERT INTO screens (name, total_seats, theater_id) VALUES ('Audi 1', 10, 2);
INSERT INTO screens (name, total_seats, theater_id) VALUES ('Screen 1', 10, 3);
INSERT INTO screens (name, total_seats, theater_id) VALUES ('IMAX', 12, 4);
INSERT INTO screens (name, total_seats, theater_id) VALUES ('Dolby Atmos', 10, 5);
INSERT INTO screens (name, total_seats, theater_id) VALUES ('Screen 1', 8, 6);

-- 6. Seats for Screen 1 (id=1, PVR Phoenix Screen 1) — 10 seats
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A1', 'A', 1, 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A2', 'A', 2, 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A3', 'A', 3, 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A4', 'A', 4, 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A5', 'A', 5, 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B1', 'B', 1, 'PREMIUM', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B2', 'B', 2, 'PREMIUM', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B3', 'B', 3, 'PREMIUM', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C1', 'C', 1, 'VIP', 1);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C2', 'C', 2, 'VIP', 1);

-- Seats for Screen 2 (id=2, PVR Phoenix Screen 2) — 8 seats
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A1', 'A', 1, 'REGULAR', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A2', 'A', 2, 'REGULAR', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A3', 'A', 3, 'REGULAR', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A4', 'A', 4, 'REGULAR', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B1', 'B', 1, 'PREMIUM', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B2', 'B', 2, 'PREMIUM', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B3', 'B', 3, 'PREMIUM', 2);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C1', 'C', 1, 'VIP', 2);

-- Seats for IMAX (id=5, INOX Mantri) — 12 seats
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A1', 'A', 1, 'REGULAR', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A2', 'A', 2, 'REGULAR', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A3', 'A', 3, 'REGULAR', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('A4', 'A', 4, 'REGULAR', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B1', 'B', 1, 'PREMIUM', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B2', 'B', 2, 'PREMIUM', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B3', 'B', 3, 'PREMIUM', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('B4', 'B', 4, 'PREMIUM', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C1', 'C', 1, 'VIP', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C2', 'C', 2, 'VIP', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C3', 'C', 3, 'VIP', 5);
INSERT INTO seats (seat_number, seat_row, seat_col, seat_type, screen_id) VALUES ('C4', 'C', 4, 'VIP', 5);

-- 7. Shows (movieId: 1=Pushpa2, 2=Jawan, 3=Animal, 4=Leo, 5=Dunki)
--          (screenId: 1=PVR Phoenix S1, 2=PVR Phoenix S2, 3=INOX Nariman, 4=PVR Select, 5=IMAX Mantri, 6=AMB Dolby, 7=SPI)
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (1, 1, '2026-03-15', '10:00:00', '12:30:00', 250.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (1, 1, '2026-03-15', '14:00:00', '16:30:00', 300.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (2, 2, '2026-03-15', '11:00:00', '13:40:00', 200.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (3, 5, '2026-03-15', '18:00:00', '21:00:00', 450.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (4, 5, '2026-03-16', '10:00:00', '12:35:00', 400.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (5, 2, '2026-03-16', '15:00:00', '17:20:00', 200.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (1, 5, '2026-03-16', '20:00:00', '22:30:00', 500.00);
INSERT INTO shows (movie_id, screen_id, show_date, start_time, end_time, ticket_price) VALUES (3, 5, '2026-03-15', '9:00:00', '12:00:00', 800.00);

-- 8. Bookings (userId: 1=Rahul, 2=Priya, 3=Amit, 4=Sneha)
--             (showId: 1=Pushpa morning, 3=Jawan, 4=Animal IMAX)
INSERT INTO bookings (user_id, show_id, total_price, status, booked_at) VALUES (1, 1, 500.00, 'CONFIRMED', NOW());
INSERT INTO bookings (user_id, show_id, total_price, status, booked_at) VALUES (2, 3, 200.00, 'CONFIRMED', NOW());
INSERT INTO bookings (user_id, show_id, total_price, status, booked_at) VALUES (3, 4, 900.00, 'CONFIRMED', NOW());
INSERT INTO bookings (user_id, show_id, total_price, status, booked_at) VALUES (4, 1, 250.00, 'CANCELLED', NOW());

-- 9. Booking-Seats mapping
-- Booking 1 (Rahul → Pushpa morning): seats 1,2 (A1,A2 of Screen 1)
INSERT INTO booking_seats (booking_id, seat_id) VALUES (1, 1);
INSERT INTO booking_seats (booking_id, seat_id) VALUES (1, 2);
-- Booking 2 (Priya → Jawan): seat 11 (A1 of Screen 2)
INSERT INTO booking_seats (booking_id, seat_id) VALUES (2, 11);
-- Booking 3 (Amit → Animal IMAX): seats 19,20 (A1,A2 of IMAX)
INSERT INTO booking_seats (booking_id, seat_id) VALUES (3, 19);
INSERT INTO booking_seats (booking_id, seat_id) VALUES (3, 20);
-- Booking 4 (Sneha cancelled → Pushpa morning): seat 3 (A3 of Screen 1)
INSERT INTO booking_seats (booking_id, seat_id) VALUES (4, 3);

SELECT * from bookings;