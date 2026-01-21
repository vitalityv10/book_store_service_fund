INSERT INTO EMPLOYEES (BIRTH_DATE, EMAIL, NAME, PASSWORD, PHONE)
VALUES ('1990-05-15', 'john.doe@email.com', 'John Doe', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-123-4567'),
       ('1985-09-20', 'jane.smith@email.com', 'Jane Smith', 'abc456', '555-987-6543'),
       ('1978-03-08', 'bob.jones@email.com', 'Bob Jones', 'qwerty789', '555-321-6789'),
       ('1982-11-25', 'alice.white@email.com', 'Alice White', 'secret567', '555-876-5432'),
       ('1995-07-12', 'mike.wilson@email.com', 'Mike Wilson', 'mypassword', '555-234-5678'),
       ('1989-01-30', 'sara.brown@email.com', 'Sara Brown', 'letmein123', '555-876-5433'),
       ('1975-06-18', 'tom.jenkins@email.com', 'Tom Jenkins', 'pass4321', '555-345-6789'),
       ('1987-12-04', 'lisa.taylor@email.com', 'Lisa Taylor', 'securepwd', '555-789-0123'),
       ('1992-08-22', 'david.wright@email.com', 'David Wright', 'access123', '555-456-7890'),
       ('1980-04-10', 'emily.harris@email.com', 'Emily Harris', '1234abcd', '555-098-7654');

INSERT INTO CLIENTS (BALANCE, EMAIL, NAME, PASSWORD)
VALUES (1000.00, 'client1@example.com', 'Medelyn Wright', '$2a$10$AApRClXLfjOl8SlhEYH/NeOv6wW0XjtTIh18rbhvjud0PS54HFC4a'),
       (1500.50, 'client2@example.com', 'Landon Phillips', '$2a$10$p1VWeMNwM0lRet/pPWG/vuvNn6a92KWF7cyKLHNzQD8SG.ILmG8V2'),
       (800.75, 'client3@example.com', 'Harmony Mason', 'abc123'),
       (1200.25, 'client4@example.com', 'Archer Harper', 'pass456'),
       (900.80, 'client5@example.com', 'Kira Jacobs', 'letmein789'),
       (1100.60, 'client6@example.com', 'Maximus Kelly', 'adminpass'),
       (1300.45, 'client7@example.com', 'Sierra Mitchell', 'mypassword'),
       (950.30, 'client8@example.com', 'Quinton Saunders', 'test123'),
       (1050.90, 'client9@example.com', 'Amina Clarke', 'qwerty123'),
       (880.20, 'client10@example.com', 'Bryson Chavez', 'pass789');

INSERT INTO USER_ROLE(EMAIL, ROLE)
VALUES ('client1@example.com', 'ROLE_CLIENT'),
       ('client1@example.com', 'ROLE_BLOCKED'),
('client2@example.com', 'ROLE_CLIENT'),
('john.doe@email.com', 'ROLE_EMPLOYEE'),
('john.doe@email.com', 'ROLE_ADMIN');


INSERT INTO BOOKS (name, genre, age_group, price, publication_year, author, number_of_pages, characteristics,description, language)
VALUES ('The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey','An enthralling adventure of discovery', 'ENGLISH'),
       ('Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
       ('Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense','A gripping mystery that keeps you guessing', 'ENGLISH'),
       ('The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story','A beautiful journey of love and passion', 'ENGLISH'),
       ('Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280,'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
       ('Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists','A thrilling tale of danger and intrigue', 'ENGLISH'),
       ('Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500,'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
       ('Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms','A magical fantasy filled with wonder', 'ENGLISH'),
       ('Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work','A mystery that keeps you on the edge', 'ENGLISH'),
       ('Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair','A romance that sweeps you off your feet', 'ENGLISH');


INSERT INTO ORDERS (CLIENT_ID, EMPLOYEE_ID, ORDER_DATE, PRICE, ORDER_STATUS)
VALUES
    ((SELECT id FROM CLIENTS WHERE EMAIL = 'client1@example.com'),
     (SELECT id FROM EMPLOYEES WHERE EMAIL = 'john.doe@email.com'),
     CURRENT_TIMESTAMP, 24.99 + 16.50, 'PROCESSING'),

    ((SELECT id FROM CLIENTS WHERE EMAIL = 'client2@example.com'),
     (SELECT id FROM EMPLOYEES WHERE EMAIL = 'john.doe@email.com'),
     CURRENT_TIMESTAMP, 29.95, 'PROCESSING');

-- 2. Додаємо книги (BookItem) до цих замовлень
-- До замовлення №1 додаємо книги 'The Hidden Treasure' та 'Echoes of Eternity'
INSERT INTO BOOK_ITEM (ORDER_ID, BOOK_ID, QUANTITY)
VALUES
    ((SELECT ID FROM ORDERS WHERE CLIENT_ID = (SELECT id FROM CLIENTS WHERE EMAIL = 'client1@example.com') LIMIT 1),
    (SELECT id FROM BOOKS WHERE name = 'The Hidden Treasure'), 1),

    ((SELECT ID FROM ORDERS WHERE CLIENT_ID = (SELECT id FROM CLIENTS WHERE EMAIL = 'client1@example.com') LIMIT 1),
     (SELECT id FROM BOOKS WHERE name = 'Echoes of Eternity'), 1);

-- До замовлення №2 додаємо книгу 'Whispers in the Shadows'
INSERT INTO BOOK_ITEM (ORDER_ID, BOOK_ID, QUANTITY)
VALUES
    ((SELECT ID FROM ORDERS WHERE CLIENT_ID = (SELECT id FROM CLIENTS WHERE EMAIL = 'client2@example.com') LIMIT 1),
    (SELECT id FROM BOOKS WHERE name = 'Whispers in the Shadows'), 1);

-- 1. Створюємо кошики для перших двох клієнтів
-- Для клієнта client1@example.com (Medelyn Wright)
INSERT INTO CARTS (client_id, price)
VALUES ((SELECT id FROM CLIENTS WHERE EMAIL = 'client1@example.com'), 41.49);

-- Для клієнта client2@example.com (Landon Phillips)
INSERT INTO CARTS (client_id, price)
VALUES ((SELECT id FROM CLIENTS WHERE EMAIL = 'client2@example.com'), 29.95);

-- 2. Додаємо товари в ці кошики (CART_ITEMS)

-- Товари для Medelyn Wright (книги 'The Hidden Treasure' та 'Echoes of Eternity')
INSERT INTO CART_ITEMS (cart_id, book_id, quantity)
VALUES
    ((SELECT id FROM CARTS WHERE client_id = (SELECT id FROM CLIENTS WHERE EMAIL = 'client1@example.com')),
     (SELECT id FROM BOOKS WHERE name = 'The Hidden Treasure'), 1),

    ((SELECT id FROM CARTS WHERE client_id = (SELECT id FROM CLIENTS WHERE EMAIL = 'client1@example.com')),
     (SELECT id FROM BOOKS WHERE name = 'Echoes of Eternity'), 1);

-- Товари для Landon Phillips (книга 'Whispers in the Shadows')
INSERT INTO CART_ITEMS (cart_id, book_id, quantity)
VALUES
    ((SELECT id FROM CARTS WHERE client_id = (SELECT id FROM CLIENTS WHERE EMAIL = 'client2@example.com')),
     (SELECT id FROM BOOKS WHERE name = 'Whispers in the Shadows'), 1);