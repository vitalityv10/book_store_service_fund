INSERT INTO employees (id, birth_date, email, name, password, phone)
VALUES
    ('e0000000-0000-0000-0000-000000000001', '1990-05-15', 'john.doe@email.com', 'John Doe', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-123-4567'),
    ('e0000000-0000-0000-0000-000000000002', '1985-09-20', 'jane.smith@email.com', 'Jane Smith', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-987-6543'),
    ('e0000000-0000-0000-0000-000000000003', '1978-03-08', 'bob.jones@email.com', 'Bob Jones', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-321-6789'),
    ('e0000000-0000-0000-0000-000000000004', '1982-11-25', 'alice.white@email.com', 'Alice White', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-876-5432'),
    ('e0000000-0000-0000-0000-000000000005', '1995-07-12', 'mike.wilson@email.com', 'Mike Wilson', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-234-5678'),
    ('e0000000-0000-0000-0000-000000000006', '1989-01-30', 'sara.brown@email.com', 'Sara Brown', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-876-5433'),
    ('e0000000-0000-0000-0000-000000000007', '1975-06-18', 'tom.jenkins@email.com', 'Tom Jenkins', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-345-6789'),
    ('e0000000-0000-0000-0000-000000000008', '1987-12-04', 'lisa.taylor@email.com', 'Lisa Taylor', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-789-0123'),
    ('e0000000-0000-0000-0000-000000000009', '1992-08-22', 'david.wright@email.com', 'David Wright', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-456-7890'),
    ('e0000000-0000-0000-0000-000000000010', '1980-04-10', 'emily.harris@email.com', 'Emily Harris', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke', '555-098-7654');

INSERT INTO clients (id, balance, email, name, password)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 1000.00, 'client1@example.com', 'Medelyn Wright', '$2a$10$AApRClXLfjOl8SlhEYH/NeOv6wW0XjtTIh18rbhvjud0PS54HFC4a'),
    ('c0000000-0000-0000-0000-000000000002', 1500.50, 'client2@example.com', 'Landon Phillips', '$2a$10$p1VWeMNwM0lRet/pPWG/vuvNn6a92KWF7cyKLHNzQD8SG.ILmG8V2'),
    ('c0000000-0000-0000-0000-000000000003', 800.75, 'client3@example.com', 'Harmony Mason', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000004', 1200.25, 'client4@example.com', 'Archer Harper', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000005', 900.80, 'client5@example.com', 'Kira Jacobs', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000006', 1100.60, 'client6@example.com', 'Maximus Kelly', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000007', 1300.45, 'client7@example.com', 'Sierra Mitchell', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000008', 950.30, 'client8@example.com', 'Quinton Saunders', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000009', 1050.90, 'client9@example.com', 'Amina Clarke', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke'),
    ('c0000000-0000-0000-0000-000000000010', 880.20, 'client10@example.com', 'Bryson Chavez', '$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke');
INSERT INTO user_role(id, role)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 'ROLE_CLIENT'),
    ('c0000000-0000-0000-0000-000000000001', 'ROLE_BLOCKED'),
    ('c0000000-0000-0000-0000-000000000002', 'ROLE_CLIENT'),
    ('e0000000-0000-0000-0000-000000000001', 'ROLE_EMPLOYEE'),
    ('e0000000-0000-0000-0000-000000000001', 'ROLE_ADMIN');

INSERT INTO books (id, name, genre, age_group, price, publication_year, author, number_of_pages, characteristics, description, language)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey', 'An enthralling adventure of discovery', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000002', 'Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000003', 'Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense', 'A gripping mystery that keeps you guessing', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000004', 'The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story', 'A beautiful journey of love and passion', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000005', 'Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280, 'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000006', 'Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists', 'A thrilling tale of danger and intrigue', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000007', 'Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500, 'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000008', 'Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms', 'A magical fantasy filled with wonder', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000009', 'Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work', 'A mystery that keeps you on the edge', 'ENGLISH'),
    ('b0000000-0000-0000-0000-000000000010', 'Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair', 'A romance that sweeps you off your feet', 'ENGLISH');

INSERT INTO orders (id, client_id, employee_id, order_date, price, order_status)
VALUES
---client1 ---employee1
('00000000-0000-0000-0000-000000000001',
 'c0000000-0000-0000-0000-000000000001',
 'e0000000-0000-0000-0000-000000000001',
 CURRENT_TIMESTAMP, 41.49, 'PROCESSING'),

---(client2)--- (employee1)
('00000000-0000-0000-0000-000000000002',
 'c0000000-0000-0000-0000-000000000002',
 'e0000000-0000-0000-0000-000000000001',
 CURRENT_TIMESTAMP, 29.95, 'PROCESSING');

---(client1)
INSERT INTO book_item (order_id, book_id, quantity)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 1),
    ('00000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 1);

-- (client2 )
INSERT INTO book_item (order_id, book_id, quantity)
VALUES
    ('00000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000003', 1);

--  (client1)
INSERT INTO carts (id, client_id, total_price)
VALUES ('ca000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 41.49);

--(client2)
INSERT INTO carts (id, client_id, total_price)
VALUES ('ca000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 29.95);


-- client1
INSERT INTO cart_items (cart_id, book_id, quantity)
VALUES
    ('ca000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 1),
    ('ca000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 1);

-- client2
INSERT INTO cart_items (cart_id, book_id, quantity)
VALUES
    ('ca000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000003', 1);