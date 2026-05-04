INSERT INTO students (name, email) VALUES ('Ali', 'ali@mail.com');
INSERT INTO students (name, email) VALUES ('Sara', 'sara@mail.com');

INSERT INTO professors (name, email) VALUES ('Dr. Müller', 'mueller@uni.de');

INSERT INTO events (title, created_by)
VALUES ('Study Group', 1);

INSERT INTO posts (content, author_student_id)
VALUES ('Hello World', 1);

INSERT INTO trusts (professor_id, student_id)
VALUES (1, 1);

INSERT INTO flames (from_student_id, to_student_id)
VALUES (1, 2);