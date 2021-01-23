
CREATE TABLE family
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    water ENUM('SEA', 'FRESH') NOT NULL
);


CREATE TABLE fish
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(100),
    temperature INT,
    price INT,
    family_fk INT,
    CONSTRAINT FK_FishFamily FOREIGN KEY (family_fk)
    REFERENCES family(id)
);
