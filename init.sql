CREATE TABLE flashcard
(
    id             integer PRIMARY KEY AUTOINCREMENT,
    chat_id        integer      not null,
    question       varchar(255) not null,
    answer         varchar(255) not null,
    category       varchar(255) not null,
    showed_counter integer default 0
);

CREATE INDEX chat_id_index on flashcard (chat_id);