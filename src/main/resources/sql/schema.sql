CREATE TABLE IF NOT EXISTS employees (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    birth_date DATE,
    email VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS clients (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    balance DECIMAL(19, 2),
    email VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_role (
    id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id, role)
);

CREATE TABLE IF NOT EXISTS books (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    genre VARCHAR(50),
    age_group VARCHAR(20) NOT NULL,
    price DECIMAL(19, 2) NOT NULL CHECK (price >= 0),
    publication_year DATE NOT NULL,
    author VARCHAR(100) NOT NULL,
    number_of_pages INT,
    characteristics VARCHAR(255),
    description TEXT,
    language VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    client_id UUID NOT NULL,
    employee_id UUID,
    order_date TIMESTAMP NOT NULL,
    price DECIMAL(19, 2) NOT NULL CHECK (price >= 0),
    order_status VARCHAR(50),
    FOREIGN KEY(client_id) REFERENCES clients(id),
    FOREIGN KEY(employee_id) REFERENCES employees(id)
);

CREATE TABLE IF NOT EXISTS book_item (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id UUID NOT NUll,
    book_id UUID NOT NULL,
    quantity INT CHECK (quantity >=1),
    FOREIGN KEY(order_id) REFERENCES orders(id),
    FOREIGN KEY(book_id) REFERENCES books(id)
);

CREATE TABLE IF NOT EXISTS carts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    client_id UUID NOT NUll,
    total_price DECIMAL(19, 2) NOT NULL CHECK (total_price >= 0),
    FOREIGN KEY(client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    cart_id UUID NOT NULL,
    book_id UUID NOT NULL,
    quantity INT CHECK (quantity >= 1),
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
