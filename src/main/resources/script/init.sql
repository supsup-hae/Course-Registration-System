-- ENUM 타입
DO
$$
BEGIN
        IF
NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
CREATE TYPE user_role AS ENUM ('STUDENT', 'CREATOR');
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'course_status') THEN
CREATE TYPE course_status AS ENUM ('DRAFT', 'OPEN', 'CLOSED');
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'enrollment_status') THEN
CREATE TYPE enrollment_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'WAITLISTED');
END IF;
END
$$;

-- users
CREATE TABLE IF NOT EXISTS users
(
    user_id
    BIGINT
    GENERATED
    ALWAYS AS
    IDENTITY
    NOT
    NULL,
    name
    VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL,
    password VARCHAR
(
    255
) NOT NULL,
    role user_role DEFAULT 'STUDENT' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    );

DO
$$
BEGIN
        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pk_users') THEN
ALTER TABLE users
    ADD CONSTRAINT PK_USERS PRIMARY KEY (user_id);
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_user_email') THEN
ALTER TABLE users
    ADD CONSTRAINT UK_USER_EMAIL UNIQUE (email);
END IF;
END
$$;

-- courses
CREATE TABLE IF NOT EXISTS courses
(
    course_id
    BIGINT
    GENERATED
    ALWAYS AS
    IDENTITY
    NOT
    NULL,
    creator_id
    BIGINT
    NOT
    NULL,
    title
    VARCHAR
(
    255
) NOT NULL,
    description TEXT NULL,
    price NUMERIC
(
    12,
    0
) DEFAULT 0 NOT NULL,
    capacity INT NOT NULL,
    status course_status DEFAULT 'DRAFT' NOT NULL,
    start_date TIMESTAMP NULL,
    end_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    );

DO
$$
BEGIN
        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pk_courses') THEN
ALTER TABLE courses
    ADD CONSTRAINT PK_COURSES PRIMARY KEY (course_id);
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_to_courses') THEN
ALTER TABLE courses
    ADD CONSTRAINT FK_USERS_TO_COURSES
        FOREIGN KEY (creator_id) REFERENCES users (user_id);
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_courses_capacity') THEN
ALTER TABLE courses
    ADD CONSTRAINT CK_COURSES_CAPACITY CHECK (capacity > 0);
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_courses_price') THEN
ALTER TABLE courses
    ADD CONSTRAINT CK_COURSES_PRICE CHECK (price >= 0);
END IF;
END
$$;

CREATE INDEX IF NOT EXISTS IX_COURSES_CREATOR ON courses (creator_id);

-- enrollments
CREATE TABLE IF NOT EXISTS enrollments
(
    enrollment_id
    BIGINT
    GENERATED
    ALWAYS AS
    IDENTITY
    NOT
    NULL,
    student_id
    BIGINT
    NOT
    NULL,
    course_id
    BIGINT
    NOT
    NULL,
    status
    enrollment_status
    DEFAULT
    'PENDING'
    NOT
    NULL,
    waitlist_order
    INT
    NULL,
    confirmed_at
    TIMESTAMP
    NULL,
    cancelled_at
    TIMESTAMP
    NULL,
    created_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP
    NOT
    NULL,
    updated_at
    TIMESTAMP
    DEFAULT
    CURRENT_TIMESTAMP
    NOT
    NULL
);

DO
$$
BEGIN
        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pk_enrollments') THEN
ALTER TABLE enrollments
    ADD CONSTRAINT PK_ENROLLMENTS PRIMARY KEY (enrollment_id);
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_to_enrollments') THEN
ALTER TABLE enrollments
    ADD CONSTRAINT FK_USERS_TO_ENROLLMENTS
        FOREIGN KEY (student_id) REFERENCES users (user_id);
END IF;

        IF
NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_courses_to_enrollments') THEN
ALTER TABLE enrollments
    ADD CONSTRAINT FK_COURSES_TO_ENROLLMENTS
        FOREIGN KEY (course_id) REFERENCES courses (course_id);
END IF;
END
$$;

CREATE INDEX IF NOT EXISTS IX_ENROLLMENTS_STUDENT ON enrollments (student_id);
CREATE INDEX IF NOT EXISTS IX_ENROLLMENTS_COURSE ON enrollments (course_id);
